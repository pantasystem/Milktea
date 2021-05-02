package jp.panta.misskeyandroidclient.view.drive

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.DriveActivity
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.drive.folder.FolderViewData
import jp.panta.misskeyandroidclient.viewmodel.drive.folder.FolderViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.folder.FolderViewModelFactory
import kotlinx.android.synthetic.main.fragment_folder.*
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class FolderFragment : Fragment(R.layout.fragment_folder){

    lateinit var mLinearLayoutManager: LinearLayoutManager
    private var mFolderViewModel: FolderViewModel? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mLinearLayoutManager = LinearLayoutManager(context)
        folder_view.layoutManager = mLinearLayoutManager

        val miApplication  = context?.applicationContext as MiApplication

        miApplication.getCurrentAccount().filterNotNull().onEach{ ar ->
            val folderViewModelFactory = FolderViewModelFactory(ar, miApplication, null)
            val folderViewModel = ViewModelProvider( requireActivity(), folderViewModelFactory).get(FolderViewModel::class.java)
            mFolderViewModel = folderViewModel

            val activity = activity
                ?:return@onEach
            val driveViewModelFactory = DriveViewModelFactory(0)
            val driveViewModel = ViewModelProvider(activity, driveViewModelFactory).get(DriveViewModel::class.java)
            driveViewModel.currentDirectory.observe(viewLifecycleOwner, {
                folderViewModel.currentFolder.postValue(it.id)
            })

            val adapter = FolderListAdapter(diffUtilItemCallback, driveViewModel, folderViewModel)
            folder_view.adapter = adapter

            driveViewModel.currentDirectory.observe(viewLifecycleOwner, {
                folderViewModel.currentFolder.postValue(it.id)
            })

            folderViewModel.isRefreshing.observe(viewLifecycleOwner, {
                refresh.isRefreshing = it
            })

            folderViewModel.currentFolder.observe(viewLifecycleOwner, {
                folderViewModel.loadInit()
            })

            folderViewModel.foldersLiveData.observe(viewLifecycleOwner, {
                adapter.submitList(it)
            })

            refresh.setOnRefreshListener {
                folderViewModel.loadInit()
            }

            folderViewModel.error.filterNotNull().onEach {
                Toast.makeText(requireContext(), "$it", Toast.LENGTH_SHORT).show()
            }.launchIn(lifecycleScope)


        }.launchIn(lifecycleScope)

        folder_view.addOnScrollListener(mScrollListener)

    }

    override fun onResume() {
        super.onResume()

        val ac = activity
        if(ac is DriveActivity){
            ac.setCurrentFragment(DriveActivity.Type.FOLDER)
        }
    }
    private val diffUtilItemCallback = object : DiffUtil.ItemCallback<FolderViewData>(){
        override fun areContentsTheSame(oldItem: FolderViewData, newItem: FolderViewData): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: FolderViewData, newItem: FolderViewData): Boolean {
            return oldItem.id == newItem.id
        }
    }

    private val mScrollListener = object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            val firstVisibleItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition()
            val endVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition()
            val itemCount = mLinearLayoutManager.itemCount

            if(firstVisibleItemPosition == 0){
                Log.d("", "先頭")
            }

            if(endVisibleItemPosition == (itemCount - 1)){
                Log.d("", "後ろ")
                //mTimelineViewModel?.getOldTimeline()
                mFolderViewModel?.loadNext()

            }

        }
    }


}