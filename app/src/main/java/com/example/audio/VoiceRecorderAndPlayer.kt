package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.min
import kotlin.math.roundToInt

data class PlaybackState(
    val activeFilePath: String? = null,
    val activeMessageId: Long? = null,
    val isPlaying: Boolean = false,
    val positionMs: Int = 0,
    val durationMs: Int = 0,
)

data class RecordingState(
    val isRecording: Boolean = false,
    val elapsedSeconds: Int = 0,
    val elapsedMillis: Long = 0L,
    val amplitudes: List<Float> = emptyList(),
)

data class VoiceRecordingResult(
    val file: File,
    val durationSeconds: Int,
    val waveform: ByteArray,
)

class VoiceAudioController(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var mediaPlayer: MediaPlayer? = null
    private var playbackJob: Job? = null

    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private var recordingJob: Job? = null
    private var recordingStartMs: Long = 0L
    private val capturedAmplitudes = mutableListOf<Float>()

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _recordingState = MutableStateFlow(RecordingState())
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    fun togglePlay(messageId: Long, filePath: String) {
        val current = _playbackState.value
        if (current.activeMessageId == messageId && current.activeFilePath == filePath) {
            val player = mediaPlayer
            if (player != null) {
                if (player.isPlaying) {
                    player.pause()
                    _playbackState.value = current.copy(isPlaying = false)
                } else {
                    player.start()
                    _playbackState.value = current.copy(isPlaying = true)
                    startPlaybackTicker()
                }
                return
            }
        }

        stopPlayback()
        val file = File(filePath)
        if (!file.exists()) return

        runCatching {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(filePath)
                prepare()
                setOnCompletionListener {
                    stopPlayback()
                }
                setOnErrorListener { _, _, _ ->
                    stopPlayback()
                    true
                }
                start()
            }
            mediaPlayer = player
            _playbackState.value = PlaybackState(
                activeFilePath = filePath,
                activeMessageId = messageId,
                isPlaying = true,
                positionMs = 0,
                durationMs = player.duration.coerceAtLeast(1),
            )
            startPlaybackTicker()
        }
    }

    private fun startPlaybackTicker() {
        playbackJob?.cancel()
        playbackJob = scope.launch {
            while (isActive) {
                val player = mediaPlayer ?: break
                if (player.isPlaying) {
                    _playbackState.value = _playbackState.value.copy(
                        positionMs = player.currentPosition,
                        durationMs = player.duration.coerceAtLeast(1),
                    )
                }
                delay(80L)
            }
        }
    }

    fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        runCatching {
            mediaPlayer?.stop()
        }
        runCatching {
            mediaPlayer?.release()
        }
        mediaPlayer = null
        _playbackState.value = PlaybackState()
    }

    private fun createRecorderInstance(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }

    private fun tryStartOggOpus(): Pair<MediaRecorder, File>? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        for (sampleRate in intArrayOf(48000, 16000)) {
            val outFile = File(context.cacheDir, "voice_${System.currentTimeMillis()}.ogg")
            var recorder: MediaRecorder? = null
            val ok = try {
                recorder = createRecorderInstance().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.OGG)
                    setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
                    setAudioChannels(1)
                    setAudioSamplingRate(sampleRate)
                    setAudioEncodingBitRate(if (sampleRate == 48000) 32000 else 24000)
                    setOutputFile(outFile.absolutePath)
                    prepare()
                    start()
                }
                true
            } catch (_: Throwable) {
                runCatching { recorder?.reset() }
                runCatching { recorder?.release() }
                outFile.delete()
                false
            }
            if (ok && recorder != null) {
                return recorder to outFile
            }
        }
        return null
    }

    private fun tryStartAacFallback(): Pair<MediaRecorder, File>? {
        val outFile = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
        var recorder: MediaRecorder? = null
        return try {
            recorder = createRecorderInstance().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioChannels(1)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(64000)
                setOutputFile(outFile.absolutePath)
                prepare()
                start()
            }
            recorder to outFile
        } catch (_: Throwable) {
            runCatching { recorder?.reset() }
            runCatching { recorder?.release() }
            outFile.delete()
            null
        }
    }

    fun startRecording(): Boolean {
        if (_recordingState.value.isRecording) return true
        stopPlayback()
        capturedAmplitudes.clear()

        val started = tryStartOggOpus() ?: tryStartAacFallback()
        if (started == null) {
            cancelRecording()
            return false
        }

        mediaRecorder = started.first
        recordingFile = started.second
        recordingStartMs = System.currentTimeMillis()
        _recordingState.value = RecordingState(isRecording = true, elapsedSeconds = 0, amplitudes = emptyList())

        recordingJob?.cancel()
        recordingJob = scope.launch {
            while (isActive && _recordingState.value.isRecording) {
                val rawAmp = runCatching { mediaRecorder?.maxAmplitude ?: 0 }.getOrDefault(0)
                val normalized = min(1f, (rawAmp / 18000f).coerceAtLeast(0f))
                capturedAmplitudes.add(normalized)
                val elapsedMs = (System.currentTimeMillis() - recordingStartMs).coerceAtLeast(0L)
                val elapsed = (elapsedMs / 1000L).toInt()
                val list = (_recordingState.value.amplitudes + normalized).takeLast(32)
                _recordingState.value = RecordingState(
                    isRecording = true,
                    elapsedSeconds = elapsed,
                    elapsedMillis = elapsedMs,
                    amplitudes = list,
                )
                delay(80L)
            }
        }
        return true
    }

    fun stopAndReturnRecording(): VoiceRecordingResult? {
        if (!_recordingState.value.isRecording) return null
        val elapsedMs = (System.currentTimeMillis() - recordingStartMs).coerceAtLeast(0L)
        val durationSec = (elapsedMs / 1000L).toInt().coerceAtLeast(1)
        val file = recordingFile
        val waveform = encodeTelegramWaveform(capturedAmplitudes)
        capturedAmplitudes.clear()
        recordingJob?.cancel()
        recordingJob = null
        var stopSucceeded = false
        runCatching {
            mediaRecorder?.stop()
            stopSucceeded = true
        }
        runCatching { mediaRecorder?.release() }
        mediaRecorder = null
        recordingFile = null
        _recordingState.value = RecordingState()
        if (!stopSucceeded || file == null || !file.exists() || file.length() < 64L) {
            file?.delete()
            return null
        }
        return VoiceRecordingResult(file = file, durationSeconds = durationSec, waveform = waveform)
    }

    fun cancelRecording() {
        capturedAmplitudes.clear()
        recordingJob?.cancel()
        recordingJob = null
        runCatching { mediaRecorder?.stop() }
        runCatching { mediaRecorder?.release() }
        mediaRecorder = null
        recordingFile?.delete()
        recordingFile = null
        _recordingState.value = RecordingState()
    }

    fun release() {
        stopPlayback()
        cancelRecording()
    }

    private fun encodeTelegramWaveform(samples: List<Float>): ByteArray {
        if (samples.isEmpty()) return byteArrayOf()
        val targetBars = samples.size.coerceIn(1, 64)
        val resampled = IntArray(targetBars) { idx ->
            val srcIdx = ((idx.toFloat() / targetBars) * samples.size).toInt().coerceIn(0, samples.lastIndex)
            (samples[srcIdx] * 31f).roundToInt().coerceIn(0, 31)
        }
        val totalBits = targetBars * 5
        val out = ByteArray((totalBits + 7) / 8)
        for (i in resampled.indices) {
            val value = resampled[i]
            val bitOffset = i * 5
            for (b in 0 until 5) {
                if ((value and (1 shl b)) != 0) {
                    val pos = bitOffset + b
                    out[pos / 8] = (out[pos / 8].toInt() or (1 shl (pos % 8))).toByte()
                }
            }
        }
        return out
    }
}
