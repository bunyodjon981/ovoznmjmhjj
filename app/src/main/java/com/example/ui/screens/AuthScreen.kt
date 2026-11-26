package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.LocalStrings
import com.example.tdlib.AuthStage
import com.example.ui.components.CountryPickerSheet
import com.example.ui.components.PopularCountries
import com.example.ui.theme.LocalAppearanceSettings
import com.example.ui.theme.LocalTelegramColors
import com.example.ui.theme.TgBlue
import com.example.ui.theme.TgBlueDeep
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    authStage: AuthStage,
    authError: String?,
    isBusy: Boolean,
    canCancelAddAccount: Boolean = false,
    onCancelAddAccount: () -> Unit = {},
    onSelectLanguage: (AppLanguage) -> Unit,
    onSubmitPhone: (String) -> Unit,
    onSubmitCode: (String) -> Unit,
    onResendCode: () -> Unit,
    onSubmitPassword: (String) -> Unit,
    onSubmitRegistration: (String, String) -> Unit,
    onRequestQr: () -> Unit,
    onCancelToPhone: () -> Unit,
) {
    val strings = LocalStrings.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(authError) {
        if (!authError.isNullOrBlank()) {
            snackbarHostState.showSnackbar(authError)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = authStage,
                transitionSpec = {
                    (slideInHorizontally(tween(260)) { it / 5 } + fadeIn(tween(220)))
                        .togetherWith(slideOutHorizontally(tween(200)) { -it / 6 } + fadeOut(tween(180)))
                },
                label = "auth_stage_transition",
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 520.dp),
            ) { stage ->
                when (stage) {
                    is AuthStage.Initializing,
                    is AuthStage.Closed,
                    is AuthStage.Ready -> AuthInitializingView()
                    is AuthStage.WaitPhoneNumber -> PhoneInputView(
                        isBusy = isBusy,
                        canCancelAddAccount = canCancelAddAccount,
                        onCancelAddAccount = onCancelAddAccount,
                        onSelectLanguage = onSelectLanguage,
                        onSubmitPhone = onSubmitPhone,
                        onRequestQr = onRequestQr,
                    )
                    is AuthStage.WaitQrConfirmation -> QrAuthView(
                        link = stage.link,
                        onCopyLink = {
                            scope.launch { snackbarHostState.showSnackbar(strings.linkCopied) }
                        },
                        onBackToPhone = onCancelToPhone,
                    )
                    is AuthStage.WaitCode -> CodeVerificationView(
                        stage = stage,
                        isBusy = isBusy,
                        onSubmitCode = onSubmitCode,
                        onResendCode = onResendCode,
                        onWrongNumber = onCancelToPhone,
                    )
                    is AuthStage.WaitPassword -> PasswordVerificationView(
                        stage = stage,
                        isBusy = isBusy,
                        onSubmitPassword = onSubmitPassword,
                        onBackToPhone = onCancelToPhone,
                    )
                    is AuthStage.WaitRegistration -> RegistrationView(
                        isBusy = isBusy,
                        onSubmitRegistration = onSubmitRegistration,
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedTelegramEmblem(
    sizeDp: Int = 82,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.AutoMirrored.Rounded.Send,
) {
    val animationsOn = LocalAppearanceSettings.current.animationsEnabled
    val infinite = rememberInfiniteTransition(label = "hero_pulse")
    val pulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = if (animationsOn) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_scale",
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size((sizeDp + 20).dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(TgBlue.copy(alpha = 0.16f)),
        )
        Box(
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(TgBlue, TgBlueDeep))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size((sizeDp / 2).dp),
            )
        }
    }
}

@Composable
private fun AuthInitializingView() {
    val strings = LocalStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AnimatedTelegramEmblem(sizeDp = 88)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = strings.appName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(14.dp))
        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = strings.connecting,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PhoneInputView(
    isBusy: Boolean,
    canCancelAddAccount: Boolean,
    onCancelAddAccount: () -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSubmitPhone: (String) -> Unit,
    onRequestQr: () -> Unit,
) {
    val strings = LocalStrings.current
    val tgColors = LocalTelegramColors.current
    val currentLang = LocalAppearanceSettings.current.language
    var selectedCountry by remember { mutableStateOf(PopularCountries.first()) }
    var showCountrySheet by remember { mutableStateOf(false) }
    var showLangMenu by remember { mutableStateOf(false) }
    var phoneDigits by rememberSaveable { mutableStateOf("") }
    var syncContacts by rememberSaveable { mutableStateOf(false) }

    androidx.activity.compose.BackHandler(enabled = canCancelAddAccount) {
        onCancelAddAccount()
    }

    if (showCountrySheet) {
        CountryPickerSheet(
            onSelect = {
                selectedCountry = it
                showCountrySheet = false
            },
            onDismiss = { showCountrySheet = false },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top Back + Language Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (canCancelAddAccount) Arrangement.SpaceBetween else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (canCancelAddAccount) {
                    IconButton(onClick = onCancelAddAccount) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.cancel)
                    }
                }
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(tgColors.cardBackground)
                            .clickable { showLangMenu = true }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("auth_language_button"),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Language,
                            contentDescription = strings.languageTitle,
                            tint = tgColors.accent,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentLang.nativeName,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    DropdownMenu(expanded = showLangMenu, onDismissRequest = { showLangMenu = false }) {
                        AppLanguage.entries.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text("${lang.nativeName} (${lang.englishName})") },
                                onClick = {
                                    showLangMenu = false
                                    onSelectLanguage(lang)
                                },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = strings.yourPhoneNumber,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = strings.phoneSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Country selector card matching screenshot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(tgColors.cardBackground)
                    .border(1.dp, tgColors.divider, RoundedCornerShape(14.dp))
                    .clickable { showCountrySheet = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("country_picker_button"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = selectedCountry.flag, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.countryLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = tgColors.accent,
                    )
                    Text(
                        text = selectedCountry.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Phone number field card
            OutlinedTextField(
                value = phoneDigits,
                onValueChange = { input ->
                    phoneDigits = input.filter { it.isDigit() || it == ' ' || it == '-' }.take(15)
                },
                label = { Text(strings.phoneNumberLabel) },
                prefix = {
                    Text(
                        text = "${selectedCountry.dialCode}  |  ",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                trailingIcon = {
                    if (phoneDigits.isNotEmpty()) {
                        IconButton(onClick = { phoneDigits = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear")
                        }
                    }
                },
                placeholder = { Text("00 000 00 00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val cleaned = phoneDigits.filter { it.isDigit() }
                        if (cleaned.isNotEmpty() && !isBusy) {
                            onSubmitPhone("${selectedCountry.dialCode}$cleaned")
                        }
                    },
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("phone_number_input"),
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Kontaklarni sinxronlash checkbox matching IMG_20261125_222141.jpg
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { syncContacts = !syncContacts }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = syncContacts,
                    onCheckedChange = { syncContacts = it },
                    colors = CheckboxDefaults.colors(checkedColor = tgColors.accent),
                )
                Text(
                    text = strings.syncContacts,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onRequestQr,
                    enabled = !isBusy,
                    modifier = Modifier.testTag("qr_login_button"),
                ) {
                    Icon(Icons.Rounded.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.signInQr)
                }

                FloatingActionButton(
                    onClick = {
                        val cleaned = phoneDigits.filter { it.isDigit() }
                        if (cleaned.isNotEmpty() && !isBusy) {
                            onSubmitPhone("${selectedCountry.dialCode}$cleaned")
                        }
                    },
                    shape = CircleShape,
                    containerColor = tgColors.accent,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("auth_submit_button"),
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.5.dp, modifier = Modifier.size(22.dp))
                    } else {
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = strings.continueAction)
                    }
                }
            }
        }

        // Telegram Dark Numeric Keypad matching IMG_20261125_222141.jpg
        TelegramPhoneKeypad(
            onDigit = { d ->
                val raw = phoneDigits.filter { it.isDigit() }
                if (raw.length < 13) phoneDigits = raw + d
            },
            onBackspace = {
                if (phoneDigits.isNotEmpty()) phoneDigits = phoneDigits.dropLast(1)
            },
        )
    }
}

@Composable
private fun TelegramPhoneKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
) {
    val tgColors = LocalTelegramColors.current
    val rows = listOf(
        listOf("1" to "", "2" to "ABC", "3" to "DEF"),
        listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
        listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
        listOf("" to "", "0" to "+", "DEL" to ""),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(tgColors.headerBackground)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { (num, sub) ->
                    when (num) {
                        "" -> Spacer(modifier = Modifier.weight(1f).height(50.dp))
                        "DEL" -> Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(tgColors.cardBackground)
                                .clickable(onClick = onBackspace),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = "Backspace", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        else -> Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(tgColors.cardBackground)
                                .clickable { onDigit(num) }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = num,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium,
                            )
                            if (sub.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = sub,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QrAuthView(
    link: String,
    onCopyLink: () -> Unit,
    onBackToPhone: () -> Unit,
) {
    val strings = LocalStrings.current
    val clipboard = LocalClipboardManager.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AnimatedTelegramEmblem(sizeDp = 78, icon = Icons.Rounded.QrCode2)
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = strings.signInQr,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = strings.qrScanInstruction,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = link,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = {
                clipboard.setText(AnnotatedString(link))
                onCopyLink()
            },
            modifier = Modifier.testTag("copy_qr_link_button"),
        ) {
            Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.copyLoginLink)
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onBackToPhone) {
            Text(strings.signInPhone)
        }
    }
}

@Composable
private fun CodeVerificationView(
    stage: AuthStage.WaitCode,
    isBusy: Boolean,
    onSubmitCode: (String) -> Unit,
    onResendCode: () -> Unit,
    onWrongNumber: () -> Unit,
) {
    val strings = LocalStrings.current
    val tgColors = LocalTelegramColors.current
    var code by rememberSaveable { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onWrongNumber) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            AnimatedTelegramEmblem(sizeDp = 74, icon = Icons.Rounded.Sms)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stage.phoneNumber,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            val localizedHint = when {
                stage.deliveryHint.contains("SMS", ignoreCase = true) ->
                    String.format(strings.codeSentSms, stage.phoneNumber)
                stage.deliveryHint.contains("calling", ignoreCase = true) ->
                    String.format(strings.codeSentCall, stage.phoneNumber)
                stage.deliveryHint.contains("another device", ignoreCase = true) ->
                    strings.codeSentToTelegram
                else -> String.format(strings.codeSentGeneric, stage.phoneNumber)
            }
            Text(
                text = localizedHint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(28.dp))

            // Segmented animated OTP input boxes overlaid on a BasicTextField
            BasicTextField(
                value = code,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }.take(stage.codeLength)
                    code = filtered
                    if (filtered.length == stage.codeLength && !isBusy) {
                        onSubmitCode(filtered)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                cursorBrush = SolidColor(Color.Transparent),
                textStyle = TextStyle(color = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("otp_code_input"),
                decorationBox = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    ) {
                        repeat(stage.codeLength) { index ->
                            val char = code.getOrNull(index)?.toString() ?: ""
                            val isFocused = index == code.length
                            val borderColor by animateColorAsState(
                                targetValue = when {
                                    isFocused -> tgColors.accent
                                    char.isNotEmpty() -> tgColors.accent.copy(alpha = 0.7f)
                                    else -> tgColors.divider
                                },
                                label = "otp_border",
                            )
                            Box(
                                modifier = Modifier
                                    .size(width = 48.dp, height = 56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(tgColors.cardBackground)
                                    .border(1.6.dp, borderColor, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = char,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                },
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onWrongNumber) {
                    Text(strings.wrongNumber)
                }
                TextButton(onClick = onResendCode, enabled = !isBusy) {
                    Text(strings.resendCode)
                }
            }
        }

        FloatingActionButton(
            onClick = {
                if (code.isNotBlank() && !isBusy) {
                    onSubmitCode(code)
                }
            },
            shape = CircleShape,
            containerColor = tgColors.accent,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("otp_submit_button"),
        ) {
            if (isBusy) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.5.dp, modifier = Modifier.size(22.dp))
            } else {
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = strings.enterCode)
            }
        }
    }
}

