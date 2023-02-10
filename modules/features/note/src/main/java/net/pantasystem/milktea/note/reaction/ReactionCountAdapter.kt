package net.pantasystem.milktea.note.reaction

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import net.pantasystem.milktea.model.notes.reaction.ReactionCount
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.ItemReactionBinding
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData

class ReactionCountAdapter(
    val lifecycleOwner: LifecycleOwner,
    val reactionCountActionListener: (ReactionCountAction) -> Unit
) : ListAdapter<ReactionCount, ReactionCountAdapter.ReactionHolder>(
    reactionDiffUtilItemCallback
) {
    class ReactionHolder(val binding: ItemReactionBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private val reactionDiffUtilItemCallback = object : DiffUtil.ItemCallback<ReactionCount>() {
            override fun areContentsTheSame(
                oldItem: ReactionCount,
                newItem: ReactionCount
            ): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(
                oldItem: ReactionCount,
                newItem: ReactionCount
            ): Boolean {
                return oldItem.reaction == newItem.reaction
            }
        }
    }

    var note: PlaneNoteViewData? = null

    override fun onBindViewHolder(holder: ReactionHolder, position: Int) {
        val item = getItem(position)
        if (note == null) {
            Log.w("ReactionCountAdapter", "noteがNullです。正常に処理が行われない可能性があります。")
        }
        holder.binding.reaction =
            item//Pair(java.lang.String(item.first), Integer.valueOf(item.second))
        holder.binding.note = note
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
        holder.binding.lifecycleOwner = lifecycleOwner
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

    override fun onViewRecycled(holder: ReactionHolder) {
        super.onViewRecycled(holder)
        Glide.with(holder.binding.reactionImage)
            .clear(holder.binding.reactionImage)
    }
}

sealed interface ReactionCountAction {
    data class OnClicked(val note: PlaneNoteViewData, val reaction: String) : ReactionCountAction
    data class OnLongClicked(val note: PlaneNoteViewData, val reaction: String) :
        ReactionCountAction
}