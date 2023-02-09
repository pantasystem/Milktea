package net.pantasystem.milktea.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.*
import net.pantasystem.milktea.model.notes.reaction.ReactionCount
import net.pantasystem.milktea.note.databinding.ItemTimelineEmptyBinding
import net.pantasystem.milktea.note.reaction.ReactionCountAdapter
import net.pantasystem.milktea.note.view.NoteCardAction
import net.pantasystem.milktea.note.view.NoteCardActionListenerAdapter
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData
import net.pantasystem.milktea.notification.databinding.ItemNotificationBinding
import net.pantasystem.milktea.notification.databinding.ItemNotificationErrorBinding
import net.pantasystem.milktea.notification.viewmodel.NotificationListItem
import net.pantasystem.milktea.notification.viewmodel.NotificationViewData
import net.pantasystem.milktea.notification.viewmodel.NotificationViewModel


class NotificationListAdapter constructor(
    diffUtilCallBack: DiffUtil.ItemCallback<NotificationViewData>,
    val notificationViewModel: NotificationViewModel,
    private val lifecycleOwner: LifecycleOwner,
    onNoteCardAction: (NoteCardAction) -> Unit
) : ListAdapter<NotificationViewData, NotificationListAdapter.NotificationHolder>(diffUtilCallBack) {
    class NotificationHolder(val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val noteCardActionListenerAdapter = NoteCardActionListenerAdapter(onNoteCardAction)

    override fun onBindViewHolder(holder: NotificationHolder, position: Int) {
        holder.binding.notification = getItem(position)
        holder.binding.notificationViewModel = notificationViewModel
        holder.binding.noteCardActionListener = noteCardActionListenerAdapter

        val note = getItem(position).noteViewData
        note ?: return

        setReactionCounter(note, holder.binding.simpleNote.reactionView)
        holder.binding.lifecycleOwner = lifecycleOwner
        holder.binding.executePendingBindings()


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationHolder {
        val binding = DataBindingUtil.inflate<ItemNotificationBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_notification,
            parent,
            false
        )
        return NotificationHolder(binding)
    }

    private fun setReactionCounter(note: PlaneNoteViewData, reactionView: RecyclerView) {

        val reactionList = note.reactionCounts.value?.toList() ?: emptyList()
        val adapter = ReactionCountAdapter(lifecycleOwner) {
            noteCardActionListenerAdapter.onReactionCountAction(it)
        }
        adapter.note = note
        reactionView.adapter = adapter

        adapter.submitList(reactionList)

        val observer = Observer<List<ReactionCount>> {
            adapter.submitList(it.toList())
        }
        note.reactionCounts.observe(lifecycleOwner, observer)

        val exLayoutManager = reactionView.layoutManager
        if (exLayoutManager !is FlexboxLayoutManager) {
            val flexBoxLayoutManager = FlexboxLayoutManager(reactionView.context)
            flexBoxLayoutManager.flexDirection = FlexDirection.ROW
            flexBoxLayoutManager.flexWrap = FlexWrap.WRAP
            flexBoxLayoutManager.justifyContent = JustifyContent.FLEX_START
            flexBoxLayoutManager.alignItems = AlignItems.STRETCH
            reactionView.layoutManager = flexBoxLayoutManager
        }

        if (reactionList.isNotEmpty()) {
            reactionView.visibility = View.VISIBLE
        }

    }
}

sealed class NotificationBaseViewHolder<T : NotificationListItem>(view: View) :
    RecyclerView.ViewHolder(view) {
    abstract fun onBind(item: T)
}

class NotificationViewHolder(
    val binding: ItemNotificationBinding,
    private val lifecycleOwner: LifecycleOwner,
    private val notificationViewModel: NotificationViewModel,
    private val noteCardActionListenerAdapter: NoteCardActionListenerAdapter,
) : NotificationBaseViewHolder<NotificationListItem.Notification>(binding.root) {
    override fun onBind(item: NotificationListItem.Notification) {
        binding.notification = item.notificationViewData
        binding.notificationViewModel = notificationViewModel
        binding.noteCardActionListener = noteCardActionListenerAdapter

        val note = item.notificationViewData.noteViewData
        note ?: return

        setReactionCounter(note, binding.simpleNote.reactionView)
        binding.lifecycleOwner = lifecycleOwner
        binding.executePendingBindings()
    }

    private fun setReactionCounter(note: PlaneNoteViewData, reactionView: RecyclerView) {

        val reactionList = note.reactionCounts.value?.toList() ?: emptyList()
        val adapter = ReactionCountAdapter(lifecycleOwner) {
            noteCardActionListenerAdapter.onReactionCountAction(it)
        }
        adapter.note = note
        reactionView.adapter = adapter

        adapter.submitList(reactionList)

        val observer = Observer<List<ReactionCount>> {
            adapter.submitList(it.toList())
        }
        note.reactionCounts.observe(lifecycleOwner, observer)

        val exLayoutManager = reactionView.layoutManager
        if (exLayoutManager !is FlexboxLayoutManager) {
            val flexBoxLayoutManager = FlexboxLayoutManager(reactionView.context)
            flexBoxLayoutManager.flexDirection = FlexDirection.ROW
            flexBoxLayoutManager.flexWrap = FlexWrap.WRAP
            flexBoxLayoutManager.justifyContent = JustifyContent.FLEX_START
            flexBoxLayoutManager.alignItems = AlignItems.STRETCH
            reactionView.layoutManager = flexBoxLayoutManager
        }

        if (reactionList.isNotEmpty()) {
            reactionView.visibility = View.VISIBLE
        }

    }
}

class NotificationLoading(view: View) :
    NotificationBaseViewHolder<NotificationListItem.Loading>(view) {
    override fun onBind(item: NotificationListItem.Loading) = Unit
}

class NotificationErrorViewHolder(val binding: ItemNotificationErrorBinding) :
    NotificationBaseViewHolder<NotificationListItem.Error>(binding.root) {
    override fun onBind(item: NotificationListItem.Error) {
        binding.errorItem = item
        binding.errorView.isVisible = false
        binding.showErrorMessageButton.isVisible = true
        binding.errorView.text = item.throwable.toString()
        binding.showErrorMessageButton.setOnClickListener {
            binding.errorView.isVisible = true
        }
    }
}

class NotificationEmptyViewHolder(val binding: ItemTimelineEmptyBinding, val onRetryButtonClicked: () -> Unit) :
    NotificationBaseViewHolder<NotificationListItem.Empty>(binding.root) {
    override fun onBind(item: NotificationListItem.Empty) {
        binding.retryLoadButton.setOnClickListener {
            onRetryButtonClicked()
        }
    }
}