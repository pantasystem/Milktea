package jp.panta.misskeyandroidclient.view.settings.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.*
import jp.panta.misskeyandroidclient.databinding.ActivityPageSettingBinding
import jp.panta.misskeyandroidclient.model.Page
import jp.panta.misskeyandroidclient.model.account.page.PageType
import jp.panta.misskeyandroidclient.view.settings.page.EditTabNameDialog
import jp.panta.misskeyandroidclient.view.settings.page.PageSettingActionDialog
import jp.panta.misskeyandroidclient.view.settings.page.PagesAdapter
import jp.panta.misskeyandroidclient.view.settings.page.SelectPageToAddDialog
import jp.panta.misskeyandroidclient.viewmodel.setting.page.PageSettingViewModel
import jp.panta.misskeyandroidclient.viewmodel.setting.page.PageableTemplate
import jp.panta.misskeyandroidclient.viewmodel.users.selectable.SelectedUserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

class PageSettingActivity : AppCompatActivity() {

    companion object{
        const val SEARCH_AND_SELECT_USER_RESULT_CODE = 30
        const val SEARCH_AND_SELECT_USER_FOR_GALLERY_CODE = 31
    }

    private lateinit var mPageSettingViewModel: PageSettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        val binding = DataBindingUtil.setContentView<ActivityPageSettingBinding>(this, R.layout.activity_page_setting)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.pageSettingToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val miApplication = applicationContext as MiApplication
        mPageSettingViewModel = ViewModelProvider(this, PageSettingViewModel.Factory(miApplication))[PageSettingViewModel::class.java]

        val touchHelper = ItemTouchHelper(ItemTouchCallback())
        touchHelper.attachToRecyclerView(binding.pagesView)
        binding.pagesView.addItemDecoration(touchHelper)

        val pagesAdapter = PagesAdapter(mPageSettingViewModel)
        binding.pagesView.adapter = pagesAdapter
        binding.pagesView.layoutManager = LinearLayoutManager(this)

        mPageSettingViewModel.selectedPages.observe(this, {
            Log.d("PageSettingActivity", "選択済みページが更新された")
            pagesAdapter.submitList(it)
        })

        binding.addPageButton.setOnClickListener {
            SelectPageToAddDialog().show(supportFragmentManager, "Activity")
        }

        mPageSettingViewModel.pageOnActionEvent.observe(this, {
            PageSettingActionDialog().show(supportFragmentManager, "PSA")
        })

        mPageSettingViewModel.pageOnUpdateEvent.observe(this, {
            EditTabNameDialog().show(supportFragmentManager, "ETD")
        })

        mPageSettingViewModel.pageAddedEvent.observe(this, { pt ->
            when(pt){
                PageType.SEARCH, PageType.SEARCH_HASH -> startActivity(Intent(this, SearchActivity::class.java))
                PageType.USER -> {
                    val intent = SearchAndSelectUserActivity.newIntent(this, selectableMaximumSize = 1)
                    startActivityForResult(intent, SEARCH_AND_SELECT_USER_RESULT_CODE)
                }
                PageType.USER_LIST -> startActivity(Intent(this, ListListActivity::class.java))
                PageType.DETAIL -> startActivity(Intent(this, SearchActivity::class.java))
                PageType.ANTENNA -> startActivity(Intent(this, AntennaListActivity::class.java))
                PageType.USERS_GALLERY_POSTS -> {
                    val intent = SearchAndSelectUserActivity.newIntent(this, selectableMaximumSize = 1)
                    startActivityForResult(intent, SEARCH_AND_SELECT_USER_FOR_GALLERY_CODE)
                }
                else ->{
                    // auto add
                }
            }
        })

    }

    override fun onStop() {
        super.onStop()

        mPageSettingViewModel.save()
        //save
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

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == SEARCH_AND_SELECT_USER_RESULT_CODE || requestCode == SEARCH_AND_SELECT_USER_FOR_GALLERY_CODE){
            if(resultCode == RESULT_OK && data != null){
                val changeDiff = data.getSerializableExtra(SearchAndSelectUserActivity.EXTRA_SELECTED_USER_CHANGED_DIFF) as SelectedUserViewModel.ChangedDiffResult
                val userId = changeDiff.selectedUsers.firstOrNull()?.id?.id
                if(userId != null) {
                    if(resultCode == SEARCH_AND_SELECT_USER_FOR_GALLERY_CODE) {
                        mPageSettingViewModel.addUsersGalleryById(userId)
                    }else{
                        mPageSettingViewModel.addUserPageById(userId)
                    }
                }
            }
        }else if(requestCode == SEARCH_AND_SELECT_USER_FOR_GALLERY_CODE) {
            if(resultCode == RESULT_OK && data != null) {

            }
        }
    }

}
