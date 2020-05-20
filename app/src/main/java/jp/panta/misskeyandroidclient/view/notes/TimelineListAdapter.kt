package jp.panta.misskeyandroidclient.view.notes


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
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemHasReplyToNoteBinding
import jp.panta.misskeyandroidclient.databinding.ItemNoteBinding
import jp.panta.misskeyandroidclient.view.notes.poll.PollListAdapter
import jp.panta.misskeyandroidclient.view.notes.reaction.ReactionCountAdapter
import jp.panta.misskeyandroidclient.viewmodel.notes.HasReplyToNoteViewData
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import java.lang.IllegalArgumentException

class TimelineListAdapter(
    diffUtilCallBack: DiffUtil.ItemCallback<PlaneNoteViewData>,
    private val lifecycleOwner: LifecycleOwner,
    private val notesViewModel: NotesViewModel
) : ListAdapter<PlaneNoteViewData, TimelineListAdapter.NoteViewHolderBase>(diffUtilCallBack){

    abstract class NoteViewHolderBase(view: View) : RecyclerView.ViewHolder(view){
        var reactionCountsObserver: Observer<LinkedHashMap<String, Int>>? = null
    }
    class NoteViewHolder(val binding: ItemNoteBinding): NoteViewHolderBase(binding.root)
    class HasReplyToNoteViewHolder(val binding: ItemHasReplyToNoteBinding): NoteViewHolderBase(binding.root)

    companion object{
        private const val NORMAL_NOTE = 0
        private const val HAS_REPLY_TO_NOTE = 1
    }
    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if(item is HasReplyToNoteViewData){
            HAS_REPLY_TO_NOTE
        }else{
            NORMAL_NOTE
        }
    }

    override fun onBindViewHolder(p0: NoteViewHolderBase, position: Int) {

        val item = getItem(position)
        if(p0 is NoteViewHolder){
            p0.binding.note = item
            setReactionCounter(item, p0)
            p0.binding.lifecycleOwner = lifecycleOwner
            p0.binding.executePendingBindings()
            p0.binding.notesViewModel = notesViewModel
            if(item.poll != null){
                p0.binding.simpleNote.poll.adapter = PollListAdapter(item.poll, notesViewModel, lifecycleOwner)
                p0.binding.simpleNote.poll.layoutManager = LinearLayoutManager(p0.binding.root.context)
            }
        }else if(p0 is HasReplyToNoteViewHolder){
            p0.binding.hasReplyToNote = item as HasReplyToNoteViewData
            setReactionCounter(item, p0)
            p0.binding.lifecycleOwner = lifecycleOwner
            p0.binding.executePendingBindings()
            p0.binding.notesViewModel = notesViewModel
            if(item.poll != null){
                p0.binding.simpleNote.poll.adapter = PollListAdapter(item.poll, notesViewModel, lifecycleOwner)
                p0.binding.simpleNote.poll.layoutManager = LinearLayoutManager(p0.binding.root.context)
            }
        }


    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): NoteViewHolderBase {
        return when(p1){
            HAS_REPLY_TO_NOTE ->{
                val binding = DataBindingUtil.inflate<ItemHasReplyToNoteBinding>(LayoutInflater.from(p0.context), R.layout.item_has_reply_to_note, p0, false)
                HasReplyToNoteViewHolder(binding)
            }
            NORMAL_NOTE ->{
                val binding = DataBindingUtil.inflate<ItemNoteBinding>(LayoutInflater.from(p0.context), R.layout.item_note, p0, false)
                NoteViewHolder(binding)
            }
            else -> throw IllegalArgumentException("I don't know this type")
        }

    }

    private fun setReactionCounter(note: PlaneNoteViewData, holder: NoteViewHolderBase){
        val reactionView = when(holder){
            is NoteViewHolder ->{
                holder.binding.simpleNote.reactionView
            }
            is HasReplyToNoteViewHolder ->{
                holder.binding.simpleNote.reactionView
            }
            else ->{
                throw IllegalArgumentException()
            }
        }
        val reactionList = note.reactionCounts.value?.toList()?: emptyList()
        val adapter = ReactionCountAdapter(note, notesViewModel)
        reactionView.adapter = adapter

        adapter.submitList(reactionList)

        val observer = Observer<LinkedHashMap<String, Int>> {
            adapter.submitList(it?.toList())
        }
        holder.reactionCountsObserver = observer
        note.reactionCounts.observe(lifecycleOwner, observer)

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

