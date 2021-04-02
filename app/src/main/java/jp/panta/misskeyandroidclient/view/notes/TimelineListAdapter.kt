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
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionCount
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
        var reactionCountsObserver: Observer<List<ReactionCount>>? = null
        abstract var reactionCountAdapter: ReactionCountAdapter?
        private var mNoteIdAndPollListAdapter: Pair<Note.Id, PollListAdapter>? = null


        abstract fun bind(note: PlaneNoteViewData)

        protected fun getPollAdapter(notesViewModel: NotesViewModel, note: PlaneNoteViewData, lifecycleOwner: LifecycleOwner): PollListAdapter?{
            val noteAndPoll = mNoteIdAndPollListAdapter
            if(note.poll != null){
                val pollAdapter = if(note.id != noteAndPoll?.first){
                    PollListAdapter(note.poll, notesViewModel, lifecycleOwner)
                }else{
                    noteAndPoll.second
                }
                mNoteIdAndPollListAdapter = note.id to pollAdapter
                return pollAdapter
            }
            return null
        }
    }

    companion object{
        private const val NORMAL_NOTE = 0
        private const val HAS_REPLY_TO_NOTE = 1
    }

    inner class NoteViewHolder(val binding: ItemNoteBinding): NoteViewHolderBase(binding.root){

        override var reactionCountAdapter: ReactionCountAdapter? = null

        override fun bind(note: PlaneNoteViewData) {
            binding.note = note
            reactionCountAdapter = setReactionCounter(this, binding.simpleNote.reactionView, note)
            binding.lifecycleOwner = lifecycleOwner
            binding.notesViewModel = notesViewModel

            if(note.poll != null){
                val pollAdapter = getPollAdapter(notesViewModel,note, lifecycleOwner)
                pollAdapter?.let{
                    binding.simpleNote.poll.adapter = pollAdapter
                    binding.simpleNote.poll.layoutManager = LinearLayoutManager(binding.root.context)
                }

            }
            binding.executePendingBindings()
        }
    }

    inner class HasReplyToNoteViewHolder(val binding: ItemHasReplyToNoteBinding): NoteViewHolderBase(binding.root){
        override var reactionCountAdapter: ReactionCountAdapter? = null

        override fun bind(note: PlaneNoteViewData) {
            if(note is HasReplyToNoteViewData){
                binding.hasReplyToNote = note
                setReactionCounter(this, binding.simpleNote.reactionView, note)

                binding.lifecycleOwner = lifecycleOwner
                binding.notesViewModel = notesViewModel
                if(note.poll != null){
                    val pollAdapter = getPollAdapter(notesViewModel,note, lifecycleOwner)
                    pollAdapter?.let{
                        binding.simpleNote.poll.adapter = pollAdapter
                        binding.simpleNote.poll.layoutManager = LinearLayoutManager(binding.root.context)
                    }

                }

                binding.executePendingBindings()

            }
        }
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
            p0.bind(item)

        }else if(p0 is HasReplyToNoteViewHolder){
            p0.bind(item)
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

    private fun setReactionCounter(holder: NoteViewHolderBase, reactionView: RecyclerView, note: PlaneNoteViewData) : ReactionCountAdapter{
        val reactionList = note.reactionCounts.value?.toList()?: emptyList()

        val adapter = holder.reactionCountAdapter

        val reactionCountAdapter = if(adapter?.note?.id == note.id){
            adapter
        }else{
            ReactionCountAdapter(note, notesViewModel)
        }

        reactionView.adapter = reactionCountAdapter

        reactionCountAdapter.submitList(reactionList)

        val observer = Observer<List<ReactionCount>> {
            reactionCountAdapter.submitList(it?.toList())
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

        return reactionCountAdapter
    }



}

