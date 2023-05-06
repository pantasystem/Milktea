package net.pantasystem.milktea.note.timeline


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.ItemHasReplyToNoteBinding
import net.pantasystem.milktea.note.databinding.ItemNoteBinding
import net.pantasystem.milktea.note.databinding.ItemTimelineEmptyBinding
import net.pantasystem.milktea.note.databinding.ItemTimelineErrorBinding
import net.pantasystem.milktea.note.reaction.ReactionCountAdapter
import net.pantasystem.milktea.note.reaction.ReactionViewData
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

    private val reactionCounterRecyclerViewPool = RecyclerView.RecycledViewPool().apply {
        setMaxRecycledViews(0, 30)
    }
    private val urlPreviewListRecyclerViewPool = RecyclerView.RecycledViewPool()
    private val manyFilePreviewListViewRecyclerViewPool = RecyclerView.RecycledViewPool()

    sealed class TimelineListItemViewHolderBase(view: View) : RecyclerView.ViewHolder(view)

    sealed class NoteViewHolderBase<out T: ViewDataBinding>(view: View) : TimelineListItemViewHolderBase(view){
        abstract val binding: T
        abstract val lifecycleOwner: LifecycleOwner
        abstract val reactionCountsView: RecyclerView
        abstract val noteCardActionListenerAdapter: NoteCardActionListenerAdapter

//        private var reactionCountAdapter: ReactionCountAdapter? = null


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

        private var job: Job? = null

        fun unbind() {
            job?.cancel()

            mCurrentNote = null
        }

        @Suppress("ObjectLiteralToLambda")
        private fun bindReactionCounter() {
            val reactionCountAdapter = ReactionCountAdapter {
                noteCardActionListenerAdapter.onReactionCountAction(it)
            }
            val note = mCurrentNote!!
            reactionCountAdapter.note = note

            val reactionList = note.reactionCountsViewData.value

            reactionCountsView.layoutManager = getLayoutManager()
            reactionCountsView.adapter = reactionCountAdapter
            reactionCountsView.isNestedScrollingEnabled = false
            reactionCountAdapter.submitList(reactionList)
            job = note.reactionCountsViewData.onEach { counts ->
                if(reactionCountAdapter.note?.id == mCurrentNote?.id) {
                    bindReactionCountVisibility(counts)
                    reactionCountAdapter.submitList(counts)
                }
            }.flowWithLifecycle(lifecycleOwner.lifecycle).launchIn(lifecycleOwner.lifecycleScope)
        }
        
        private fun bindReactionCountVisibility(reactionCounts: List<ReactionViewData>?) {
            reactionCountsView.visibility = if(reactionCounts.isNullOrEmpty()){
                View.GONE
            }else{
                View.VISIBLE
            }
        }

        private fun getLayoutManager(): FlexboxLayoutManager {
            val flexBoxLayoutManager = FlexboxLayoutManager(reactionCountsView.context)
            flexBoxLayoutManager.alignItems = AlignItems.STRETCH
            reactionCountsView.layoutManager = flexBoxLayoutManager
            flexBoxLayoutManager.recycleChildrenOnDetach = true
            return flexBoxLayoutManager
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
                binding.simpleNote.reactionView.setRecycledViewPool(reactionCounterRecyclerViewPool)
                binding.simpleNote.urlPreviewList.setRecycledViewPool(urlPreviewListRecyclerViewPool)
                binding.simpleNote.manyFilePreviewListView.setRecycledViewPool(manyFilePreviewListViewRecyclerViewPool)
                NoteFontSizeBinder.from(binding.simpleNote).bind(
                    headerFontSize = 15f,
                    contentFontSize = 15f,
                )
                NoteViewHolder(binding)
            }
            ViewHolderType.HasReplyToNote -> {
                val binding = DataBindingUtil.inflate<ItemHasReplyToNoteBinding>(LayoutInflater.from(p0.context), R.layout.item_has_reply_to_note, p0, false)
                binding.simpleNote.reactionView.setRecycledViewPool(reactionCounterRecyclerViewPool)
                binding.simpleNote.urlPreviewList.setRecycledViewPool(urlPreviewListRecyclerViewPool)
                binding.simpleNote.manyFilePreviewListView.setRecycledViewPool(manyFilePreviewListViewRecyclerViewPool)
                NoteFontSizeBinder.from(binding.simpleNote).bind(
                    headerFontSize = 15f,
                    contentFontSize = 15f,
                )
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


    override fun onViewRecycled(holder: TimelineListItemViewHolderBase) {
        super.onViewRecycled(holder)
        val simpleNote = when(holder) {
            is EmptyViewHolder -> return
            is ErrorViewHolder -> return
            is LoadingViewHolder -> return
            is HasReplyToNoteViewHolder -> {
                holder.binding.simpleNote
            }
            is NoteViewHolder -> {
                holder.binding.simpleNote
            }
        }
        val imageViews = listOf(
            simpleNote.avatarIcon,
            simpleNote.mediaPreview.thumbnailTopLeft,
            simpleNote.mediaPreview.thumbnailTopRight,
            simpleNote.mediaPreview.thumbnailBottomLeft,
            simpleNote.mediaPreview.thumbnailBottomRight,
            simpleNote.subAvatarIcon,
            simpleNote.subNoteMediaPreview.thumbnailBottomLeft,
            simpleNote.subNoteMediaPreview.thumbnailBottomRight,
            simpleNote.subNoteMediaPreview.thumbnailBottomLeft,
            simpleNote.subNoteMediaPreview.thumbnailBottomRight,

        )

        imageViews.map {
            Glide.with(simpleNote.avatarIcon).clear(it)
        }

        if (holder is NoteViewHolderBase<*>) {
            holder.unbind()
        }
    }


}

