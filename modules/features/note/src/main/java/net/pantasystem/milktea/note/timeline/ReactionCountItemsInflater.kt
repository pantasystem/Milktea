package net.pantasystem.milktea.note.timeline

import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayout
import net.pantasystem.milktea.common_android.ui.FontSizeHelper.setMemoFontSpSize
import net.pantasystem.milktea.common_android.ui.VisibilityHelper.setMemoVisibility
import net.pantasystem.milktea.note.databinding.ItemReactionBinding
import net.pantasystem.milktea.note.reaction.NoteReactionViewHelper.bindReactionCount
import net.pantasystem.milktea.note.reaction.ReactionCountAction
import net.pantasystem.milktea.note.reaction.ReactionHelper.applyBackgroundColor
import net.pantasystem.milktea.note.reaction.ReactionViewData
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData
import java.util.Stack

class ReactionCountItemsFlexboxLayoutBinder(
    val viewRecycler: ViewRecycler<View>,
    val reactionCountActionListener: (ReactionCountAction) -> Unit,
) {

    fun bindReactionCounts(flexboxLayout: FlexboxLayout, currentNote: PlaneNoteViewData?, reactionCounts: List<ReactionViewData>) {
        val currentViewsCount = flexboxLayout.childCount
        val newDataCount = reactionCounts.size

        // Step 1: Remove unnecessary views
        while (flexboxLayout.childCount > newDataCount) {

            val child = flexboxLayout.getChildAt(flexboxLayout.childCount - 1)
            flexboxLayout.removeViewAt(flexboxLayout.childCount - 1)
            Glide.with(child).clear(ItemReactionBinding.bind(child).reactionImage)
            viewRecycler.recycleView(child)
        }

        reactionCounts.forEachIndexed { index, reactionData ->
            val existingBinding: ItemReactionBinding? = if (index < currentViewsCount) {
                ItemReactionBinding.bind(flexboxLayout.getChildAt(index))
            } else null

            val binding = existingBinding ?: viewRecycler.getRecycledView {
                ItemReactionBinding.inflate(
                    LayoutInflater.from(flexboxLayout.context),
                    flexboxLayout,
                    false
                ).root
            }.let {
                ItemReactionBinding.bind(it)
            }

            // Step 2: Bind data to the view
            binding.apply {
                reactionLayout.applyBackgroundColor(
                    reactionData,
                    currentNote?.toShowNote?.note?.isMisskey ?: false
                )
                reactionLayout.bindReactionCount(
                    reactionText,
                    reactionImage,
                    reactionData,
                    (currentNote?.config?.value?.noteReactionCounterFontSize ?: 15f) * 1.2f
                )
                reactionCounter.text = reactionData.reactionCount.count.toString()
                reactionCounter.setMemoFontSpSize(
                    currentNote?.config?.value?.noteReactionCounterFontSize ?: 15f
                )
            }

            binding.root.setOnLongClickListener {
                val id = currentNote?.toShowNote?.note?.id
                if (id != null) {
                    reactionCountActionListener(
                        ReactionCountAction.OnLongClicked(
                            currentNote,
                            reactionData.reaction
                        )
                    )
                    true
                } else {
                    false
                }
            }
            binding.root.setOnClickListener {
                currentNote?.let {
                    reactionCountActionListener(ReactionCountAction.OnClicked(currentNote, reactionData.reaction))
                }
            }

            // Step 3: Add new view if necessary
            if (existingBinding == null) {
                flexboxLayout.addView(binding.root)
            }
        }
        flexboxLayout.setMemoVisibility(View.VISIBLE)
    }
}

class ViewRecycler<T> where T : View {

    private val recycledViews: Stack<T> = Stack()

    /**
     * 使わなくなったViewをリサイクル用のスタックに追加する
     */
    fun recycleView(view: T) {
        if (recycledViews.size < 40) {
            recycledViews.push(view)
        }
    }

    /**
     * 必要なビューを取得する。
     * リサイクルできるビューがあればそれを返し、なければnullを返す。
     */
    fun getRecycledView(onInflateView: () -> T): T {
        return if (recycledViews.isNotEmpty()) {
            recycledViews.pop()
        } else {
            onInflateView()
        }
    }

}
