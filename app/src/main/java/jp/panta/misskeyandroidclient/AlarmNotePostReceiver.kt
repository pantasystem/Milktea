package jp.panta.misskeyandroidclient

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.pantasystem.milktea.data.infrastructure.notes.draft.db.DraftNoteDao
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.notes.CreateNoteUseCase
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.toCreateNote
import net.pantasystem.milktea.model.notes.toNoteEditingState
import javax.inject.Inject

@AndroidEntryPoint
class AlarmNotePostReceiver : BroadcastReceiver() {


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

    override fun onReceive(context: Context, intent: Intent) {
        val draftNoteId = intent.getLongExtra("DRAFT_NOTE_ID", -1)
        val accountId = intent.getLongExtra("ACCOUNT_ID", -1)
        require(draftNoteId >= 0)
        require(accountId >= 0)

        coroutineScope.launch {
            runCatching {
                val draftNote =
                    draftNoteDAO.getDraftNote(accountId = accountId, draftNoteId = draftNoteId)
                draftNote ?: return@launch
                val account = accountRepository.get(accountId).getOrThrow()
                val createNote = draftNote.toNoteEditingState().toCreateNote(account)
                createNoteUseCase.invoke(createNote)
            }.onFailure {
                Log.e("AlarmPostExecutor", "failed create note", it)
            }

        }

    }
}