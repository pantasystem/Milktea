package net.pantasystem.milktea.common_compose.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

interface HapticFeedback {
    fun performClickHapticFeedback()
    fun performLongClickHapticFeedback()
    fun performTickVibrateHapticFeedback()
}

@Composable
fun rememberHapticFeedback(): HapticFeedback {
    val context = LocalContext.current
    return remember {
        object : HapticFeedback {
            override fun performClickHapticFeedback() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK).let {
                        getVibrator().vibrate(it)
                    }
                }
            }

            override fun performLongClickHapticFeedback() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK).let {
                        getVibrator().vibrate(it)
                    }
                }
            }


            override fun performTickVibrateHapticFeedback() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK).let {
                        getVibrator().vibrate(it)
                    }
                }

            }

            @Suppress("DEPRECATION")
            private fun getVibrator() = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
}