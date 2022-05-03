package jp.panta.misskeyandroidclient

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import com.google.android.material.composethemeadapter.MdcTheme
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.databinding.ActivitySearchBinding
import jp.panta.misskeyandroidclient.ui.users.SimpleUserListView
import jp.panta.misskeyandroidclient.ui.users.viewmodel.search.SearchUserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.model.user.User

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

        findViewById<ComposeView>(R.id.composeBase).setContent {
            MdcTheme {
                val users by mSearchUserViewModel.users.collectAsState()
                SimpleUserListView(
                    users = users,
                    onSelected = ::showUserDetail,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

    }

    fun showUserDetail(user: User) {
        startActivity(UserDetailActivity.newInstance(this, user.id))
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