@Composable
private fun PasswordVerificationView(
    stage: AuthStage.WaitPassword,
    isBusy: Boolean,
    onSubmitPassword: (String) -> Unit,
    onBackToPhone: () -> Unit,
) {
    val strings = LocalStrings.current
    val tgColors = LocalTelegramColors.current
    var password by rememberSaveable { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBackToPhone) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            AnimatedTelegramEmblem(sizeDp = 74, icon = Icons.Rounded.Lock)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = strings.twoStepVerification,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (stage.passwordHint.isNotBlank()) "${strings.passwordHintPrefix}${stage.passwordHint}"
                else strings.cloudPasswordHint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(28.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(strings.password) },
                singleLine = true,
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { visible = !visible }) {
                        Icon(
                            imageVector = if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = "Toggle password",
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { if (password.isNotEmpty() && !isBusy) onSubmitPassword(password) },
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cloud_password_input"),
            )
        }

        FloatingActionButton(
            onClick = { if (password.isNotEmpty() && !isBusy) onSubmitPassword(password) },
            shape = CircleShape,
            containerColor = tgColors.accent,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("password_submit_button"),
        ) {
            if (isBusy) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.5.dp, modifier = Modifier.size(22.dp))
            } else {
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = strings.continueAction)
            }
        }
    }
}

@Composable
private fun RegistrationView(
    isBusy: Boolean,
    onSubmitRegistration: (String, String) -> Unit,
) {
    val strings = LocalStrings.current
    val tgColors = LocalTelegramColors.current
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            AnimatedTelegramEmblem(sizeDp = 74, icon = Icons.Rounded.Person)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = strings.registerTitle,
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = strings.registerSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(28.dp))
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text(strings.firstName) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_first_name_input"),
            )
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text(strings.lastName) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reg_last_name_input"),
            )
        }

        FloatingActionButton(
            onClick = {
                if (firstName.isNotBlank() && !isBusy) {
                    onSubmitRegistration(firstName.trim(), lastName.trim())
                }
            },
            shape = CircleShape,
            containerColor = tgColors.accent,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("reg_submit_button"),
        ) {
            if (isBusy) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.5.dp, modifier = Modifier.size(22.dp))
            } else {
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = strings.continueAction)
            }
        }
    }
}
