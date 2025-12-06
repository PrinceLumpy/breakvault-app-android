package com.princelumpy.breakvault.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

private const val PREFS_NAME = "timer_prefs"
private const val KEY_LAST_DURATION = "last_duration"

// Dummy resource references as requested. 
// Replace 0 with R.raw.your_file_name when files are added.
private const val RES_ID_BEEP = 0 // e.g., R.raw.beep
private const val RES_ID_START = 0 // e.g., R.raw.start_beep
private const val RES_ID_FINISH = 0 // e.g., R.raw.finish_beep

@Composable
fun TimerScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE) }
    val focusManager = LocalFocusManager.current
    
    // State
    var durationInput by remember { 
        mutableStateOf(prefs.getLong(KEY_LAST_DURATION, 30L).toString()) 
    }
    var timeLeft by remember { mutableStateOf(0L) }
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
        prefs.edit().putLong(KEY_LAST_DURATION, duration).apply()
    }
    
    fun playSound(resId: Int, fallbackTone: Int) {
        if (resId != 0) {
            try {
                MediaPlayer.create(context, resId)?.apply {
                    setOnCompletionListener { release() }
                    start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback if media creation fails
                toneGenerator.startTone(fallbackTone, 200)
            }
        } else {
            toneGenerator.startTone(fallbackTone, 200)
        }
    }

    LaunchedEffect(isRunning, isPreTimer) {
        if (isRunning) {
            val startTime = System.currentTimeMillis()
            // Use 3000L explicitly for pre-timer to ensure it is exactly 3 seconds
            val totalDuration = if (isPreTimer) 3100L else (timeLeft * 1000L) 
            // Added 100ms buffer to pre-timer to ensure the "3" is seen/heard clearly before tick
            
            val endTime = startTime + totalDuration
            
            // Track last seconds to trigger ticks
            var lastSeconds = if (isPreTimer) 4L else timeLeft + 1
            
            while (isRunning) {
                val now = System.currentTimeMillis()
                val remaining = endTime - now
                
                if (remaining <= 0) {
                    if (isPreTimer) {
                        // PRE-TIMER FINISHED -> GO!
                        playSound(RES_ID_START, ToneGenerator.TONE_DTMF_D) // Higher pitch for Start
                        isPreTimer = false
                        // Start main timer
                        val duration = durationInput.toLongOrNull() ?: 30L
                        timeLeft = duration
                        break // Restart effect for main timer
                    } else {
                        // MAIN TIMER FINISHED
                        playSound(RES_ID_FINISH, ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD) // Distinct finish sound
                        isRunning = false
                        timeLeft = 0
                        break
                    }
                } else {
                    // Calculate seconds for display (ceiling)
                    // e.g. 3000ms -> 3, 2900 -> 3, ..., 2000 -> 2 (wait, 2000/1000 = 2. We want 3 until it hits 2000?)
                    // Usually countdowns: 2999 is 2s 999ms, usually displayed as 3.
                    // When it hits 2000, it is exactly 2s. Display 2.
                    
                    // Logic: (remaining - 1) / 1000 + 1
                    val currentSeconds = if (remaining % 1000 == 0L) remaining / 1000 else (remaining / 1000) + 1
                    
                    if (currentSeconds != lastSeconds) {
                        // A second has passed
                        timeLeft = currentSeconds
                        lastSeconds = currentSeconds
                        
                        if (isPreTimer) {
                            // Play tick sound for 3, 2, 1
                            if (currentSeconds > 0) {
                                playSound(RES_ID_BEEP, ToneGenerator.TONE_DTMF_1)
                            }
                        }
                    }
                }
                delay(50) // Update frequently
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
            Button(onClick = { isRunning = false }) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Text("Stop")
            }
        } else {
            Text(
                text = "Set Timer (seconds)",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = durationInput,
                onValueChange = { 
                    if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                        durationInput = it
                        it.toLongOrNull()?.let { time -> saveDuration(time) }
                    }
                },
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
            
            Button(
                onClick = {
                    val duration = durationInput.toLongOrNull() ?: 30L
                    if (duration > 0) {
                        timeLeft = 3 // Start with 3s pre-timer visually
                        isPreTimer = true
                        isRunning = true
                        focusManager.clearFocus()
                    }
                }
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Text("Start")
            }
        }
    }
}
