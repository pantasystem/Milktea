package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.databinding.ActivitySearchBinding
import jp.panta.misskeyandroidclient.ui.users.ClickableUserListAdapter
import jp.panta.misskeyandroidclient.ui.users.viewmodel.search.SearchUserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_SEARCH_WORD = "jp.panta.misskeyandroidclient.SearchActivity.EXTRA_SEARCH_WORD"
    }

    private var mSearchView: SearchView? = null

    private var mSearchWord: String? = null

    @FlowPreview
    @ExperimentalCoroutinesApi
    private val mSearchUserViewModel: SearchUserViewModel by viewModels()

    private val binding: ActivitySearchBinding by dataBinding()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_search)
        setSupportActionBar(binding.searchToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        mSearchWord = intent.getStringExtra(EXTRA_SEARCH_WORD)

        val usersAdapter = ClickableUserListAdapter(this)
        binding.searchedUsers.adapter = usersAdapter
        binding.searchedUsers.layoutManager = LinearLayoutManager(this)
        mSearchUserViewModel.users.observe(this, {
            usersAdapter.submitList(it)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_search, menu)

        val searchView = menu.findItem(R.id.app_bar_search)?.actionView as SearchView
        mSearchView = searchView

        searchView.isIconifiedByDefault = false
        searchView.setOnQueryTextListener(queryTextListener)
        searchView.isIconified = false
        searchView.setQuery(mSearchWord,false)

        return super.onCreateOptionsMenu(menu)
    }


    @FlowPreview
    @ExperimentalCoroutinesApi
    private val queryTextListener = object : SearchView.OnQueryTextListener{
        override fun onQueryTextChange(newText: String?): Boolean {
            mSearchUserViewModel.userName.value = newText?: ""
            return true
        }

        override fun onQueryTextSubmit(query: String?): Boolean {
            if(!query.isNullOrBlank()){
                //search(query)
                showSearchResult(query)
                return true
            }
            return false
        }
    }

    @ExperimentalCoroutinesApi
    fun showSearchResult(searchWord: String){
        val intent = Intent(this, SearchResultActivity::class.java)
        intent.putExtra(SearchResultActivity.EXTRA_SEARCH_WORLD, searchWord)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
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
