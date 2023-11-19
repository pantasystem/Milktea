package net.pantasystem.milktea.note.view

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.EntryPointAccessors
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common_android_ui.NavigationEntryPointForBinding
import net.pantasystem.milktea.common_navigation.ChannelDetailNavigation
import net.pantasystem.milktea.common_navigation.MediaNavigationArgs
import net.pantasystem.milktea.common_navigation.UserDetailNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigationArgs
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.reaction.Reaction
import net.pantasystem.milktea.model.setting.ReactionPickerType
import net.pantasystem.milktea.note.NoteDetailActivity
import net.pantasystem.milktea.note.NoteEditorActivity
import net.pantasystem.milktea.note.R
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
                    .show(activity.supportFragmentManager, NoteOptionDialog.FRAGMENT_TAG)
            }
            is NoteCardAction.OnPollChoiceClicked -> {
                notesViewModel.vote(
                    action.noteId,
                    action.poll,
                    action.choice,
                )
            }
            is NoteCardAction.OnReactionButtonClicked -> {
                if (
                    action.note.currentNote.value.isReacted
                    && !action.note.currentNote.value.canReaction
                ) {
                    notesViewModel.deleteReactions(action.note.toShowNote.note.id)
                    return
                }
                if (action.note.toShowNote.note.isAcceptingOnlyLikeReaction) {
                    notesViewModel.toggleReaction(action.note.toShowNote.note.id, "❤️")
                    return
                }
                when (settingStore.reactionPickerType) {
                    ReactionPickerType.LIST -> {
                        ReactionSelectionDialog.newInstance(action.note.toShowNote.note.id)
                            .show(activity.supportFragmentManager, ReactionSelectionDialog.FRAGMENT_TAG)
                    }
                    ReactionPickerType.SIMPLE -> {
                        ReactionPickerDialog.newInstance(action.note.toShowNote.note.id)
                            .show(activity.supportFragmentManager, ReactionPickerDialog.FRAGMENT_TAG)
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
                        ).show(activity.supportFragmentManager, RemoteReactionEmojiSuggestionDialog.FRAGMENT_TAG)
                        return
                    }
                }

                notesViewModel.postReaction(action.note, action.reaction)
            }
            is NoteCardAction.OnReactionLongClicked -> {
                ReactionHistoryPagerDialog.newInstance(
                    action.note.toShowNote.note.id,
                    action.reaction
                ).show(activity.supportFragmentManager, ReactionHistoryPagerDialog.FRAGMENT_TAG)
            }
            is NoteCardAction.OnRenoteButtonClicked -> {
                RenoteBottomSheetDialog.newInstance(
                    action.note.note.note.id,
                    action.note.isRenotedByMe
                ).show(activity.supportFragmentManager, RenoteBottomSheetDialog.FRAGMENT_TAG)
            }
            is NoteCardAction.OnRenoteButtonLongClicked -> {
                RenotesBottomSheetDialog.newInstance(action.note.toShowNote.note.id)
                    .show(activity.supportFragmentManager, RenotesBottomSheetDialog.FRAGMENT_TAG)
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
            is NoteCardAction.OnMediaPreviewLongClicked -> {
                val context = activity
                val previewAbleFile = action.previewAbleFile
                val title = previewAbleFile?.source?.name
                val altText = previewAbleFile?.source?.comment
                val alertDialog = MaterialAlertDialogBuilder(context)
                alertDialog.setTitle(title)
                alertDialog.setMessage(altText)
                alertDialog.setNeutralButton("Exit") { intf, _ ->
                    intf.cancel()
                }
                alertDialog.show()
            }
            is NoteCardAction.OnMediaPreviewClicked -> {
                val previewAbleFileList = action.files
                val previewAbleFile = action.previewAbleFile
                val intent = EntryPointAccessors.fromActivity(
                    activity,
                    NavigationEntryPointForBinding::class.java
                )
                    .mediaNavigation().newIntent(
                        MediaNavigationArgs.Files(
                            files = previewAbleFileList.map { fvd ->
                                fvd.source
                            },
                            index = previewAbleFileList.indexOfFirst { f ->
                                f === previewAbleFile
                            })
                    )

                when(val thumbnailView = action.thumbnailView.get()) {
                    null -> activity.startActivity(intent)
                    else -> {
                        val compat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            activity,
                            thumbnailView,
                            "image"
                        )
                        activity.startActivity(intent, compat.toBundle())
                    }
                }

            }
            is NoteCardAction.OnSensitiveMediaPreviewClicked -> {
                if (settingStore.configState.value.isShowWarningDisplayingSensitiveMedia) {
                    val dialog = MaterialAlertDialogBuilder(activity)
                        .setTitle(R.string.confirm_display_sensitive_media_dialog_title)
                        .setMessage(R.string.confirm_display_sensitive_media_dialog_message)
                        .setNeutralButton(R.string.confirm_display_sensitive_media_dialog_neutral_button) { _, _ ->
                            notesViewModel.neverShowSensitiveMediaDialog()
                        }
                        .setPositiveButton(R.string.confirm_display_sensitive_media_dialog_positive_button) { _, _ ->
                            action.mediaViewData.show(action.targetIndex)
                        }
                        .setNegativeButton(android.R.string.cancel) { _, _ ->
                            // do nothing
                        }
                        .create()
                    dialog.show()
                } else {
                    action.mediaViewData.show(action.targetIndex)
                }

            }
        }
    }
}