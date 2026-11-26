package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.LocalStrings
import com.example.ui.theme.LocalTelegramColors
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

@Composable
fun VideoNoteCameraOverlay(
    isLocked: Boolean,
    shouldFinalizeSignal: Int,
    shouldCancelSignal: Int,
    onVideoRecorded: (File, Int) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val tgColors = LocalTelegramColors.current
    val strings = LocalStrings.current

    val latestOnVideoRecorded by rememberUpdatedState(onVideoRecorded)
    val latestOnCancel by rememberUpdatedState(onCancel)
    val coroutineScope = rememberCoroutineScope()

    var useFrontCamera by remember { mutableStateOf(true) }
    var flashEnabled by remember { mutableStateOf(false) }
    var elapsedDeciSeconds by remember { mutableIntStateOf(0) }
    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var outputFile by remember { mutableStateOf<File?>(null) }
    var pendingAction by remember { mutableStateOf<String?>(null) } // "SEND" or "CANCEL"
    var startTimeMs by remember { mutableStateOf(System.currentTimeMillis()) }
    var recordingStartedMs by remember { mutableStateOf(0L) }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    LaunchedEffect(Unit) {
        startTimeMs = System.currentTimeMillis()
        while (true) {
            elapsedDeciSeconds = ((System.currentTimeMillis() - startTimeMs) / 100L).toInt().coerceAtMost(600)
            delay(100L)
        }
    }

    LaunchedEffect(useFrontCamera) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            runCatching {
                val provider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val recorder = Recorder.Builder()
                    .setQualitySelector(QualitySelector.from(Quality.SD))
                    .build()
                val vc = VideoCapture.withOutput(recorder)
                val selector = if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                provider.unbindAll()
                val cam = provider.bindToLifecycle(lifecycleOwner, selector, preview, vc)
                boundCamera = cam
                videoCapture = vc
            }
        }, ContextCompat.getMainExecutor(context))
    }

    LaunchedEffect(flashEnabled, boundCamera) {
        runCatching {
            boundCamera?.cameraControl?.enableTorch(flashEnabled && !useFrontCamera)
        }
    }

    LaunchedEffect(videoCapture) {
        val vc = videoCapture ?: return@LaunchedEffect
        if (activeRecording != null || pendingAction == "CANCEL") return@LaunchedEffect
        val outFile = File(context.cacheDir, "vnote_${System.currentTimeMillis()}.mp4")
        outputFile = outFile
        val options = FileOutputOptions.Builder(outFile).build()
        val hasAudio = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        var pending = vc.output.prepareRecording(context, options)
        if (hasAudio) {
            @Suppress("MissingPermission")
            pending = pending.withAudioEnabled()
        }
        recordingStartedMs = System.currentTimeMillis()
        val started = pending.start(ContextCompat.getMainExecutor(context)) { event ->
            if (event is VideoRecordEvent.Finalize) {
                val f = outputFile
                val durSec = ((System.currentTimeMillis() - startTimeMs) / 1000L).toInt().coerceAtLeast(1)
                val allowableError = event.error == VideoRecordEvent.Finalize.ERROR_DURATION_LIMIT_REACHED ||
                    event.error == VideoRecordEvent.Finalize.ERROR_FILE_SIZE_LIMIT_REACHED
                if (pendingAction == "CANCEL" || (event.hasError() && !allowableError)) {
                    f?.delete()
                    latestOnCancel()
                } else if (pendingAction == "SEND" && f != null && f.exists() && f.length() > 0L) {
                    latestOnVideoRecorded(f, durSec)
                } else {
                    f?.delete()
                    latestOnCancel()
                }
            }
        }
        activeRecording = started
        if (pendingAction == "CANCEL") {
            started.stop()
        } else if (pendingAction == "SEND") {
            coroutineScope.launch {
                val elapsedSinceStart = System.currentTimeMillis() - recordingStartedMs
                if (elapsedSinceStart < 450L) delay(450L - elapsedSinceStart)
                runCatching { started.stop() }
            }
        }
    }

    LaunchedEffect(shouldFinalizeSignal) {
        if (shouldFinalizeSignal > 0) {
            pendingAction = "SEND"
            val rec = activeRecording
            if (rec != null) {
                val elapsedSinceStart = System.currentTimeMillis() - recordingStartedMs
                if (elapsedSinceStart < 450L) delay(450L - elapsedSinceStart)
                runCatching { rec.stop() }
            }
        }
    }

    LaunchedEffect(shouldCancelSignal) {
        if (shouldCancelSignal > 0) {
            pendingAction = "CANCEL"
            val rec = activeRecording
            if (rec != null) {
                runCatching { rec.stop() }
            } else {
                outputFile?.delete()
                latestOnCancel()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching {
                activeRecording?.stop()
                ProcessCameraProvider.getInstance(context).get().unbindAll()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.68f)),
    ) {
        // Centered 260.dp Circular Live Camera Preview matching screenshot
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(268.dp),
            contentAlignment = Alignment.Center,
        ) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .size(254.dp)
                    .clip(CircleShape),
            )
            val progress = (elapsedDeciSeconds / 600f).coerceIn(0f, 1f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = tgColors.accent,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                )
            }
        }

        // Bottom Flip + Flash pill matching IMG_20261125_222710_d22f6fd2.jpg
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 74.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1F2732).copy(alpha = 0.9f))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { useFrontCamera = !useFrontCamera }, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Rounded.Cameraswitch, contentDescription = "Switch camera", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = { flashEnabled = !flashEnabled }, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = if (flashEnabled) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                    contentDescription = "Flash",
                    tint = if (flashEnabled) Color(0xFFFFD54F) else Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        if (isLocked) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0xFF171E27))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFEF5350)))
                    Spacer(modifier = Modifier.width(10.dp))
                    val secs = elapsedDeciSeconds / 10
                    val deci = elapsedDeciSeconds % 10
                    Text(
                        text = String.format(Locale.US, "%d:%02d,%d", secs / 60, secs % 60, deci),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = strings.cancel,
                    color = Color(0xFFEF5350),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            pendingAction = "CANCEL"
                            activeRecording?.stop() ?: onCancel()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(tgColors.accent)
                        .clickable {
                            pendingAction = "SEND"
                            activeRecording?.stop() ?: onCancel()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}
