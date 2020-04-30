package jp.panta.misskeyandroidclient.view.settings.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ActivityPageSettingBinding
import jp.panta.misskeyandroidclient.model.Page
import jp.panta.misskeyandroidclient.setTheme
import jp.panta.misskeyandroidclient.view.settings.page.PageSettingActionDialog
import jp.panta.misskeyandroidclient.view.settings.page.PagesAdapter
import jp.panta.misskeyandroidclient.view.settings.page.SelectPageToAddDialog
import jp.panta.misskeyandroidclient.viewmodel.setting.page.PageSettingViewModel
import jp.panta.misskeyandroidclient.viewmodel.setting.page.PageableTemplate

class PageSettingActivity : AppCompatActivity() {

    private lateinit var mPageSettingViewModel: PageSettingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        val binding = DataBindingUtil.setContentView<ActivityPageSettingBinding>(this, R.layout.activity_page_setting)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.pageSettingToolbar)

        val miApplication = applicationContext as MiApplication
        mPageSettingViewModel = ViewModelProvider(this, PageSettingViewModel.Factory(miApplication))[PageSettingViewModel::class.java]
        mPageSettingViewModel.defaultPages.value = createDefaultPage()

        val touchHelper = ItemTouchHelper(ItemTouchCallback())
        touchHelper.attachToRecyclerView(binding.pagesView)
        binding.pagesView.addItemDecoration(touchHelper)

        val pagesAdapter = PagesAdapter(mPageSettingViewModel)
        binding.pagesView.adapter = pagesAdapter
        binding.pagesView.layoutManager = LinearLayoutManager(this)

        mPageSettingViewModel.selectedPages.observe(this, Observer {
            pagesAdapter.submitList(it)
        })

        binding.addPageButton.setOnClickListener {
            SelectPageToAddDialog().show(supportFragmentManager, "Activity")
        }

        mPageSettingViewModel.pageOnActionEvent.observe(this, Observer {
            PageSettingActionDialog().show(supportFragmentManager, "PSA")
        })

    }

    private fun createDefaultPage(): List<Page>{
        return listOf(
            PageableTemplate.homeTimeline(getString(R.string.home_timeline)),
            PageableTemplate.hybridTimeline(getString(R.string.hybrid_timeline)),
            PageableTemplate.globalTimeline(getString(R.string.global_timeline))
        )
    }

    override fun onStop() {
        super.onStop()

        mPageSettingViewModel.save()
        //save
    }

    inner class ItemTouchCallback : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.ACTION_STATE_IDLE){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = viewHolder.adapterPosition
            val to = target.adapterPosition
            val exList = ArrayList(mPageSettingViewModel.selectedPages.value?: emptyList())
            val d = exList.removeAt(from)
            exList.add(to, d)
            mPageSettingViewModel.setList(exList)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit
    }
}
