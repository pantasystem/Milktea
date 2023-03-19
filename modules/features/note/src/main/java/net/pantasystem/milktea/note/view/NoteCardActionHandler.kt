package net.pantasystem.milktea.note.view

import androidx.appcompat.app.AppCompatActivity
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common_navigation.ChannelDetailNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigationArgs
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.reaction.Reaction
import net.pantasystem.milktea.model.setting.ReactionPickerType
import net.pantasystem.milktea.note.NoteDetailActivity
import net.pantasystem.milktea.note.NoteEditorActivity
import net.pantasystem.milktea.note.option.NoteOptionDialog
import net.pantasystem.milktea.note.reaction.ReactionSelectionDialog
import net.pantasystem.milktea.note.reaction.RemoteReactionEmojiSuggestionDialog
import net.pantasystem.milktea.note.reaction.history.ReactionHistoryPagerDialog
import net.pantasystem.milktea.note.reaction.picker.ReactionPickerDialog
import net.pantasystem.milktea.note.renote.RenoteBottomSheetDialog
import net.pantasystem.milktea.note.renote.RenotesBottomSheetDialog
import net.pantasystem.milktea.note.viewmodel.NotesViewModel

class NoteCardActionHandler(
    val activity: AppCompatActivity,
    val notesViewModel: NotesViewModel,
    val settingStore: SettingStore,
    val userDetailNavigation: UserDetailNavigation,
    val channelDetailNavigation: ChannelDetailNavigation,
    val currentPageable: Pageable? = null,
) {

    fun onAction(action: NoteCardAction) {
        when (action) {
            is NoteCardAction.OnNoteCardClicked -> {
                activity.startActivity(
                    NoteDetailActivity.newIntent(
                        activity,
                        noteId = action.note.id,
                        fromPageable = currentPageable
                    )
                )
            }
            is NoteCardAction.OnOptionButtonClicked -> {
                NoteOptionDialog.newInstance(action.note.toShowNote.note.id, fromPageable = currentPageable)
                    .show(activity.supportFragmentManager, "")
            }
            is NoteCardAction.OnPollChoiceClicked -> {
                notesViewModel.vote(
                    action.noteId,
                    action.poll,
                    action.choice,
                )
            }
            is NoteCardAction.OnReactionButtonClicked -> {
                val myReaction = action.note.myReaction.value
                if (myReaction != null) {
                    notesViewModel.toggleReaction(action.note.toShowNote.note.id, myReaction)
                    return
                }
                if (action.note.toShowNote.note.isAcceptingOnlyLikeReaction) {
                    notesViewModel.toggleReaction(action.note.toShowNote.note.id, "❤️")
                    return
                }
                when (settingStore.reactionPickerType) {
                    ReactionPickerType.LIST -> {
                        ReactionSelectionDialog.newInstance(action.note.toShowNote.note.id)
                            .show(activity.supportFragmentManager, "MainActivity")
                    }
                    ReactionPickerType.SIMPLE -> {
                        ReactionPickerDialog.newInstance(action.note.toShowNote.note.id)
                            .show(activity.supportFragmentManager, "Activity")
                    }
                }
            }
            is NoteCardAction.OnReactionClicked -> {

                // NOTE: MisskeyだとLocalの以外のカスタム絵文字はリアクションとして送信できない
                if (action.note.toShowNote.note.type is Note.Type.Misskey) {
                    if (!Reaction(action.reaction).isLocal()) {
                        RemoteReactionEmojiSuggestionDialog.newInstance(
                            accountId = action.note.id.accountId,
                            noteId = action.note.toShowNote.note.id.noteId,
                            reaction = action.reaction
                        ).show(activity.supportFragmentManager, "")
                        return
                    }
                }

                notesViewModel.postReaction(action.note, action.reaction)
            }
            is NoteCardAction.OnReactionLongClicked -> {
                ReactionHistoryPagerDialog.newInstance(
                    action.note.toShowNote.note.id,
                    action.reaction
                ).show(activity.supportFragmentManager, "")
            }
            is NoteCardAction.OnRenoteButtonClicked -> {
                when(action.note.note.note.type) {
                    is Note.Type.Mastodon -> {
                        notesViewModel.toggleReblog(action.note.toShowNote.note.id)
                    }
                    is Note.Type.Misskey -> {
                        RenoteBottomSheetDialog.newInstance(
                            action.note.note.note.id,
                            action.note.isRenotedByMe
                        ).show(activity.supportFragmentManager, "")
                    }
                }
            }
            is NoteCardAction.OnRenoteButtonLongClicked -> {
                RenotesBottomSheetDialog.newInstance(action.note.toShowNote.note.id)
                    .show(activity.supportFragmentManager, "")
            }
            is NoteCardAction.OnReplyButtonClicked -> {
                activity.startActivity(
                    NoteEditorActivity.newBundle(
                        activity,
                        replyTo = action.note.toShowNote.note.id
                    )
                )
            }
            is NoteCardAction.OnUserClicked -> {
                val intent = userDetailNavigation.newIntent(UserDetailNavigationArgs.UserId(action.user.id))
                activity.startActivity(
                    intent
                )
            }
            is NoteCardAction.OnFavoriteButtonClicked -> {
                notesViewModel.onToggleFavoriteUseCase(action.note)
            }
            is NoteCardAction.OnChannelButtonClicked -> {
                if (currentPageable is Pageable.ChannelTimeline && currentPageable.channelId == action.channelId.channelId) {
                    return
                }
                activity.startActivity(
                    channelDetailNavigation.newIntent(action.channelId)
                )
            }
        }
    }
}