package net.pantasystem.milktea.note.timeline


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
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
import net.pantasystem.milktea.model.notes.reaction.ReactionCount
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.ItemHasReplyToNoteBinding
import net.pantasystem.milktea.note.databinding.ItemNoteBinding
import net.pantasystem.milktea.note.databinding.ItemTimelineEmptyBinding
import net.pantasystem.milktea.note.databinding.ItemTimelineErrorBinding
import net.pantasystem.milktea.note.reaction.ReactionCountAdapter
import net.pantasystem.milktea.note.timeline.viewmodel.TimelineListItem
import net.pantasystem.milktea.note.view.NoteCardAction
import net.pantasystem.milktea.note.view.NoteCardActionListenerAdapter
import net.pantasystem.milktea.note.viewmodel.HasReplyToNoteViewData
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData

class TimelineListAdapter(
    private val lifecycleOwner: LifecycleOwner,
    val onRefreshAction: () -> Unit,
    val onReauthenticateAction: () -> Unit,
    val onAction: (NoteCardAction) -> Unit,
) : ListAdapter<TimelineListItem, TimelineListAdapter.TimelineListItemViewHolderBase>(object : DiffUtil.ItemCallback<TimelineListItem>() {
    override fun areContentsTheSame(
        oldItem: TimelineListItem,
        newItem: TimelineListItem,
    ): Boolean {
        if (oldItem is TimelineListItem.Note && newItem is TimelineListItem.Note) {
            return oldItem.note.id == newItem.note.id
        }
        return oldItem == newItem
    }

    override fun areItemsTheSame(
        oldItem: TimelineListItem,
        newItem: TimelineListItem,
    ): Boolean {
        if (oldItem is TimelineListItem.Note && newItem is TimelineListItem.Note) {
            return oldItem.note.id == newItem.note.id
        }
        return oldItem == newItem
    }
}){

    val cardActionListener = NoteCardActionListenerAdapter(onAction)

    sealed class TimelineListItemViewHolderBase(view: View) : RecyclerView.ViewHolder(view)

    sealed class NoteViewHolderBase<out T: ViewDataBinding>(view: View) : TimelineListItemViewHolderBase(view){
        abstract val binding: T
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

    class LoadingViewHolder(view: View) : TimelineListItemViewHolderBase(view)

    class ErrorViewHolder(val binding: ItemTimelineErrorBinding) : TimelineListItemViewHolderBase(binding.root) {
        fun bind(item: TimelineListItem.Error) {
            binding.errorItem = item
            binding.errorView.isVisible = false
            binding.showErrorMessageButton.isVisible = true
            binding.errorView.text = item.throwable.toString()
            binding.showErrorMessageButton.setOnClickListener {
                binding.errorView.isVisible = true
            }
        }
    }

    class EmptyViewHolder(val binding: ItemTimelineEmptyBinding) : TimelineListItemViewHolderBase(binding.root)

    enum class ViewHolderType {
        NormalNote, HasReplyToNote, Loading, Empty, Error
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

    }


    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when (item) {
            TimelineListItem.Empty -> ViewHolderType.Empty
            is TimelineListItem.Error -> ViewHolderType.Error
            TimelineListItem.Loading -> ViewHolderType.Loading
            is TimelineListItem.Note -> {
                if(item.note is HasReplyToNoteViewData){
                    ViewHolderType.HasReplyToNote
                }else{
                    ViewHolderType.NormalNote
                }
            }
        }.ordinal

    }

    override fun onBindViewHolder(p0: TimelineListItemViewHolderBase, position: Int) {

        val item = getItem(position)

        when(p0) {

            is LoadingViewHolder -> {
                // 何もしない
            }
            is HasReplyToNoteViewHolder -> {
                p0.bind((item as TimelineListItem.Note).note)
            }
            is NoteViewHolder -> {
                p0.bind((item as TimelineListItem.Note).note)
            }
            is EmptyViewHolder -> {
                p0.binding.retryLoadButton.setOnClickListener {
                    onRefreshAction()
                }
            }
            is ErrorViewHolder -> {
                p0.binding.retryLoadButton.setOnClickListener {
                    onRefreshAction()
                }
                p0.binding.reauthenticateButton.setOnClickListener {
                    onReauthenticateAction()
                }
                p0.bind((item as TimelineListItem.Error))
            }
        }

    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): TimelineListItemViewHolderBase {
        return when(ViewHolderType.values()[p1]) {
            ViewHolderType.NormalNote -> {
                val binding = DataBindingUtil.inflate<ItemNoteBinding>(LayoutInflater.from(p0.context), R.layout.item_note, p0, false)
                NoteViewHolder(binding)
            }
            ViewHolderType.HasReplyToNote -> {
                val binding = DataBindingUtil.inflate<ItemHasReplyToNoteBinding>(LayoutInflater.from(p0.context), R.layout.item_has_reply_to_note, p0, false)
                HasReplyToNoteViewHolder(binding)
            }
            ViewHolderType.Loading -> {
                LoadingViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_timeline_loading, p0, false))
            }
            ViewHolderType.Empty -> {
                EmptyViewHolder(DataBindingUtil.inflate(LayoutInflater.from(p0.context), R.layout.item_timeline_empty, p0, false))
            }
            ViewHolderType.Error -> {
                ErrorViewHolder(DataBindingUtil.inflate(LayoutInflater.from(p0.context), R.layout.item_timeline_error, p0, false))
            }
        }


    }





}

