package net.pantasystem.milktea.note.timeline

import android.text.Spannable
import android.text.SpannableString
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.common_android.ui.FontSizeUnitConverter.specialPointToPixel
import net.pantasystem.milktea.note.databinding.ItemHasReplyToNoteBinding
import net.pantasystem.milktea.note.databinding.ItemNoteBinding
import net.pantasystem.milktea.note.databinding.ItemTimelineEmptyBinding
import net.pantasystem.milktea.note.databinding.ItemTimelineErrorBinding
import net.pantasystem.milktea.note.reaction.ReactionViewData
import net.pantasystem.milktea.note.timeline.viewmodel.TimelineListItem
import net.pantasystem.milktea.note.view.NoteCardActionListenerAdapter
import net.pantasystem.milktea.note.view.NoteUserRoleBadgeBinder.setUserRoleBadge
import net.pantasystem.milktea.note.viewmodel.HasReplyToNoteViewData
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData


sealed class TimelineListItemViewHolderBase(view: View) : RecyclerView.ViewHolder(view)

sealed class NoteViewHolderBase<out T : ViewDataBinding>(
    view: View,
    private val reactionCountBinder: ReactionCountItemsFlexboxLayoutBinder
) :
    TimelineListItemViewHolderBase(view) {

    abstract val binding: T
    abstract val lifecycleOwner: LifecycleOwner
    abstract val flexboxLayout: FlexboxLayout
    abstract val noteCardActionListenerAdapter: NoteCardActionListenerAdapter


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
//            reactionCountsView.itemAnimator?.endAnimations()

        mCurrentNote = null
    }

    private fun bindReactionCounter() {
        val note = mCurrentNote!!
        job = note.reactionCountsViewData.onEach { counts ->
            bindReactionCountVisibility(counts)
        }.flowWithLifecycle(lifecycleOwner.lifecycle).launchIn(lifecycleOwner.lifecycleScope)
    }

    private fun bindReactionCountVisibility(reactionCounts: List<ReactionViewData>?) {
        reactionCountBinder.bindReactionCounts(
            flexboxLayout,
            mCurrentNote,
            reactionCounts ?: emptyList(),
        )

        flexboxLayout.visibility = if (reactionCounts.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

}

class LoadingViewHolder(view: View) : TimelineListItemViewHolderBase(view)

class ErrorViewHolder(val binding: ItemTimelineErrorBinding) :
    TimelineListItemViewHolderBase(binding.root) {
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

class EmptyViewHolder(val binding: ItemTimelineEmptyBinding) :
    TimelineListItemViewHolderBase(binding.root)

class NoteViewHolder(
    override val binding: ItemNoteBinding,
    binder: ReactionCountItemsFlexboxLayoutBinder,
    override val lifecycleOwner: LifecycleOwner,
    override val noteCardActionListenerAdapter: NoteCardActionListenerAdapter
) :
    NoteViewHolderBase<ItemNoteBinding>(binding.root, binder) {

    override val flexboxLayout: FlexboxLayout
        get() = binding.simpleNote.reactionView



    init {
        listOf(
            binding.simpleNote.text,
            binding.simpleNote.cw,
            binding.simpleNote.mainName,
            binding.simpleNote.subName,

            binding.simpleNote.subNoteText,
            binding.simpleNote.subCw,
            binding.simpleNote.subNoteSubName,
            binding.simpleNote.subNoteMainName,
        ).forEach {
            it.setSpannableFactory(spannableFactory)
        }
    }

    override fun onBind(note: PlaneNoteViewData) {
        binding.note = note
        binding.noteCardActionListener = noteCardActionListenerAdapter
        val badgeIconSize =
            binding.root.context.specialPointToPixel(note.config.value.noteHeaderFontSize).toInt()
        binding.simpleNote.badgeRoles.apply {
            setUserRoleBadge(
                binding.simpleNote.noteLayout,
                note.toShowNote.user.iconBadgeRoles,
                badgeIconSize
            )
        }
    }


}

class HasReplyToNoteViewHolder(
    override val binding: ItemHasReplyToNoteBinding, binder: ReactionCountItemsFlexboxLayoutBinder,
    override val lifecycleOwner: LifecycleOwner,
    override val noteCardActionListenerAdapter: NoteCardActionListenerAdapter
) :
    NoteViewHolderBase<ItemHasReplyToNoteBinding>(binding.root, binder) {

    override val flexboxLayout: FlexboxLayout
        get() = binding.simpleNote.reactionView

    init {
        listOf(
            binding.simpleNote.text,
            binding.simpleNote.cw,
            binding.simpleNote.mainName,
            binding.simpleNote.subName,

            binding.simpleNote.subNoteText,
            binding.simpleNote.subCw,
            binding.simpleNote.subNoteSubName,
            binding.simpleNote.subNoteMainName,
        ).forEach {
            it.setSpannableFactory(spannableFactory)
        }
    }


    override fun onBind(note: PlaneNoteViewData) {
        if (note is HasReplyToNoteViewData) {
            binding.hasReplyToNote = note
            binding.noteCardActionListener = noteCardActionListenerAdapter
            binding.simpleNote.badgeRoles.apply {
                val badgeIconSize =
                    context.specialPointToPixel(note.config.value.noteHeaderFontSize).toInt()
                setUserRoleBadge(
                    binding.simpleNote.noteLayout,
                    note.toShowNote.user.iconBadgeRoles,
                    badgeIconSize
                )
            }
        }
    }

}

private val spannableFactory = object : Spannable.Factory() {
    override fun newSpannable(source: CharSequence?): Spannable {
        return (source as? Spannable?) ?: SpannableString(source)
    }
}