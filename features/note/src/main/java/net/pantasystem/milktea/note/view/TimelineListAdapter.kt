package net.pantasystem.milktea.note.view


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.marginTop
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayoutManager
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.reaction.ReactionCount
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.ItemHasReplyToNoteBinding
import net.pantasystem.milktea.note.databinding.ItemNoteBinding
import net.pantasystem.milktea.note.poll.PollListAdapter
import net.pantasystem.milktea.note.reaction.ReactionCountAdapter
import net.pantasystem.milktea.note.viewmodel.HasReplyToNoteViewData
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData

class TimelineListAdapter(
    diffUtilCallBack: DiffUtil.ItemCallback<PlaneNoteViewData>,
    private val lifecycleOwner: LifecycleOwner,
    val onAction: (NoteCardAction) -> Unit,
) : ListAdapter<PlaneNoteViewData, TimelineListAdapter.NoteViewHolderBase<ViewDataBinding>>(diffUtilCallBack){

    val cardActionListener = NoteCardActionListenerAdapter(onAction)

    abstract class NoteViewHolderBase<out T: ViewDataBinding>(view: View) : RecyclerView.ViewHolder(view){
        abstract val binding: T
        private var mNoteIdAndPollListAdapter: Pair<Note.Id, PollListAdapter>? = null
        abstract val lifecycleOwner: LifecycleOwner
        abstract val reactionCountsView: RecyclerView
        abstract val noteCardActionListenerAdapter: NoteCardActionListenerAdapter

        private var reactionCountAdapter: ReactionCountAdapter? = null

        private val flexBoxLayoutManager: FlexboxLayoutManager by lazy {
            val flexBoxLayoutManager = FlexboxLayoutManager(reactionCountsView.context)
            flexBoxLayoutManager.alignItems = AlignItems.STRETCH
            reactionCountsView.layoutManager = flexBoxLayoutManager
            flexBoxLayoutManager
        }

        @Suppress("ObjectLiteralToLambda")
        private val reactionCountsObserver = object : Observer<List<ReactionCount>> {
            override fun onChanged(counts: List<ReactionCount>?) {

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
        }

        abstract fun onBind(note: PlaneNoteViewData)
        abstract fun getAvatarIcon(): ImageView

        private var mCurrentNote: PlaneNoteViewData? = null

        fun bind(note: PlaneNoteViewData) {

            unbind()

            mCurrentNote = note
            bindReactionCounter()

            onBind(mCurrentNote!!)
            binding.lifecycleOwner = lifecycleOwner
            val parent = getAvatarIcon().parent
            if (parent is ViewGroup) {
                getAvatarIcon().y = getAvatarIcon().marginTop.toFloat() + parent.paddingTop
            }
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
                ReactionCountAdapter(lifecycleOwner) {
                    noteCardActionListenerAdapter.onReactionCountAction(it)
                }
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


        override val noteCardActionListenerAdapter: NoteCardActionListenerAdapter
            get() = this@TimelineListAdapter.cardActionListener

        override fun onBind(note: PlaneNoteViewData) {
            binding.note = note
            binding.noteCardActionListener = noteCardActionListenerAdapter

        }

        override fun getAvatarIcon(): ImageView {
            return binding.simpleNote.avatarIcon
        }
    }

    inner class HasReplyToNoteViewHolder(override val binding: ItemHasReplyToNoteBinding): NoteViewHolderBase<ItemHasReplyToNoteBinding>(binding.root){
        override val lifecycleOwner: LifecycleOwner
            get() = this@TimelineListAdapter.lifecycleOwner

        override val reactionCountsView: RecyclerView
            get() = binding.simpleNote.reactionView

        override val noteCardActionListenerAdapter: NoteCardActionListenerAdapter
            get() = this@TimelineListAdapter.cardActionListener

        override fun onBind(note: PlaneNoteViewData) {
            if(note is HasReplyToNoteViewData){
                binding.hasReplyToNote = note

                binding.noteCardActionListener = noteCardActionListenerAdapter

            }
        }
        override fun getAvatarIcon(): ImageView {
            return binding.simpleNote.avatarIcon
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

