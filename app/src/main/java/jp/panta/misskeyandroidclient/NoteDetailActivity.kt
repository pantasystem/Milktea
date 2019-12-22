package jp.panta.misskeyandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import jp.panta.misskeyandroidclient.view.notes.ActionNoteHandler
import jp.panta.misskeyandroidclient.view.notes.detail.NoteDetailFragment
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import kotlinx.android.synthetic.main.activity_note_detail.*

class NoteDetailActivity : AppCompatActivity() {
    companion object{
        const val EXTRA_NOTE_ID = "jp.panta.misskeyandroidclient.EXTRA_NOTE_ID"

        private const val TAG = "NoteDetailActivity"
    }
    private var mNoteId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_note_detail)

        setSupportActionBar(noteDetailToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val noteId = intent.getStringExtra(EXTRA_NOTE_ID)
        Log.d(TAG, "受け取ったnoteId: $noteId")
        mNoteId = noteId

        val miApplication = applicationContext as MiApplication
        miApplication.currentConnectionInstanceLiveData.observe(this, Observer {ci ->
            val notesViewModel = ViewModelProvider(this, NotesViewModelFactory(ci, miApplication))[NotesViewModel::class.java]
            ActionNoteHandler(this, notesViewModel).initViewModelListener()
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragment_base, NoteDetailFragment.newInstance(noteId))
            ft.commit()
        })


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.note_detail_menu, menu)
        if(menu != null){
            setMenuTint(menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home ->{
                finish()
            }
            R.id.nav_add_to_tab ->{
                addToTab()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun addToTab(){
        (application as MiApplication).addPageToNoteSettings(NoteRequest.Setting(type = NoteType.DETAIL, noteId = mNoteId).apply{
            title = getString(R.string.detail)
        })
    }
}
