package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_SEARCH_WORD = "jp.panta.misskeyandroidclient.SearchActivity.EXTRA_SEARCH_WORD"
    }

    private var mSearchView: SearchView? = null

    private var mSearchWord: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_search)
        setSupportActionBar(search_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        mSearchWord = intent.getStringExtra(EXTRA_SEARCH_WORD)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_search, menu)

        val searchView = menu?.findItem(R.id.app_bar_search)?.actionView as SearchView
        mSearchView = searchView

        searchView.setIconifiedByDefault(false)
        searchView.setOnQueryTextListener(queryTextListener)
        searchView.isIconified = false
        searchView.setQuery(mSearchWord,false)

        return super.onCreateOptionsMenu(menu)
    }


    private val queryTextListener = object : SearchView.OnQueryTextListener{
        override fun onQueryTextChange(newText: String?): Boolean {
            return false
        }

        override fun onQueryTextSubmit(query: String?): Boolean {
            if(!query.isNullOrBlank()){
                //search(query)
                showSearchResult(query)
            }
            return false
        }
    }

    fun showSearchResult(searchWord: String){
        val intent = Intent(this, SearchResultActivity::class.java)
        intent.putExtra(SearchResultActivity.EXTRA_SEARCH_WORLD, searchWord)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home ->{
                finish()
                overridePendingTransition(0, 0)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        finish()
        overridePendingTransition(0, 0)
    }
}
