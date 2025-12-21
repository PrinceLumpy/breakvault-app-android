package com.princelumpy.breakvault.ui.timer

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.princelumpy.breakvault.ui.theme.BreakVaultTheme
import kotlinx.coroutines.delay
import androidx.core.content.edit

private const val PREFS_NAME = "timer_prefs"
private const val KEY_LAST_DURATION = "last_duration"

// Dummy resource references as requested.
// Replace 0 with R.raw.your_file_name when files are added.
private const val RES_ID_BEEP = 0 // e.g., R.raw.beep
private const val RES_ID_START = 0 // e.g., R.raw.start_beep
private const val RES_ID_FINISH = 0 // e.g., R.raw.finish_beep

/**
 * The main, stateful screen composable that holds the timer logic and state.
 */
@Composable
fun TimerScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val focusManager = LocalFocusManager.current

    // State
    var durationInput by remember {
        mutableStateOf(prefs.getLong(KEY_LAST_DURATION, 30L).toString())
    }
    var timeLeft by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var isPreTimer by remember { mutableStateOf(false) } // 3-second countdown

    // ToneGenerator for fallback sounds
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_ALARM, 100) }

    // Cleanup ToneGenerator
    DisposableEffect(Unit) {
        onDispose {
            toneGenerator.release()
        }
    }

    // Save duration when modified
    fun saveDuration(duration: Long) {
        prefs.edit { putLong(KEY_LAST_DURATION, duration) }
    }

    fun playSound(resId: Int, fallbackTone: Int) {
        if (resId != 0) {
            try {
                MediaPlayer.create(context, resId)?.apply {
                    setOnCompletionListener { it.release() }
                    start()
                }
            } catch (_: Exception) {
                toneGenerator.startTone(fallbackTone, 200)
            }
        } else {
            toneGenerator.startTone(fallbackTone, 200)
        }
    }

    LaunchedEffect(isRunning, isPreTimer) {
        if (isRunning) {
            val startTime = System.currentTimeMillis()
            val totalDuration =
                if (isPreTimer) 3100L else (durationInput.toLongOrNull() ?: 30L) * 1000L
            val endTime = startTime + totalDuration

            var lastSeconds = if (isPreTimer) 4L else (durationInput.toLongOrNull() ?: 30L) + 1

            while (System.currentTimeMillis() < endTime && isRunning) {
                val remaining = endTime - System.currentTimeMillis()
                val currentSeconds = (remaining + 999) / 1000 // Ceiling division

                if (currentSeconds != lastSeconds) {
                    timeLeft = currentSeconds
                    lastSeconds = currentSeconds

                    if (isPreTimer && currentSeconds > 0) {
                        playSound(RES_ID_BEEP, ToneGenerator.TONE_DTMF_1)
                    }
                }
                delay(50)
            }

            if (isRunning) { // Only proceed if not manually stopped
                if (isPreTimer) {
                    // PRE-TIMER FINISHED -> GO!
                    playSound(RES_ID_START, ToneGenerator.TONE_DTMF_D)
                    isPreTimer = false
                    timeLeft = durationInput.toLongOrNull() ?: 30L
                    // The LaunchedEffect will restart for the main timer
                } else {
                    // MAIN TIMER FINISHED
                    playSound(RES_ID_FINISH, ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD)
                    isRunning = false
                    timeLeft = 0
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isRunning) {
            TimerRunning(
                timeLeft = timeLeft,
                isPreTimer = isPreTimer,
                onStopClick = {
                    isRunning = false
                    isPreTimer = false // Ensure pre-timer stops as well
                    timeLeft = 0
                }
            )
        } else {
            TimerSetup(
                durationInput = durationInput,
                onDurationChange = {
                    if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                        durationInput = it
                        it.toLongOrNull()?.let { time -> saveDuration(time) }
                    }
                },
                onStartClick = {
                    val duration = durationInput.toLongOrNull() ?: 30L
                    if (duration > 0) {
                        timeLeft = 3 // Start with 3s pre-timer visually
                        isPreTimer = true
                        isRunning = true
                        focusManager.clearFocus()
                    }
                }
            )
        }
    }
}

/**
 * A stateless composable for displaying the running timer.
 */
@Composable
private fun TimerRunning(
    timeLeft: Long,
    isPreTimer: Boolean,
    onStopClick: () -> Unit
) {
    Text(
        text = if (isPreTimer) "Get Ready!" else "Go!",
        style = MaterialTheme.typography.headlineSmall,
        color = if (isPreTimer) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
    )
    Text(
        text = "$timeLeft",
        style = MaterialTheme.typography.displayLarge,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(32.dp))
    Button(onClick = onStopClick) {
        Icon(Icons.Default.Stop, contentDescription = "Stop Timer")
        Text("Stop")
    }
}

/**
 * A stateless composable for setting up the timer.
 */
@Composable
private fun TimerSetup(
    durationInput: String,
    onDurationChange: (String) -> Unit,
    onStartClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    Text(
        text = "Set Timer (seconds)",
        style = MaterialTheme.typography.titleMedium
    )
    OutlinedTextField(
        value = durationInput,
        onValueChange = onDurationChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        modifier = Modifier.padding(vertical = 16.dp)
    )

    Button(onClick = onStartClick) {
        Icon(Icons.Default.PlayArrow, contentDescription = "Start Timer")
        Text("Start")
    }
}

//region Previews

@Preview(showBackground = true)
@Composable
private fun TimerSetupPreview() {
    BreakVaultTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimerSetup(
                durationInput = "60",
                onDurationChange = {},
                onStartClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimerRunning_PreTimer_Preview() {
    BreakVaultTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimerRunning(
                timeLeft = 3,
                isPreTimer = true,
                onStopClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimerRunning_MainTimer_Preview() {
    BreakVaultTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimerRunning(
                timeLeft = 45,
                isPreTimer = false,
                onStopClick = {}
            )
        }
    }
}

//endregion
