package jp.panta.misskeyandroidclient.view.notes.detail

import android.media.Image
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemSimpleNoteBinding
import jp.panta.misskeyandroidclient.view.notes.poll.PollListAdapter
import jp.panta.misskeyandroidclient.view.notes.reaction.ReactionCountAdapter
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData

class NoteChildConversationAdapter(
  val notesViewModel: NotesViewModel,
  private val reactionCounterAdapter: ReactionCountAdapter
) : ListAdapter<PlaneNoteViewData, NoteChildConversationAdapter.SimpleNoteHolder>(object : DiffUtil.ItemCallback<PlaneNoteViewData>(){
    override fun areContentsTheSame(
        oldItem: PlaneNoteViewData,
        newItem: PlaneNoteViewData
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areItemsTheSame(oldItem: PlaneNoteViewData, newItem: PlaneNoteViewData): Boolean {
        return oldItem.id == newItem.id
    }
}){
    class SimpleNoteHolder(val binding: ItemSimpleNoteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: SimpleNoteHolder, position: Int) {
        holder.binding.note = getItem(position)
        holder.binding.reactionView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.binding.reactionView.adapter = reactionCounterAdapter
        holder.binding.notesViewModel = notesViewModel
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleNoteHolder {
        val binding = DataBindingUtil.inflate<ItemSimpleNoteBinding>(LayoutInflater.from(parent.context), R.layout.item_simple_note, parent, false)
        return SimpleNoteHolder(binding)
    }
}