package jp.panta.misskeyandroidclient.ui.notes.view


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.*
import com.google.android.flexbox.*
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemHasReplyToNoteBinding
import jp.panta.misskeyandroidclient.databinding.ItemNoteBinding
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionCount
import jp.panta.misskeyandroidclient.ui.notes.view.poll.PollListAdapter
import jp.panta.misskeyandroidclient.ui.notes.view.reaction.ReactionCountAdapter
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.HasReplyToNoteViewData
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.NotesViewModel
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData
import java.lang.IllegalArgumentException

class TimelineListAdapter(
    diffUtilCallBack: DiffUtil.ItemCallback<PlaneNoteViewData>,
    private val lifecycleOwner: LifecycleOwner,
    private val notesViewModel: NotesViewModel
) : ListAdapter<PlaneNoteViewData, TimelineListAdapter.NoteViewHolderBase<ViewDataBinding>>(diffUtilCallBack){

    abstract class NoteViewHolderBase<out T: ViewDataBinding>(view: View) : RecyclerView.ViewHolder(view){
        abstract val binding: T
        private var mNoteIdAndPollListAdapter: Pair<Note.Id, PollListAdapter>? = null
        abstract val lifecycleOwner: LifecycleOwner
        abstract val reactionCountsView: RecyclerView
        abstract val notesViewModel: NotesViewModel

        private var reactionCountAdapter: ReactionCountAdapter? = null

        private val flexBoxLayoutManager: FlexboxLayoutManager by lazy {
            val flexBoxLayoutManager = FlexboxLayoutManager(reactionCountsView.context)
            flexBoxLayoutManager.alignItems = AlignItems.STRETCH
            reactionCountsView.layoutManager = flexBoxLayoutManager
            flexBoxLayoutManager
        }

        private val reactionCountsObserver = Observer<List<ReactionCount>> { counts ->
            if(reactionCountAdapter?.note?.id == mCurrentNote?.id) {
                bindReactionCountVisibility()

                reactionCountAdapter?.submitList(counts) {
                    reactionCountsView.itemAnimator = if(reactionCountsView.itemAnimator == null) {
                        DefaultItemAnimator()
                    }else{
                        reactionCountsView.itemAnimator
                    }
                }
            }
        }

        abstract fun onBind(note: PlaneNoteViewData)

        private var mCurrentNote: PlaneNoteViewData? = null

        fun bind(note: PlaneNoteViewData) {

            unbind()

            mCurrentNote = note
            bindReactionCounter()

            onBind(mCurrentNote!!)
            binding.lifecycleOwner = lifecycleOwner

            binding.executePendingBindings()
        }

        private fun unbind() {
            mCurrentNote?.reactionCounts?.removeObserver(reactionCountsObserver)
            mCurrentNote = null
        }

        private fun bindReactionCounter() {
            val note = mCurrentNote!!
            val reactionList = note.reactionCounts.value?.toList()?: emptyList()
            reactionCountAdapter = if(reactionCountAdapter != null && reactionCountAdapter?.note?.id == note.id){
                reactionCountAdapter!!
            }else{
                ReactionCountAdapter(notesViewModel)
            }
            reactionCountAdapter?.note = note
            reactionCountsView.adapter = reactionCountAdapter
            reactionCountsView.isNestedScrollingEnabled = false
            reactionCountsView.itemAnimator = if(reactionList.isEmpty()) DefaultItemAnimator() else null
            reactionCountAdapter?.submitList(reactionList) {
                reactionCountsView.itemAnimator = DefaultItemAnimator()
            }
            note.reactionCounts.observe(lifecycleOwner, reactionCountsObserver)
            reactionCountsView.layoutManager = flexBoxLayoutManager
        }
        
        private fun bindReactionCountVisibility() {
            val note = mCurrentNote!!
            val reactionList = note.reactionCounts.value?.toList()?: emptyList()
            reactionCountsView.visibility = if(reactionList.isNotEmpty()){
                View.VISIBLE
            }else{
                View.GONE
            }
        }
    }

    companion object{
        private const val NORMAL_NOTE = 0
        private const val HAS_REPLY_TO_NOTE = 1
    }

    inner class NoteViewHolder(override val binding: ItemNoteBinding): NoteViewHolderBase<ItemNoteBinding>(binding.root){

        override val lifecycleOwner: LifecycleOwner
            get() = this@TimelineListAdapter.lifecycleOwner
        override val reactionCountsView: RecyclerView
            get() = binding.simpleNote.reactionView
        override val notesViewModel: NotesViewModel
            get() = this@TimelineListAdapter.notesViewModel


        override fun onBind(note: PlaneNoteViewData) {
            binding.note = note
            binding.notesViewModel = notesViewModel

        }
    }

    inner class HasReplyToNoteViewHolder(override val binding: ItemHasReplyToNoteBinding): NoteViewHolderBase<ItemHasReplyToNoteBinding>(binding.root){
        override val lifecycleOwner: LifecycleOwner
            get() = this@TimelineListAdapter.lifecycleOwner

        override val reactionCountsView: RecyclerView
            get() = binding.simpleNote.reactionView
        override val notesViewModel: NotesViewModel
            get() = this@TimelineListAdapter.notesViewModel

        override fun onBind(note: PlaneNoteViewData) {
            if(note is HasReplyToNoteViewData){
                binding.hasReplyToNote = note

                binding.notesViewModel = notesViewModel
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

    override fun onBindViewHolder(p0: NoteViewHolderBase<ViewDataBinding>, position: Int) {

        val item = getItem(position)
        p0.bind(item)

    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): NoteViewHolderBase<ViewDataBinding> {
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





}

