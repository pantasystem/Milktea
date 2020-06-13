package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.TaskStackBuilder
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.Page
import jp.panta.misskeyandroidclient.view.notes.ActionNoteHandler
import jp.panta.misskeyandroidclient.view.notes.detail.NoteDetailFragment
import jp.panta.misskeyandroidclient.viewmodel.confirm.ConfirmViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory
import kotlinx.android.synthetic.main.activity_note_detail.*

class NoteDetailActivity : AppCompatActivity() {
    companion object{
        const val EXTRA_NOTE_ID = "jp.panta.misskeyandroidclient.EXTRA_NOTE_ID"

        private const val TAG = "NoteDetailActivity"

        const val EXTRA_IS_MAIN_ACTIVE = "jp.panta.misskeyandroidclient.EXTRA_IS_MAIN_ACTIVE"
    }
    private var mNoteId: String? = null
    private var mIsMainActive: Boolean = true

    private var mParentActivity: Activities? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_note_detail)

        setSupportActionBar(noteDetailToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mParentActivity = intent.getParentActivity()

        val noteId = intent.getStringExtra(EXTRA_NOTE_ID)
            ?: intent.data?.path?.replace("/notes/", "")
        Log.d(TAG, "受け取ったnoteId: $noteId")
        mNoteId = noteId

        mIsMainActive = intent.getBooleanExtra(EXTRA_IS_MAIN_ACTIVE, true)

        val miApplication = applicationContext as MiApplication
        val notesViewModel = ViewModelProvider(this, NotesViewModelFactory(miApplication))[NotesViewModel::class.java]
        ActionNoteHandler(this, notesViewModel, ViewModelProvider(this)[ConfirmViewModel::class.java]).initViewModelListener()
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_base, NoteDetailFragment.newInstance(noteId!!))
        ft.commit()


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.note_detail_menu, menu)
        if(menu != null){
            setMenuTint(menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home ->{
                finishAndGoToMainActivity()
            }
            R.id.nav_add_to_tab ->{
                addToTab()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun finishAndGoToMainActivity(){
        when(mParentActivity){
            Activities.ACTIVITY_OUT_APP -> {
                val upIntent = Intent(this, MainActivity::class.java)
                upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                if(shouldUpRecreateTask(upIntent)){
                    TaskStackBuilder.create(this)
                        .addNextIntentWithParentStack(upIntent)
                        .startActivities()
                    finish()
                }else{
                    navigateUpTo(upIntent)
                }
            }
            Activities.ACTIVITY_IN_APP ->{
                finish()
            }

        }
        if(!mIsMainActive){
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }


    private fun addToTab(){
        val title = getString(R.string.detail)
        (application as MiApplication).addPageInCurrentAccount(
            Page(null, title, show = Page.Show(mNoteId!!), pageNumber = null)
        )
    }
}
