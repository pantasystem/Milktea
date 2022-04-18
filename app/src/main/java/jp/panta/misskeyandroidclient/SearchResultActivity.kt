@file:Suppress("DEPRECATION")
package jp.panta.misskeyandroidclient

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.databinding.ActivitySearchResultBinding
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.account.Account
import jp.panta.misskeyandroidclient.ui.account.viewmodel.AccountViewModel
import jp.panta.misskeyandroidclient.ui.notes.view.ActionNoteHandler
import jp.panta.misskeyandroidclient.ui.notes.view.TimelineFragment
import jp.panta.misskeyandroidclient.ui.users.SearchUserFragment
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.confirm.ConfirmViewModel
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.NotesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@AndroidEntryPoint
class SearchResultActivity : AppCompatActivity() {
    companion object{
        const val EXTRA_SEARCH_WORLD = "jp.panta.misskeyandroidclient.SearchResultActivity.EXTRA_SEARCH_WORLD"

        private const val SEARCH_NOTES = 0
        private const val SEARCH_USERS = 1
        private const val SEARCH_NOTES_WITH_FILES = 2
    }

    private var mSearchWord: String? = null
    private var mIsTag: Boolean? = null

    private var mAccountRelation: net.pantasystem.milktea.model.account.Account? = null
    private val binding: ActivitySearchResultBinding by dataBinding()
    val notesViewModel by viewModels<NotesViewModel>()
    private val accountViewModel: AccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_search_result)
        setSupportActionBar(binding.searchResultToolbar)
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

        val pager = PagerAdapter(this, keyword, supportFragmentManager)
        binding.searchResultPager.adapter = pager
        binding.searchResultTab.setupWithViewPager(binding.searchResultPager)

        ActionNoteHandler(this, notesViewModel, ViewModelProvider(this)[ConfirmViewModel::class.java]).initViewModelListener()
        invalidateOptionsMenu()

        (application as MiCore).getAccountStore().observeCurrentAccount.onEach { ar ->
            mAccountRelation = ar
        }.launchIn(lifecycleScope)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_search_menu, menu)
        val item = menu.findItem(R.id.nav_search_add_to_tab)

        if(isAddedPage()){
            item.setIcon(R.drawable.ic_remove_to_tab_24px)
        }else{
            item.setIcon(R.drawable.ic_add_to_tab_24px)
        }
        setMenuTint(menu)
        return super.onCreateOptionsMenu(menu)
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
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
        val word = mSearchWord ?: return

        val samePage = getSamePage()
        if(samePage == null){
            val page = if(mIsTag == true){
                net.pantasystem.milktea.model.account.page.Page(
                    mAccountRelation?.accountId ?: -1,
                    word,
                    0,
                    pageable = net.pantasystem.milktea.model.account.page.Pageable.SearchByTag(
                        tag = word.replace(
                            "#",
                            ""
                        )
                    )
                )
            }else{
                net.pantasystem.milktea.model.account.page.Page(
                    mAccountRelation?.accountId ?: -1,
                    mSearchWord ?: "",
                    -1,
                    pageable = net.pantasystem.milktea.model.account.page.Pageable.Search(word)
                )
            }
            accountViewModel.addPage(
                page
            )
        }else{
            accountViewModel.removePage(samePage)
        }
    }

    private fun isAddedPage(): Boolean{
        return getSamePage() != null
    }

    private fun getSamePage(): net.pantasystem.milktea.model.account.page.Page?{
        return mAccountRelation?.pages?.firstOrNull {
            when (val pageable = it.pageable()) {
                is net.pantasystem.milktea.model.account.page.Pageable.Search -> {
                    pageable.query == mSearchWord
                }
                is net.pantasystem.milktea.model.account.page.Pageable.SearchByTag -> {
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
        private val keyword: String,
        fragmentManager: FragmentManager
    ) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

        private val isTag = keyword.startsWith("#")

        val pages = ArrayList(listOf(SEARCH_NOTES, SEARCH_USERS)).apply{
            if(isTag){
                add(SEARCH_NOTES_WITH_FILES)
            }
        }
        override fun getCount(): Int {
            return pages.size
        }

        @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
        override fun getItem(position: Int): Fragment {
            val isTag = keyword.startsWith("#")

            return when(pages[position]){
                SEARCH_NOTES, SEARCH_NOTES_WITH_FILES ->{
                    val request: net.pantasystem.milktea.model.account.page.Pageable = if(isTag){
                        if(pages[position] == SEARCH_NOTES){
                            net.pantasystem.milktea.model.account.page.Pageable.SearchByTag(tag = keyword.replace("#", ""), withFiles = false)
                        }else{
                            net.pantasystem.milktea.model.account.page.Pageable.SearchByTag(tag = keyword.replace("#", ""), withFiles = true)
                        }

                    }else{
                        net.pantasystem.milktea.model.account.page.Pageable.Search(query = keyword)
                    }
                    TimelineFragment.newInstance(request)
                }
                SEARCH_USERS ->{
                    SearchUserFragment.newInstance(keyword)
                }
                else ->{
                    TimelineFragment.newInstance(
                        net.pantasystem.milktea.model.account.page.Pageable.Search(query = keyword)
                    )
                }
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when(pages[position]){
                SEARCH_NOTES -> context.getString(R.string.timeline)
                SEARCH_NOTES_WITH_FILES -> context.getString(R.string.media)
                SEARCH_USERS -> context.getString(R.string.user)
                else -> null
            }
        }
    }


}
