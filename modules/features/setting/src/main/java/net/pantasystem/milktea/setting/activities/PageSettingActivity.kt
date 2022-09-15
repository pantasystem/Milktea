package net.pantasystem.milktea.setting.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_navigation.*
import net.pantasystem.milktea.common_navigation.SearchAndSelectUserNavigation.Companion.EXTRA_SELECTED_USER_CHANGED_DIFF
import net.pantasystem.milktea.model.account.page.PageType
import net.pantasystem.milktea.setting.*
import net.pantasystem.milktea.setting.R
import net.pantasystem.milktea.setting.databinding.ActivityPageSettingBinding
import net.pantasystem.milktea.setting.viewmodel.page.PageSettingViewModel
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PageSettingActivity : AppCompatActivity() {

    companion object{
        const val SEARCH_AND_SELECT_USER_RESULT_CODE = 30
        const val SEARCH_AND_SELECT_USER_FOR_GALLERY_CODE = 31
    }

    @Inject
    lateinit var applyTheme: ApplyTheme

    @Inject
    lateinit var searchAndSelectUserNavigation: SearchAndSelectUserNavigation

    @Inject
    lateinit var searchNavigation: SearchNavigation

    @Inject
    lateinit var antennaNavigation: AntennaNavigation

    @Inject
    lateinit var channelNavigation: ChannelNavigation

    @Inject
    lateinit var userListNavigation: UserListNavigation

    private val mPageSettingViewModel: PageSettingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        val binding = DataBindingUtil.setContentView<ActivityPageSettingBinding>(this,
            R.layout.activity_page_setting
        )
        binding.lifecycleOwner = this
        setSupportActionBar(binding.pageSettingToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)



        val touchHelper = ItemTouchHelper(ItemTouchCallback())
        touchHelper.attachToRecyclerView(binding.pagesView)
        binding.pagesView.addItemDecoration(touchHelper)

        val pagesAdapter = PagesAdapter(mPageSettingViewModel)
        binding.pagesView.adapter = pagesAdapter
        binding.pagesView.layoutManager = LinearLayoutManager(this)

        mPageSettingViewModel.selectedPages.observe(this) {
            Log.d("PageSettingActivity", "選択済みページが更新された")
            pagesAdapter.submitList(it)
        }

        binding.addPageButton.setOnClickListener {
            SelectPageToAddDialog().show(supportFragmentManager, "Activity")
        }

        mPageSettingViewModel.pageOnActionEvent.observe(this) {
            PageSettingActionDialog().show(supportFragmentManager, "PSA")
        }

        mPageSettingViewModel.pageOnUpdateEvent.observe(this) {
            EditTabNameDialog().show(supportFragmentManager, "ETD")
        }

        mPageSettingViewModel.pageAddedEvent.observe(this) { pt ->
            when (pt) {
                PageType.SEARCH, PageType.SEARCH_HASH -> startActivity(
                    searchNavigation.newIntent(SearchNavType.SearchScreen())
                )
                PageType.USER -> {
                    val intent =
                        searchAndSelectUserNavigation.newIntent(SearchAndSelectUserNavigationArgs( selectableMaximumSize = 1))
                    startActivityForResult(intent, SEARCH_AND_SELECT_USER_RESULT_CODE)
                }
                PageType.USER_LIST -> startActivity(userListNavigation.newIntent(Unit))
                PageType.DETAIL -> startActivity(searchNavigation.newIntent(SearchNavType.SearchScreen()))
                PageType.ANTENNA -> startActivity(antennaNavigation.newIntent(Unit))
                PageType.USERS_GALLERY_POSTS -> {
                    val intent =
                        searchAndSelectUserNavigation.newIntent(SearchAndSelectUserNavigationArgs( selectableMaximumSize = 1))
                    startActivityForResult(intent, SEARCH_AND_SELECT_USER_FOR_GALLERY_CODE)
                }
                PageType.CHANNEL_TIMELINE -> {
                    val intent = channelNavigation.newIntent(Unit)
                    startActivity(intent)
                }
                else -> {
                    // auto add
                }
            }
        }

    }

    override fun onPause() {
        super.onPause()
        mPageSettingViewModel.save()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    inner class ItemTouchCallback : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.ACTION_STATE_IDLE){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = viewHolder.absoluteAdapterPosition
            val to = target.absoluteAdapterPosition
            val exList = ArrayList(mPageSettingViewModel.selectedPages.value?: emptyList())
            val d = exList.removeAt(from)
            exList.add(to, d)
            mPageSettingViewModel.setList(exList)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == SEARCH_AND_SELECT_USER_RESULT_CODE || requestCode == SEARCH_AND_SELECT_USER_FOR_GALLERY_CODE){
            if(resultCode == RESULT_OK && data != null){
                val changeDiff = data.getSerializableExtra(EXTRA_SELECTED_USER_CHANGED_DIFF) as ChangedDiffResult
                val userId = changeDiff.selected.firstOrNull()?.id
                if(userId != null) {
                    if(resultCode == SEARCH_AND_SELECT_USER_FOR_GALLERY_CODE) {
                        mPageSettingViewModel.addUsersGalleryById(userId)
                    }else{
                        mPageSettingViewModel.addUserPageById(userId)
                    }
                }
            }
        }
    }

}
