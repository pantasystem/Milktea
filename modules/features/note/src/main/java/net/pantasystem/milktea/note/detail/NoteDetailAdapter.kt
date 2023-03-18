package net.pantasystem.milktea.note.detail

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
import com.google.android.flexbox.*
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.ItemConversationBinding
import net.pantasystem.milktea.note.databinding.ItemDetailNoteBinding
import net.pantasystem.milktea.note.databinding.ItemNoteBinding
import net.pantasystem.milktea.note.detail.viewmodel.NoteConversationViewData
import net.pantasystem.milktea.note.detail.viewmodel.NoteDetailViewData
import net.pantasystem.milktea.note.detail.viewmodel.NoteDetailViewModel
import net.pantasystem.milktea.note.reaction.ReactionCountAdapter
import net.pantasystem.milktea.note.reaction.ReactionViewData
import net.pantasystem.milktea.note.view.NoteCardAction
import net.pantasystem.milktea.note.view.NoteCardActionListenerAdapter
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData

class NoteDetailAdapter(
    private val noteDetailViewModel: NoteDetailViewModel,
    private val viewLifecycleOwner: LifecycleOwner,
    diffUtil: DiffUtil.ItemCallback<PlaneNoteViewData> = object : DiffUtil.ItemCallback<PlaneNoteViewData>(){
        override fun areContentsTheSame(
            oldItem: PlaneNoteViewData,
            newItem: PlaneNoteViewData
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areItemsTheSame(oldItem: PlaneNoteViewData, newItem: PlaneNoteViewData): Boolean {
            return oldItem.id == newItem.id
        }
    },
    val onAction: (NoteCardAction) -> Unit,
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

    val noteCardActionListenerAdapter = NoteCardActionListenerAdapter(onAction)

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
        //val reactionAdapter = createReactionAdapter(note)
        //val layoutManager = LinearLayoutManager(holder.itemView.context)
        when(holder){
            is NoteHolder ->{
                holder.binding.note = note
                setReactionCounter(note, holder.binding.simpleNote.reactionView)

                holder.binding.lifecycleOwner = viewLifecycleOwner
                holder.binding.noteCardActionListener = noteCardActionListenerAdapter
                holder.binding.executePendingBindings()
            }
            is DetailNoteHolder ->{
                holder.binding.note = note as NoteDetailViewData
                holder.binding.noteCardActionListener = noteCardActionListenerAdapter
                setReactionCounter(note, holder.binding.reactionView)
                holder.binding.lifecycleOwner = viewLifecycleOwner
                holder.binding.executePendingBindings()
            }
            is ConversationHolder ->{
                Log.d("NoteDetailAdapter", "conversation: ${(note as NoteConversationViewData).conversation.value?.size}")
                holder.binding.childrenViewData = note
                setReactionCounter(note, holder.binding.childNote.reactionView)

                holder.binding.noteDetailViewModel = noteDetailViewModel
                val adapter = NoteChildConversationAdapter(viewLifecycleOwner, onAction)
                holder.binding.conversationView.adapter = adapter
                holder.binding.conversationView.layoutManager = LinearLayoutManager(holder.itemView.context)
                holder.binding.noteCardActionListener = noteCardActionListenerAdapter
                note.conversation.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                }

                holder.binding.lifecycleOwner = viewLifecycleOwner

                holder.binding.executePendingBindings()
            }
        }

    }
    private fun setReactionCounter(note: PlaneNoteViewData, reactionView: RecyclerView){

        val reactionList = note.reactionCountsViewData.value?.toList()?: emptyList()
        val adapter = ReactionCountAdapter {
            noteCardActionListenerAdapter.onReactionCountAction(it)
        }
        adapter.note = note
        reactionView.adapter = adapter

        adapter.submitList(reactionList)

        val observer = Observer<List<ReactionViewData>> {
            adapter.submitList(it.toList())
        }
        note.reactionCountsViewData.observe(viewLifecycleOwner, observer)

        val exLayoutManager = reactionView.layoutManager
        if(exLayoutManager !is FlexboxLayoutManager){
            val flexBoxLayoutManager = FlexboxLayoutManager(reactionView.context)
            flexBoxLayoutManager.flexDirection = FlexDirection.ROW
            flexBoxLayoutManager.flexWrap = FlexWrap.WRAP
            flexBoxLayoutManager.justifyContent = JustifyContent.FLEX_START
            flexBoxLayoutManager.alignItems = AlignItems.STRETCH
            reactionView.layoutManager = flexBoxLayoutManager
        }

        if(reactionList.isNotEmpty()){
            reactionView.visibility = View.VISIBLE
        }

    }


}