package jp.panta.misskeyandroidclient.view.notes.reaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemReactionBinding
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionCount
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData

class ReactionCountAdapter(val note: PlaneNoteViewData, private val notesViewModel: NotesViewModel) : ListAdapter<ReactionCount, ReactionCountAdapter.ReactionHolder>(
    reactionDiffUtilItemCallback){
    class ReactionHolder(val binding: ItemReactionBinding): RecyclerView.ViewHolder(binding.root)

    companion object{
        private val reactionDiffUtilItemCallback = object : DiffUtil.ItemCallback<ReactionCount>(){
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

    override fun onBindViewHolder(holder: ReactionHolder, position: Int) {
        val item =  getItem(position)
        holder.binding.reaction = item//Pair(java.lang.String(item.first), Integer.valueOf(item.second))
        holder.binding.note = note
        holder.binding.notesViewModel = notesViewModel
        holder.binding.root.setOnLongClickListener {
            notesViewModel.setShowReactionHistoryDialog(note.toShowNote.note.id, item.reaction)
            false
        }
        holder.binding.executePendingBindings()
        //holder.binding.lifecycleOwner = lifecycleOwner
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactionHolder {
        val binding = DataBindingUtil.inflate<ItemReactionBinding>(LayoutInflater.from(parent.context), R.layout.item_reaction, parent, false)
        return ReactionHolder(binding)
    }
}