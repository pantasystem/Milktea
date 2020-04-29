package jp.panta.misskeyandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import jp.panta.misskeyandroidclient.view.notes.ActionNoteHandler
import jp.panta.misskeyandroidclient.view.notes.TimelineFragment
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.notes.TimelineViewModel
import kotlinx.android.synthetic.main.activity_favorite.*

class FavoriteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_favorite)
        setSupportActionBar(favorite_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.favorite)

        val miApplication = applicationContext as MiApplication
        miApplication.currentAccount.observe(this, Observer {ac ->
            val notesViewModel = ViewModelProvider(this, NotesViewModelFactory(ac, miApplication))[NotesViewModel::class.java]
            val fragment = TimelineFragment.newInstance(
                NoteRequest.Setting(
                    type = NoteType.FAVORITE
                )
            )

            val manager = supportFragmentManager.beginTransaction()
            manager.replace(R.id.favorite_fragment_base, fragment)
            manager.commit()

            ActionNoteHandler(this, notesViewModel).initViewModelListener()
        })

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> finish()
        }
        item?: return false
        return super.onOptionsItemSelected(item)
    }
}
