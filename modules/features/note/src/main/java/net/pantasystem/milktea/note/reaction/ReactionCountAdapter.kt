package net.pantasystem.milktea.note.reaction

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.ItemReactionBinding
import net.pantasystem.milktea.note.reaction.NoteReactionViewHelper.bindReactionCount
import net.pantasystem.milktea.note.reaction.ReactionHelper.applyBackgroundColor
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData

class ReactionCountAdapter(
    val lifecycleOwner: LifecycleOwner,
    val reactionCountActionListener: (ReactionCountAction) -> Unit
) : ListAdapter<ReactionViewData, ReactionCountAdapter.ReactionHolder>(
    reactionDiffUtilItemCallback
) {
    class ReactionHolder(val binding: ItemReactionBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private val reactionDiffUtilItemCallback = object : DiffUtil.ItemCallback<ReactionViewData>() {
            override fun areContentsTheSame(
                oldItem: ReactionViewData,
                newItem: ReactionViewData
            ): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(
                oldItem: ReactionViewData,
                newItem: ReactionViewData
            ): Boolean {
                return oldItem.noteId == newItem.noteId
                        && oldItem.reactionCount.reaction == newItem.reactionCount.reaction
            }
        }
    }

    var note: PlaneNoteViewData? = null

    override fun onBindViewHolder(holder: ReactionHolder, position: Int) {
        val item = getItem(position)
        if (note == null) {
            Log.w("ReactionCountAdapter", "noteがNullです。正常に処理が行われない可能性があります。")
        }
        holder.binding.reactionLayout.applyBackgroundColor(item, note?.toShowNote?.note?.nodeInfo)
        holder.binding.reactionLayout.bindReactionCount(
            holder.binding.reactionText,
            holder.binding.reactionImage,
            item
        )

        holder.binding.reactionCounter.text = item.reactionCount.count.toString()

        holder.binding.root.setOnLongClickListener {
            val id = note?.toShowNote?.note?.id
            if (id != null) {
                note?.let {
                    reactionCountActionListener(
                        ReactionCountAction.OnLongClicked(
                            it,
                            item.reaction
                        )
                    )
                }
                true
            } else {
                false
            }
        }
        holder.binding.root.setOnClickListener {
            note?.let {
                reactionCountActionListener(ReactionCountAction.OnClicked(it, item.reaction))
            }

        }
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactionHolder {
        val binding = DataBindingUtil.inflate<ItemReactionBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_reaction,
            parent,
            false
        )
        return ReactionHolder(binding)
    }


}

sealed interface ReactionCountAction {
    data class OnClicked(val note: PlaneNoteViewData, val reaction: String) : ReactionCountAction
    data class OnLongClicked(val note: PlaneNoteViewData, val reaction: String) :
        ReactionCountAction
}