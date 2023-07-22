package net.pantasystem.milktea.search

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.lifecycleScope
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_compose.MilkteaStyleConfigApplyAndTheme
import net.pantasystem.milktea.common_navigation.UserDetailNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigationArgs
import net.pantasystem.milktea.model.ap.ApResolver
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.note.NoteDetailActivity
import net.pantasystem.milktea.search.databinding.ActivitySearchBinding
import net.pantasystem.milktea.user.activity.UserDetailActivity
import javax.inject.Inject

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SEARCH_WORD =
            "net.pantasystem.milktea.search.SearchActivity.EXTRA_SEARCH_WORD"
    }

    @Inject
    internal lateinit var applyTheme: ApplyTheme

    @Inject
    internal lateinit var userDetailNavigation: UserDetailNavigation

    @Inject
    internal lateinit var configRepository: LocalConfigRepository

    private var mSearchView: SearchView? = null

    private var mSearchWord: String? = null
    private var mAcct: String? = null

    private val binding: ActivitySearchBinding by dataBinding()

    private val searchViewModel: SearchViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()

        setContentView(R.layout.activity_search)
        setSupportActionBar(binding.searchToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        mSearchWord = intent.getStringExtra(EXTRA_SEARCH_WORD)
        mAcct = intent.getStringExtra(SearchResultViewModel.EXTRA_ACCT)

        findViewById<ComposeView>(R.id.composeBase).setContent {
            MilkteaStyleConfigApplyAndTheme(configRepository = configRepository) {
                val uiState by searchViewModel.uiState.collectAsState()

                SearchSuggestionsLayout(
                    uiState = uiState,
                    onUserSelected = ::showUserDetail,
                    onHashtagSelected = {
                        showSearchResult("#$it")
                    },
                    onSearchHistoryClicked = {
                        showSearchResult(it.keyword)
                    },
                    onDeleteSearchHistory = {
                        searchViewModel.deleteSearchHistory(it)
                    }
                )
            }
        }

        onBackPressedDispatcher.addCallback {
            finish()
            overridePendingTransition(0, 0)
        }

    }

    private fun showUserDetail(user: User) {
        startActivity(UserDetailActivity.newInstance(this, user.id))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_search, menu)

        val searchView = menu.findItem(R.id.app_bar_search)?.actionView as SearchView
        mSearchView = searchView

        searchView.isIconifiedByDefault = false
        searchView.setOnQueryTextListener(queryTextListener)
        searchView.isIconified = false
        searchView.setQuery(mSearchWord, false)

        return super.onCreateOptionsMenu(menu)
    }


    private val queryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextChange(newText: String?): Boolean {
            searchViewModel.onInputKeyword(newText ?: "")
            return true
        }

        override fun onQueryTextSubmit(query: String?): Boolean {
            if (!query.isNullOrBlank()) {
                //search(query)
                showSearchResult(query)
                return true
            }
            return false
        }
    }

    fun showSearchResult(searchWord: String) {
        lifecycleScope.launch {

            when (val result = searchViewModel.onQueryTextSubmit(searchWord)) {
                is SubmitResult.ApResolved -> {
                    when (result.apResolve) {
                        is ApResolver.TypeNote -> {
                            startActivity(
                                NoteDetailActivity.newIntent(
                                    this@SearchActivity,
                                    result.apResolve.note.id
                                ),
                            )
                            finish()
                        }
                        is ApResolver.TypeUser -> {
                            startActivity(
                                userDetailNavigation.newIntent(
                                    UserDetailNavigationArgs.UserId(
                                        result.apResolve.user.id
                                    ),
                                ),
                            )
                            finish()
                        }
                    }
                }
                is SubmitResult.Search -> {
                    val intent = Intent(this@SearchActivity, SearchResultActivity::class.java)
                    intent.putExtra(SearchResultActivity.EXTRA_SEARCH_WORLD, searchWord)
                    intent.putExtra(SearchResultViewModel.EXTRA_ACCT, mAcct)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                }
                SubmitResult.Cancelled -> return@launch

            }

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                overridePendingTransition(0, 0)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
