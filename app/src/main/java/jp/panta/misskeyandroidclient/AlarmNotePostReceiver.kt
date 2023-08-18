package jp.panta.misskeyandroidclient

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.notification.NotificationUtil
import net.pantasystem.milktea.data.infrastructure.note.draft.db.DraftNoteDao
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.note.CreateNoteUseCase
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteRepository
import net.pantasystem.milktea.model.note.toCreateNote
import net.pantasystem.milktea.note.NoteDetailActivity
import net.pantasystem.milktea.note.NoteEditorActivity
import javax.inject.Inject

@AndroidEntryPoint
class AlarmNotePostReceiver : BroadcastReceiver() {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID: String = "SCHEDULE_POST_NOTE_RESULT_NOTIFICATION"
    }

    @Inject
    lateinit var draftNoteDAO: DraftNoteDao
    @Inject
    lateinit var accountRepository: AccountRepository
    @Inject
    lateinit var coroutineScope: CoroutineScope
    @Inject
    lateinit var noteRepository: NoteRepository

    @Inject
    lateinit var createNoteUseCase: CreateNoteUseCase

    @Inject
    lateinit var notificationUtil: NotificationUtil

    override fun onReceive(context: Context, intent: Intent) {
        val draftNoteId = intent.getLongExtra("DRAFT_NOTE_ID", -1)
        val accountId = intent.getLongExtra("ACCOUNT_ID", -1)
        require(draftNoteId >= 0)
        require(accountId >= 0)

        val notificationManager = notificationUtil.makeNotificationManager(
            id = NOTIFICATION_CHANNEL_ID,
            description = "Schedule post notification",
            name = "Schedule Note"
        )
        coroutineScope.launch {
            runCancellableCatching {
                val draftNote =
                    draftNoteDAO.getDraftNote(accountId = accountId, draftNoteId = draftNoteId)
                draftNote ?: return@launch
                val account = accountRepository.get(accountId).getOrThrow()
                val createNote = draftNote.toCreateNote(account)
                createNoteUseCase.invoke(createNote).getOrThrow()
            }.onFailure {
                Log.e("AlarmPostExecutor", "failed create note", it)
                showCreateNoteFailureNotification(context, notificationManager, draftNoteId)
            }.onSuccess {
                showCreateNoteSuccessNotification(context, notificationManager, draftNoteId, it)
            }

        }

    }

    private fun showCreateNoteSuccessNotification(context: Context,notificationManager: NotificationManager, draftNoteId: Long,  note: Note) {

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_check_black_24dp)
            .setContentTitle(context.getString(R.string.successfully_created_note))
        builder.priority = NotificationCompat.PRIORITY_DEFAULT

        val pendingIntentBuilder = TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(NoteDetailActivity.newIntent(context, note.id))
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntentBuilder
                .getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
        } else {
            pendingIntentBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        builder.setContentIntent(pendingIntent)

        with(notificationManager) {
            notify((draftNoteId / Int.MAX_VALUE).toInt(), builder.build())
        }
    }

    private fun showCreateNoteFailureNotification(context: Context, notificationManager: NotificationManager,  draftNoteId: Long) {
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_close_clear_cancel)
            .setContentTitle(context.getString(R.string.note_creation_failure))
        builder.priority = NotificationCompat.PRIORITY_DEFAULT

        val pendingIntentBuilder = TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(NoteEditorActivity.newBundle(context, draftNoteId = draftNoteId))
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntentBuilder
                .getPendingIntent(0, PendingIntent.FLAG_MUTABLE)
        } else {
            pendingIntentBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        builder.setContentIntent(pendingIntent)
        with(notificationManager) {
            notify((draftNoteId / Int.MAX_VALUE).toInt(), builder.build())
        }

    }
}