package net.pantasystem.milktea.note.view

import android.animation.ValueAnimator
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import net.pantasystem.milktea.model.note.Note

object CwAnimationHelper {

    private val animatingNoteIds = mutableSetOf<Note.Id>()

    const val DURATION = 50L
    @BindingAdapter(
        "noteId" ,"targetView", "isVisible", "onToggleCw"
    )
    @JvmStatic
    fun View.setCwClickListenerWithAnimation(
        noteId: Note.Id?,
        targetView: View?,
        isVisible: Boolean?,
        onToggleCw: (() -> Unit)?
    ) {
        noteId?: return
        this.tag = noteId.noteId
        this.setOnClickListener {

            Log.d("CwAnimationHelper", "isVisible:$isVisible")
            if (animatingNoteIds.contains(noteId) || this.tag != noteId.noteId) {
                return@setOnClickListener
            }
            val animator = if (isVisible == false) {
                targetView!!.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                )
                val height = targetView.measuredHeight
                ValueAnimator.ofInt(height, 0).setDuration(DURATION).apply {
                    addUpdateListener {
                        val newHeight = it.animatedValue as Int

                        targetView.layoutParams.height = newHeight
                        if (targetView.parent is ViewGroup) {
                            targetView.parent.requestLayout()
                        }
                        targetView.invalidate()

                    }
                }
            } else {
                targetView!!.measure(
                    View.MeasureSpec.makeMeasureSpec(
                        0,
                        View.MeasureSpec.UNSPECIFIED
                    ), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                val height = targetView.measuredHeight
                ValueAnimator.ofInt(0, height).setDuration(DURATION).apply {
                    addUpdateListener {
                        val newHeight = it.animatedValue as Int

                        if (newHeight == 0) {
                            targetView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                            targetView.isVisible = true
                        }
                        targetView.layoutParams.height = newHeight

                        if (targetView.parent is ViewGroup) {
                            targetView.parent.requestLayout()
                        }
                        targetView.invalidate()

                        Log.d("CwAnimationHelper", "newHeight:$newHeight")
                    }

                }

            }
            animator.doOnEnd {
                targetView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                animatingNoteIds.remove(noteId)
                onToggleCw?.invoke()

            }
            animator.doOnStart {
                animatingNoteIds.add(noteId)
            }
            animator.start()


        }
    }
}