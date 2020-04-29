package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import jp.panta.misskeyandroidclient.view.SafeUnbox
import jp.panta.misskeyandroidclient.view.notes.ActionNoteHandler
import jp.panta.misskeyandroidclient.view.notes.TimelineFragment
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory
import kotlinx.android.synthetic.main.activity_search_result.*

class SearchResultActivity : AppCompatActivity() {
    companion object{
        const val EXTRA_SEARCH_WORLD = "jp.panta.misskeyandroidclient.SearchResultActivity.EXTRA_SEARCH_WORLD"
    }

    private var mSearchWord: String? = null
    private var mIsTag: Boolean? = null

    private var mAccountRelation: AccountRelation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_search_result)
        setSupportActionBar(search_result_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val keyword: String? = intent.getStringExtra(EXTRA_SEARCH_WORLD)
            ?: intent.data?.getQueryParameter("keyword")

        mSearchWord = keyword

        if(keyword == null){
            finish()
            return
        }
        supportActionBar?.title = keyword

        val isTag = keyword.startsWith("#")
        mIsTag = isTag
        val request = if(isTag){
            NoteRequest.Setting(
                type = NoteType.SEARCH_HASH,
                tag = keyword
            )
        }else{
            NoteRequest.Setting(
                type = NoteType.SEARCH,
                query = keyword
            )
        }


        val timelineFragment = TimelineFragment.newInstance(request)
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.search_result_base, timelineFragment)
        ft.commit()

        (application as MiCore).currentAccount.observe(this, Observer { ar ->
            val notesViewModel = ViewModelProvider(this, NotesViewModelFactory(ar, application as MiApplication))[NotesViewModel::class.java]
            ActionNoteHandler(this, notesViewModel).initViewModelListener()
            mAccountRelation = ar
            invalidateOptionsMenu()
        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_search_menu, menu)
        if(menu != null){
            val item = menu.findItem(R.id.nav_search_add_to_tab)

            if(isAddedPage()){
                item.setIcon(R.drawable.ic_remove_to_tab_24px)
            }else{
                item.setIcon(R.drawable.ic_add_to_tab_24px)
            }
            setMenuTint(menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
            R.id.search -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(SearchActivity.EXTRA_SEARCH_WORD, mSearchWord)
                startActivity(intent)
            }
            R.id.nav_search_add_to_tab ->{
                searchAddToTab()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun searchAddToTab(){
        val type = getType()
        val word = mSearchWord ?: return

        val miCore = application as MiCore
        val page = getSamePage()
        if(page == null){
            miCore.addPageInCurrentAccount(
                NoteRequest.Setting(type = type).apply{
                    title = word
                }
            )
        }else{
            miCore.removePageInCurrentAccount(page)
        }
    }

    private fun isAddedPage(): Boolean{
        return getSamePage() != null
    }

    private fun getSamePage(): NoteRequest.Setting?{
        return mAccountRelation?.pages?.firstOrNull {
            it.type == getType() && (it.query == mSearchWord || it.tag == mSearchWord || it.title == mSearchWord)
        }
    }

    private fun getType(): NoteType{
        return if(SafeUnbox.unbox(mIsTag)){
            NoteType.SEARCH_HASH
        }else{
            NoteType.SEARCH
        }
    }
}
