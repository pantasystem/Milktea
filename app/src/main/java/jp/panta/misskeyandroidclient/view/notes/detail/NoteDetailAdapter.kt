package jp.panta.misskeyandroidclient.view.notes.detail

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemConversationBinding
import jp.panta.misskeyandroidclient.databinding.ItemDetailNoteBinding
import jp.panta.misskeyandroidclient.databinding.ItemNoteBinding
import jp.panta.misskeyandroidclient.view.notes.reaction.ReactionCountAdapter
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import jp.panta.misskeyandroidclient.viewmodel.notes.detail.NoteConversationViewData
import jp.panta.misskeyandroidclient.viewmodel.notes.detail.NoteDetailViewData
import jp.panta.misskeyandroidclient.viewmodel.notes.detail.NoteDetailViewModel
import java.lang.IllegalArgumentException

class NoteDetailAdapter(
    private val notesViewModel: NotesViewModel,
    private val noteDetailViewModel: NoteDetailViewModel,
    private val viewLifecycleOwner: LifecycleOwner,
    private val diffUtil: DiffUtil.ItemCallback<PlaneNoteViewData> = object : DiffUtil.ItemCallback<PlaneNoteViewData>(){
        override fun areContentsTheSame(
            oldItem: PlaneNoteViewData,
            newItem: PlaneNoteViewData
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areItemsTheSame(oldItem: PlaneNoteViewData, newItem: PlaneNoteViewData): Boolean {
            return oldItem.id == newItem.id
        }
    }
) : ListAdapter<PlaneNoteViewData, NoteDetailAdapter.ViewHolder>(diffUtil){

    companion object{
        const val NOTE = 0
        const val DETAIL = 1
        const val CONVERSATION = 2
    }

    abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    class NoteHolder(val binding: ItemNoteBinding) : ViewHolder(binding.root)
    class DetailNoteHolder(val binding: ItemDetailNoteBinding) : ViewHolder(binding.root)
    class ConversationHolder(val binding: ItemConversationBinding) : ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is NoteConversationViewData -> CONVERSATION
            is NoteDetailViewData -> DETAIL
            else -> NOTE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType){
            NOTE ->{
                val binding = DataBindingUtil.inflate<ItemNoteBinding>(LayoutInflater.from(parent.context), R.layout.item_note, parent, false)
                NoteHolder(binding)
            }
            DETAIL ->{
                val binding = DataBindingUtil.inflate<ItemDetailNoteBinding>(LayoutInflater.from(parent.context), R.layout.item_detail_note, parent, false)
                DetailNoteHolder(binding)
            }
            CONVERSATION ->{
                val binding = DataBindingUtil.inflate<ItemConversationBinding>(LayoutInflater.from(parent.context), R.layout.item_conversation, parent, false)
                ConversationHolder(binding)
            }
            else -> throw IllegalArgumentException("NOTE, DETAIL, CONVERSATIONしか許可されていません")

        }
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = getItem(position)
        val reactionAdapter = createReactionAdapter(note)
        val layoutManager = LinearLayoutManager(holder.itemView.context)
        when(holder){
            is NoteHolder ->{
                holder.binding.note = note
                holder.binding.notesViewModel = notesViewModel
                holder.binding.simpleNote.reactionView.apply{
                    this.layoutManager = layoutManager
                    adapter = reactionAdapter
                }
                holder.binding.executePendingBindings()
            }
            is DetailNoteHolder ->{
                holder.binding.note = note as NoteDetailViewData
                holder.binding.notesViewModel = notesViewModel
                holder.binding.reactionView.apply{
                    this.layoutManager = layoutManager
                    adapter = reactionAdapter
                }
                holder.binding.executePendingBindings()
            }
            is ConversationHolder ->{
                Log.d("NoteDetailAdapter", "conversation: ${(note as NoteConversationViewData).conversation.value?.size}")
                holder.binding.childrenViewData = note as NoteConversationViewData
                holder.binding.notesViewModel = notesViewModel
                holder.binding.childNote.reactionView.layoutManager = layoutManager
                holder.binding.childNote.reactionView.adapter = reactionAdapter
                holder.binding.noteDetailViewModel = noteDetailViewModel
                val adapter = NoteChildConversationAdapter(notesViewModel, reactionAdapter)
                holder.binding.conversationView.adapter = adapter
                holder.binding.conversationView.layoutManager = LinearLayoutManager(holder.itemView.context)
                note.conversation.observe(viewLifecycleOwner, Observer {
                    adapter.submitList(it)
                })

                holder.binding.executePendingBindings()
            }
        }

    }

    private val reactionCountAdapterDiffUtilItemCallback = object : DiffUtil.ItemCallback<Pair<String, Int>>(){
        override fun areContentsTheSame(
            oldItem: Pair<String, Int>,
            newItem: Pair<String, Int>
        ): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(
            oldItem: Pair<String, Int>,
            newItem: Pair<String, Int>
        ): Boolean {
            return oldItem.first == newItem.first
        }
    }
    private fun createReactionAdapter(note: PlaneNoteViewData): ReactionCountAdapter{
        return ReactionCountAdapter(reactionCountAdapterDiffUtilItemCallback, note, notesViewModel)
    }
}