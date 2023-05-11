package net.pantasystem.milktea.search

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayoutMediator
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.ui.ApplyMenuTint
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.common_navigation.SearchNavType
import net.pantasystem.milktea.common_navigation.SearchNavigation
import net.pantasystem.milktea.common_viewmodel.confirm.ConfirmViewModel
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import net.pantasystem.milktea.search.databinding.ActivitySearchResultBinding
import javax.inject.Inject

@AndroidEntryPoint
class SearchResultActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_SEARCH_WORLD =
            "net.pantasystem.milktea.search.SearchResultActivity.EXTRA_SEARCH_WORLD"
    }

    private var mSearchWord: String? = null
    private var mIsTag: Boolean? = null

    private var mAccountRelation: Account? = null
    private val binding: ActivitySearchResultBinding by dataBinding()
    private val notesViewModel by viewModels<NotesViewModel>()

    @Inject
    lateinit var settingStore: SettingStore

    @Inject
    lateinit var accountStore: AccountStore

    @Inject
    lateinit var pageableFragmentFactory: PageableFragmentFactory

    @Inject
    internal lateinit var applyTheme: ApplyTheme

    @Inject
    internal lateinit var applyMenuTint: ApplyMenuTint

    private val searchResultViewModel: SearchResultViewModel by viewModels()

    private var tabLayoutMediator: TabLayoutMediator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        setContentView(R.layout.activity_search_result)
        setSupportActionBar(binding.searchResultToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val keyword: String? = intent.getStringExtra(EXTRA_SEARCH_WORLD)
            ?: intent.data?.getQueryParameter("keyword")

        searchResultViewModel.setKeyword(keyword ?: "")

        mSearchWord = keyword

        if (keyword == null) {
            finish()
            return
        }
        supportActionBar?.title = keyword

        val isTag = keyword.startsWith("#")
        mIsTag = isTag

        val pager = SearchResultViewPagerAdapter(this, pageableFragmentFactory)
        binding.searchResultPager.adapter = pager
        tabLayoutMediator = TabLayoutMediator(
            binding.searchResultTab,
            binding.searchResultPager,
        ) { tab, position ->
            tab.text = pager.items[position].title.getString(this)
        }
        tabLayoutMediator?.attach()



        net.pantasystem.milktea.note.view.ActionNoteHandler(
            this,
            notesViewModel,
            ViewModelProvider(this)[ConfirmViewModel::class.java],
            settingStore
        ).initViewModelListener()
        invalidateOptionsMenu()

        searchResultViewModel.uiState.onEach {
            pager.submitList(it.tabItems)
        }.flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED).launchIn(lifecycleScope)

        accountStore.observeCurrentAccount.onEach { ar ->
            mAccountRelation = ar
        }.launchIn(lifecycleScope)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_search_menu, menu)
        val item = menu.findItem(R.id.nav_search_add_to_tab)

        if (isAddedPage()) {
            item.setIcon(R.drawable.ic_remove_to_tab_24px)
        } else {
            item.setIcon(R.drawable.ic_add_to_tab_24px)
        }
        applyMenuTint(this, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.search -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(SearchActivity.EXTRA_SEARCH_WORD, mSearchWord)
                startActivity(intent)
            }
            R.id.nav_search_add_to_tab -> {
                searchAddToTab()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun searchAddToTab() {
        searchResultViewModel.toggleAddToTab()
    }

    private fun isAddedPage(): Boolean {
        return getSamePage() != null
    }

    private fun getSamePage(): Page? {
        return mAccountRelation?.pages?.firstOrNull {
            when (val pageable = it.pageable()) {
                is Pageable.Search -> {
                    pageable.query == mSearchWord
                }
                is Pageable.SearchByTag -> {
                    pageable.tag == mSearchWord
                }
                else -> {
                    false
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        tabLayoutMediator?.detach()
        tabLayoutMediator = null
    }


}

class SearchNavigationImpl  @Inject constructor(
    val activity: Activity
): SearchNavigation {
    override fun newIntent(args: SearchNavType): Intent {
        return when(args) {
            is SearchNavType.ResultScreen -> {
                val intent = Intent(activity, SearchResultActivity::class.java)
                intent.putExtra(SearchResultActivity.EXTRA_SEARCH_WORLD, args.searchWord)
                if (args.acct != null) {
                    intent.putExtra(SearchResultViewModel.EXTRA_ACCT, args.acct)
                }
                intent
            }
            is SearchNavType.SearchScreen -> {
                val intent = Intent(activity, SearchActivity::class.java)
                if (args.searchWord != null) {
                    intent.putExtra(SearchActivity.EXTRA_SEARCH_WORD, args.searchWord)
                }
                if (args.acct != null) {
                    intent.putExtra(SearchResultViewModel.EXTRA_ACCT, args.acct)
                }
                intent
            }
        }

    }
}