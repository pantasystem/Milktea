package net.pantasystem.milktea.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.note.timeline.NoteFontSizeBinder
import net.pantasystem.milktea.note.timeline.ReactionCountItemsFlexboxLayoutBinder
import net.pantasystem.milktea.note.timeline.ViewRecycler
import net.pantasystem.milktea.note.view.NoteCardAction
import net.pantasystem.milktea.note.view.NoteCardActionListenerAdapter
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData
import net.pantasystem.milktea.notification.databinding.ItemNotificationBinding
import net.pantasystem.milktea.notification.databinding.ItemNotificationEmptyBinding
import net.pantasystem.milktea.notification.databinding.ItemNotificationErrorBinding
import net.pantasystem.milktea.notification.databinding.ItemNotificationLoadingBinding
import net.pantasystem.milktea.notification.viewmodel.NotificationListItem
import net.pantasystem.milktea.notification.viewmodel.NotificationViewModel


class NotificationListAdapter constructor(
    private val configRepository: LocalConfigRepository,
    diffUtilCallBack: DiffUtil.ItemCallback<NotificationListItem>,
    val notificationViewModel: NotificationViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val onRetryButtonClicked: () -> Unit,
    private val onReauthenticateButtonClicked: () -> Unit,
    onNoteCardAction: (NoteCardAction) -> Unit,
) : ListAdapter<NotificationListItem, NotificationBaseViewHolder>(diffUtilCallBack) {


    private val noteCardActionListenerAdapter = NoteCardActionListenerAdapter(onNoteCardAction)
    private val binder = ReactionCountItemsFlexboxLayoutBinder(
        ViewRecycler()
    ) {
        noteCardActionListenerAdapter.onReactionCountAction(it)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            NotificationListItem.Empty -> NotificationViewType.Empty.ordinal
            is NotificationListItem.Error -> NotificationViewType.Error.ordinal
            NotificationListItem.Loading -> NotificationViewType.Loading.ordinal
            is NotificationListItem.Notification -> NotificationViewType.Notification.ordinal
        }
    }

    override fun onBindViewHolder(
        holder: NotificationBaseViewHolder,
        position: Int
    ) {
        when (val item = getItem(position)) {
            is NotificationListItem.Empty -> {
                (holder as NotificationEmptyViewHolder).onBind()
            }
            is NotificationListItem.Error -> {
                (holder as NotificationErrorViewHolder).onBind(item)
            }
            is NotificationListItem.Loading -> {
                (holder as NotificationLoading).onBind()
            }
            is NotificationListItem.Notification -> {
                (holder as NotificationViewHolder).onBind(item)
            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationBaseViewHolder {
        return when (NotificationViewType.values()[viewType]) {
            NotificationViewType.Error -> {
                NotificationErrorViewHolder(
                    ItemNotificationErrorBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    onReauthenticateButtonClicked
                )
            }
            NotificationViewType.Empty -> {
                NotificationEmptyViewHolder(
                    ItemNotificationEmptyBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    onRetryButtonClicked = onRetryButtonClicked
                )
            }
            NotificationViewType.Loading -> {
                NotificationLoading(
                    ItemNotificationLoadingBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            NotificationViewType.Notification -> {
                NotificationViewHolder(
                    ItemNotificationBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    lifecycleOwner,
                    notificationViewModel,
                    noteCardActionListenerAdapter,
                    binder,
                ).also {
                    val config = configRepository.get().getOrElse {
                        DefaultConfig.config
                    }
                    NoteFontSizeBinder.from(it.binding.simpleNote).bind(
                        contentFontSize = config.noteContentFontSize,
                        headerFontSize = config.noteHeaderFontSize,
                    )
                }
            }
        }
    }


}

sealed class NotificationBaseViewHolder(view: View) :
    RecyclerView.ViewHolder(view) {
}

class NotificationViewHolder(
    val binding: ItemNotificationBinding,
    private val lifecycleOwner: LifecycleOwner,
    private val notificationViewModel: NotificationViewModel,
    private val noteCardActionListenerAdapter: NoteCardActionListenerAdapter,
    private val binder: ReactionCountItemsFlexboxLayoutBinder,
) : NotificationBaseViewHolder(binding.root) {
    fun onBind(item: NotificationListItem.Notification) {
        binding.notification = item.notificationViewData
        binding.notificationViewModel = notificationViewModel
        binding.noteCardActionListener = noteCardActionListenerAdapter

        val note = item.notificationViewData.noteViewData
        note ?: return

        setReactionCounter(note, binding.simpleNote.reactionView)
        binding.lifecycleOwner = lifecycleOwner
        binding.executePendingBindings()
    }

    private var job: Job? = null

    private fun setReactionCounter(note: PlaneNoteViewData, reactionView: FlexboxLayout) {

        val reactionList = note.reactionCountsViewData.value



        job?.cancel()
        job = note.reactionCountsViewData.onEach {

            binder.bindReactionCounts(reactionView, note, it)
        }.flowWithLifecycle(lifecycleOwner.lifecycle).launchIn(lifecycleOwner.lifecycleScope)


        if (reactionList.isNotEmpty()) {
            reactionView.visibility = View.VISIBLE
        }

    }
}

class NotificationLoading(val binding: ItemNotificationLoadingBinding) :
    NotificationBaseViewHolder(binding.root) {
    fun onBind() = Unit
}

class NotificationErrorViewHolder(
    val binding: ItemNotificationErrorBinding,
    val onReauthenticateButtonClicked: () -> Unit
) :
    NotificationBaseViewHolder(binding.root) {
    fun onBind(item: NotificationListItem.Error) {
        binding.errorItem = item
        binding.errorView.isVisible = false
        binding.showErrorMessageButton.isVisible = true
        binding.errorView.text = item.throwable.toString()
        binding.showErrorMessageButton.setOnClickListener {
            binding.errorView.isVisible = true
        }
        binding.reauthenticateButton.setOnClickListener {
            onReauthenticateButtonClicked()
        }
    }
}

class NotificationEmptyViewHolder(
    val binding: ItemNotificationEmptyBinding,
    val onRetryButtonClicked: () -> Unit
) :
    NotificationBaseViewHolder(binding.root) {
    fun onBind() {
        binding.retryLoadButton.setOnClickListener {
            onRetryButtonClicked()
        }
    }
}

enum class NotificationViewType {
    Error, Empty, Loading, Notification
}