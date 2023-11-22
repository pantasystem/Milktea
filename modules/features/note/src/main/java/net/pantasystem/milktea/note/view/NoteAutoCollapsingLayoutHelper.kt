package net.pantasystem.milktea.note.view

import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.databinding.BindingAdapter
import net.pantasystem.milktea.common_android.ui.AutoCollapsingLayout
import net.pantasystem.milktea.common_android.ui.haptic.HapticFeedbackController

object NoteAutoCollapsingLayoutHelper {
    @JvmStatic
    @BindingAdapter("targetButton", "onExpandedChanged")
    fun AutoCollapsingLayout.setExpandedWithAnimation(
        targetButton: View,
        onExpandedChanged: (() -> Unit)?
    ) {

        val button = findExpandButton()
        button?.alpha = 1.0f

        targetButton.setOnClickListener {
            HapticFeedbackController.performClickHapticFeedback(it)

            val beforeHeight = measuredHeight

            val (_, maxHeight, _) = getWidthAndHeightAndButton(
                View.MeasureSpec.makeMeasureSpec(
                    0,
                    View.MeasureSpec.UNSPECIFIED
                ), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )

            val animator =
                ValueAnimator.ofInt(beforeHeight, maxHeight)
                    .setDuration(100).apply {
                        addUpdateListener {
                            onExpandedChanged?.invoke()
                            val newHeight = it.animatedValue as Int
                            setHeightAndInvalidate(newHeight)
                            button?.alpha = 1f - newHeight.toFloat() / maxHeight.toFloat()
                        }


                        doOnEnd {
                            isExpanded = true
                            hideExpandButton()
//                            findExpandButton()?.isVisible = false
                            onExpandedChanged?.invoke()
                            setHeightAndInvalidate(ViewGroup.LayoutParams.WRAP_CONTENT)
                        }
                        doOnCancel {
                            setHeightAndInvalidate(ViewGroup.LayoutParams.WRAP_CONTENT)
                        }
                    }


            setCurrentAnimator(animator)
            animator.start()
        }

    }
}