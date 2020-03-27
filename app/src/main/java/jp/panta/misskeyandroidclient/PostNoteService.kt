package jp.panta.misskeyandroidclient

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.google.gson.GsonBuilder
import jp.panta.misskeyandroidclient.model.drive.OkHttpDriveFileUploader
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.PostNoteTask

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
        val miApplication = applicationContext as MiApplication
        val ci = miApplication.currentAccount.value?.getCurrentConnectionInformation()
        if(ci == null){
            Log.e(tag, "ConnectionInstanceの取得に失敗しました")
            return
        }

        val uploader = OkHttpDriveFileUploader(applicationContext, ci, GsonBuilder().create(), miApplication.getEncryption())
        val createNote = noteTask.execute(uploader)
        if(createNote == null){
            Log.d(tag, "ファイルのアップロードに失敗しました")
            return
        }

        Log.d(tag, "createNote: $createNote")
        val result = miApplication.getMisskeyAPI(ci).create(createNote).execute()

        if(result?.code() in 200 until 300){
            Log.d(tag, "ノートの投稿に成功しました")
        }else{
            Log.d(tag, "ノートの投稿に失敗しました")
        }
    }

}
