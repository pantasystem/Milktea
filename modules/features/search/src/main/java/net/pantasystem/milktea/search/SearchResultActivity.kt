@file:Suppress("DEPRECATION")

package net.pantasystem.milktea.search

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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
import net.pantasystem.milktea.common_android_ui.account.viewmodel.AccountViewModel
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import net.pantasystem.milktea.search.databinding.ActivitySearchResultBinding
import net.pantasystem.milktea.user.search.SearchUserFragment
import javax.inject.Inject

@AndroidEntryPoint
class SearchResultActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_SEARCH_WORLD =
            "net.pantasystem.milktea.search.SearchResultActivity.EXTRA_SEARCH_WORLD"

        private const val SEARCH_NOTES = 0
        private const val SEARCH_USERS = 1
        private const val SEARCH_NOTES_WITH_FILES = 2
    }

    private var mSearchWord: String? = null
    private var mIsTag: Boolean? = null

    private var mAccountRelation: Account? = null
    private val binding: ActivitySearchResultBinding by dataBinding()
    val notesViewModel by viewModels<NotesViewModel>()
    private val accountViewModel: AccountViewModel by viewModels()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        setContentView(R.layout.activity_search_result)
        setSupportActionBar(binding.searchResultToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val keyword: String? = intent.getStringExtra(EXTRA_SEARCH_WORLD)
            ?: intent.data?.getQueryParameter("keyword")

        mSearchWord = keyword

        if (keyword == null) {
            finish()
            return
        }
        supportActionBar?.title = keyword

        val isTag = keyword.startsWith("#")
        mIsTag = isTag

        val pager = PagerAdapter(this, supportFragmentManager, pageableFragmentFactory, keyword)
        binding.searchResultPager.adapter = pager
        binding.searchResultTab.setupWithViewPager(binding.searchResultPager)

        net.pantasystem.milktea.note.view.ActionNoteHandler(
            this,
            notesViewModel,
            ViewModelProvider(this)[ConfirmViewModel::class.java],
            settingStore
        ).initViewModelListener()
        invalidateOptionsMenu()

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

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
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
        val word = mSearchWord ?: return

        val samePage = getSamePage()
        if (samePage == null) {
            val page = if (mIsTag == true) {
                Page(
                    mAccountRelation?.accountId ?: -1,
                    word,
                    0,
                    pageable = Pageable.SearchByTag(
                        tag = word.replace(
                            "#",
                            ""
                        )
                    )
                )
            } else {
                Page(
                    mAccountRelation?.accountId ?: -1,
                    mSearchWord ?: "",
                    -1,
                    pageable = Pageable.Search(word)
                )
            }
            accountViewModel.addPage(
                page
            )
        } else {
            accountViewModel.removePage(samePage)
        }
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

    class PagerAdapter(
        private val context: Context,
        fragmentManager: FragmentManager,
        private val pageableFragmentFactory: PageableFragmentFactory,
        private val keyword: String,
    ) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val isTag = keyword.startsWith("#")

        val pages = ArrayList(listOf(SEARCH_NOTES, SEARCH_USERS)).apply {
            if (isTag) {
                add(SEARCH_NOTES_WITH_FILES)
            }
        }

        override fun getCount(): Int {
            return pages.size
        }

        @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
        override fun getItem(position: Int): Fragment {
            val isTag = keyword.startsWith("#")

            return when (pages[position]) {
                SEARCH_NOTES, SEARCH_NOTES_WITH_FILES -> {
                    val request: Pageable = if (isTag) {
                        if (pages[position] == SEARCH_NOTES) {
                            Pageable.SearchByTag(tag = keyword.replace("#", ""), withFiles = false)
                        } else {
                            Pageable.SearchByTag(tag = keyword.replace("#", ""), withFiles = true)
                        }

                    } else {
                        Pageable.Search(query = keyword)
                    }
                    pageableFragmentFactory.create(request)
                }
                SEARCH_USERS -> {
                    SearchUserFragment.newInstance(keyword)
                }
                else -> {
                    pageableFragmentFactory.create(
                        Pageable.Search(query = keyword)
                    )
                }
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (pages[position]) {
                SEARCH_NOTES -> context.getString(R.string.timeline)
                SEARCH_NOTES_WITH_FILES -> context.getString(R.string.media)
                SEARCH_USERS -> context.getString(R.string.user)
                else -> null
            }
        }
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
                intent
            }
            is SearchNavType.SearchScreen -> {
                val intent = Intent(activity, SearchActivity::class.java)
                if (args.searchWord != null) {
                    intent.putExtra(SearchActivity.EXTRA_SEARCH_WORD, args.searchWord)
                }
                intent
            }
        }

    }
}