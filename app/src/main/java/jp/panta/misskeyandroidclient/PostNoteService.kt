@file:Suppress("DEPRECATION")

package jp.panta.misskeyandroidclient

import android.app.IntentService
import android.content.Intent
import android.util.Log
import net.pantasystem.milktea.data.infrastructure.notes.impl.PostNoteTask

@Suppress("DEPRECATION")
@Deprecated("MiApplicationへ移植したため非推奨")
class PostNoteService : IntentService("PostNoteService") {

    companion object{
        const val tag = "PostNoteService"
        const val EXTRA_NOTE_TASK = "jp.panta.misskeyandroidclient.EXTRA_NOTE_TASK"
    }

    override fun onHandleIntent(intent: Intent?) {
        val noteTask = intent?.getSerializableExtra(EXTRA_NOTE_TASK) as PostNoteTask?
        if(noteTask == null){
            Log.e(tag, "EXTRA_NOTE_TASKがnullです")
            return
        }
//        val miApplication = applicationContext as MiApplication


//        miApplication.getTaskExecutor().dispatch(noteTask.createNote.task(miApplication.getNoteRepository()))

    }


}
