package jp.panta.misskeyandroidclient

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.draft.DraftNoteDao
import net.pantasystem.milktea.model.notes.toCreateNote
import net.pantasystem.milktea.model.notes.toNoteEditingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmNotePostReceiver : BroadcastReceiver() {


    @Inject lateinit var draftNoteDAO: net.pantasystem.milktea.model.notes.draft.DraftNoteDao
    @Inject lateinit var accountRepository: net.pantasystem.milktea.model.account.AccountRepository
    @Inject lateinit var coroutineScope: CoroutineScope
    @Inject lateinit var noteRepository: net.pantasystem.milktea.model.notes.NoteRepository
    override fun onReceive(context: Context, intent: Intent) {
        val draftNoteId = intent.getLongExtra("DRAFT_NOTE_ID", -1)
        val accountId = intent.getLongExtra("ACCOUNT_ID", -1)
        require(draftNoteId >= 0)
        require(accountId >= 0)

        coroutineScope.launch {
            val draftNote = draftNoteDAO.getDraftNote(accountId = accountId, draftNoteId = draftNoteId)
            draftNote ?: return@launch
            val account = accountRepository.get(accountId)
            val createNote = draftNote.toNoteEditingState().toCreateNote(account)
            noteRepository.create(createNote)
        }

    }
}