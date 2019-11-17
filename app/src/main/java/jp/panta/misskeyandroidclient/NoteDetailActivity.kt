package jp.panta.misskeyandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData

class NoteDetailActivity : AppCompatActivity() {
    companion object{
        const val EXTRA_NOTE_ID = "jp.panta.misskeyandroidclient.EXTRA_NOTE_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)


    }
}
