package net.pantasystem.milktea.note.timeline


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.ListPreloader.PreloadModelProvider
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.FixedPreloadSizeProvider
import net.pantasystem.milktea.common.glide.GlideApp
import net.pantasystem.milktea.common.glide.GlideUtils
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.ItemHasReplyToNoteBinding
import net.pantasystem.milktea.note.databinding.ItemNoteBinding
import net.pantasystem.milktea.note.timeline.viewmodel.TimelineListItem
import net.pantasystem.milktea.note.view.NoteCardAction
import net.pantasystem.milktea.note.view.NoteCardActionListenerAdapter
import net.pantasystem.milktea.note.viewmodel.HasReplyToNoteViewData
import java.util.Collections

class TimelineListAdapter(
    private val configRepository: LocalConfigRepository,
    private val lifecycleOwner: LifecycleOwner,
    val onRefreshAction: () -> Unit,
    val onReauthenticateAction: () -> Unit,
    val onAction: (NoteCardAction) -> Unit,
) : ListAdapter<TimelineListItem, TimelineListItemViewHolderBase>(
    object : DiffUtil.ItemCallback<TimelineListItem>() {
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
    },
) {

    val cardActionListener = NoteCardActionListenerAdapter(onAction)

    private val reactionCountItemsFlexboxLayoutBinder = ReactionCountItemsFlexboxLayoutBinder(
        ViewRecycler(),
    ) {
        cardActionListener.onReactionCountAction(it)
    }

    private val manyFilePreviewListViewRecyclerViewPool = RecyclerView.RecycledViewPool()

    enum class ViewHolderType {
        NormalNote, HasReplyToNote, Loading, Empty, Error
    }



    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when (item) {
            TimelineListItem.Empty -> ViewHolderType.Empty
            is TimelineListItem.Error -> ViewHolderType.Error
            TimelineListItem.Loading -> ViewHolderType.Loading
            is TimelineListItem.Note -> {
                if (item.note is HasReplyToNoteViewData) {
                    ViewHolderType.HasReplyToNote
                } else {
                    ViewHolderType.NormalNote
                }
            }
        }.ordinal

    }

    override fun onBindViewHolder(p0: TimelineListItemViewHolderBase, position: Int) {

        val item = getItem(position)

        when (p0) {

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
        val config = configRepository.get().getOrElse {
            DefaultConfig.config
        }
        return when (ViewHolderType.values()[p1]) {
            ViewHolderType.NormalNote -> {
                val binding = DataBindingUtil.inflate<ItemNoteBinding>(
                    LayoutInflater.from(p0.context),
                    R.layout.item_note,
                    p0,
                    false
                )
//                binding.simpleNote.reactionView.setRecycledViewPool(reactionCounterRecyclerViewPool)
//                binding.simpleNote.urlPreviewList.setRecycledViewPool(urlPreviewListRecyclerViewPool)
                binding.simpleNote.manyFilePreviewListView.setRecycledViewPool(
                    manyFilePreviewListViewRecyclerViewPool
                )
                NoteFontSizeBinder.from(binding.simpleNote).bind(
                    headerFontSize = config.noteHeaderFontSize,
                    contentFontSize = config.noteContentFontSize,
                )
                NoteViewHolder(binding, reactionCountItemsFlexboxLayoutBinder, lifecycleOwner, cardActionListener)
            }

            ViewHolderType.HasReplyToNote -> {
                val binding = DataBindingUtil.inflate<ItemHasReplyToNoteBinding>(
                    LayoutInflater.from(p0.context),
                    R.layout.item_has_reply_to_note,
                    p0,
                    false
                )
//                binding.simpleNote.reactionView.setRecycledViewPool(reactionCounterRecyclerViewPool)
//                binding.simpleNote.urlPreviewList.setRecycledViewPool(urlPreviewListRecyclerViewPool)
                binding.simpleNote.manyFilePreviewListView.setRecycledViewPool(
                    manyFilePreviewListViewRecyclerViewPool
                )
                NoteFontSizeBinder.from(binding.simpleNote).bind(
                    headerFontSize = config.noteHeaderFontSize,
                    contentFontSize = config.noteContentFontSize,
                )
                HasReplyToNoteViewHolder(binding, reactionCountItemsFlexboxLayoutBinder, lifecycleOwner, cardActionListener)
            }

            ViewHolderType.Loading -> {
                LoadingViewHolder(
                    LayoutInflater.from(p0.context)
                        .inflate(R.layout.item_timeline_loading, p0, false)
                )
            }

            ViewHolderType.Empty -> {
                EmptyViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(p0.context),
                        R.layout.item_timeline_empty,
                        p0,
                        false
                    )
                )
            }

            ViewHolderType.Error -> {
                ErrorViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(p0.context),
                        R.layout.item_timeline_error,
                        p0,
                        false
                    )
                )
            }
        }


    }


    override fun onViewRecycled(holder: TimelineListItemViewHolderBase) {
        super.onViewRecycled(holder)
        val simpleNote = when (holder) {
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
//        simpleNote.reactionView.itemAnimator?.endAnimations()

        imageViews.map {
            if (GlideUtils.isAvailableContextForGlide(it.context)) {
                Glide.with(it).clear(it)
            }
        }

        if (holder is NoteViewHolderBase<*>) {
            holder.unbind()
        }
    }


    inner class AvatarIconPreloadProvider(private val context: Context) : PreloadModelProvider<String> {
        override fun getPreloadItems(position: Int): MutableList<String> {
            return when(val item = getItem(position)) {
                TimelineListItem.Empty -> Collections.emptyList()
                is TimelineListItem.Error -> Collections.emptyList()
                TimelineListItem.Loading -> Collections.emptyList()
                is TimelineListItem.Note -> Collections.singletonList(item.note.avatarUrl)
            }
        }

        override fun getPreloadRequestBuilder(item: String): RequestBuilder<*> {
            return Glide.with(context)
                .load(item)
                .override(
                    avatarIconSize,
                )

        }

        fun setup(recyclerView: RecyclerView) {
            val modelProvider = AvatarIconPreloadProvider(context)
            val sizeProvider = FixedPreloadSizeProvider<String>(modelProvider.avatarIconSize, modelProvider.avatarIconSize)
            val loader = RecyclerViewPreloader(GlideApp.with(context), modelProvider, sizeProvider, 10)
            recyclerView.addOnScrollListener(loader)
        }
        val avatarIconSize: Int
            get() {
                return context.resources.getDimensionPixelSize(R.dimen.note_avatar_icon_size)
            }
    }

}

