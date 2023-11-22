package net.pantasystem.milktea.common_android.ui.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.appcompat.app.AppCompatActivity

object HapticFeedbackController {

    fun performClickHapticFeedback(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
    }

    fun performLongClickHapticFeedback(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    fun performToggledHapticFeedback(view: View, isChecked: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (isChecked) {
                view.performHapticFeedback(HapticFeedbackConstants.TOGGLE_ON)
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.TOGGLE_OFF)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
    }

    @Suppress("DEPRECATION")
    fun performTickVibrateHapticFeedback(context: Context) {
        val vibratorManager =
            context.getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK).let {
                vibratorManager.vibrate(it)
            }
        }
    }
}