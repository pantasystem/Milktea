package net.pantasystem.milktea.note.reaction

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.pantasystem.milktea.note.databinding.ItemReactionBinding
import net.pantasystem.milktea.note.reaction.NoteReactionViewHelper.bindReactionCount
import net.pantasystem.milktea.note.reaction.ReactionHelper.applyBackgroundColor
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData

class ReactionCountAdapter(
    val reactionCountActionListener: (ReactionCountAction) -> Unit
) : ListAdapter<ReactionViewData, ReactionHolder>(
    reactionDiffUtilItemCallback
) {


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
        holder.onBind(getItem(position), note, reactionCountActionListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactionHolder {
        val binding = ItemReactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReactionHolder(binding)
    }


}

class ReactionHolder(val binding: ItemReactionBinding) : RecyclerView.ViewHolder(binding.root) {
    fun onBind(viewData: ReactionViewData, note: PlaneNoteViewData?, reactionCountActionListener: (ReactionCountAction) -> Unit) {

        if (note == null) {
            Log.w("ReactionCountAdapter", "noteがNullです。正常に処理が行われない可能性があります。")
        }
        binding.reactionLayout.applyBackgroundColor(viewData, note?.toShowNote?.note?.isMisskey ?: false)
        binding.reactionLayout.bindReactionCount(
            binding.reactionText,
            binding.reactionImage,
            viewData
        )

        binding.reactionCounter.text = viewData.reactionCount.count.toString()
        binding.root.setOnLongClickListener {
            val id = note?.toShowNote?.note?.id
            if (id != null) {
                reactionCountActionListener(
                    ReactionCountAction.OnLongClicked(
                        note,
                        viewData.reaction
                    )
                )
                true
            } else {
                false
            }
        }
        binding.root.setOnClickListener {
            note?.let {
                reactionCountActionListener(ReactionCountAction.OnClicked(note, viewData.reaction))
            }

        }
    }
}

sealed interface ReactionCountAction {
    data class OnClicked(val note: PlaneNoteViewData, val reaction: String) : ReactionCountAction
    data class OnLongClicked(val note: PlaneNoteViewData, val reaction: String) :
        ReactionCountAction
}