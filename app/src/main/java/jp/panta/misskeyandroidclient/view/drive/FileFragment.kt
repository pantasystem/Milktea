package jp.panta.misskeyandroidclient.view.drive

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewData
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewModelFactory
import kotlinx.android.synthetic.main.fragment_file.*
import java.lang.IllegalArgumentException

class FileFragment : Fragment(R.layout.fragment_file){

    companion object{
        private const val ARGS_SELECTABLE_MAX_SIZE = "file_fragment_selectable_max_size"
        private const val ARGS_FOLDER_ID = "file_fragment_folder_id"

        fun newInstance(selectableMaxSize: Int, folderId: String? = null): FileFragment{
            val bundle = Bundle().apply{
                require(selectableMaxSize > 0) { "このメソッドを使ってインスタンス化するときは0より大きい数値を指定してください" }
                putInt(ARGS_SELECTABLE_MAX_SIZE, selectableMaxSize)
                putString(ARGS_FOLDER_ID, folderId)
            }
            return FileFragment().apply{
                arguments = bundle
            }

        }

        fun newInstance(folderId: String): FileFragment{
            val bundle = Bundle().apply{
                putString(ARGS_FOLDER_ID, folderId)
            }
            return FileFragment().apply{
                arguments = bundle
            }
        }
    }

    private var mViewModel: FileViewModel? = null
    private lateinit var mLinearLayoutManager: LinearLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val maxSize = arguments?.getInt(ARGS_SELECTABLE_MAX_SIZE)
        val isSelectable = maxSize != null
        val folderId = arguments?.getString(ARGS_FOLDER_ID)

        mLinearLayoutManager = LinearLayoutManager(context)
        files_view.layoutManager = mLinearLayoutManager

        val miApplication = context?.applicationContext as MiApplication
        miApplication.currentConnectionInstanceLiveData.observe(viewLifecycleOwner, Observer {
            val factory  = FileViewModelFactory(it, miApplication, isSelectable = isSelectable, maxSelectableItemSize = maxSize?: 0, folderId = folderId)
            val viewModel  = ViewModelProvider(this, factory).get(FileViewModel::class.java)

            val activity = activity?: return@Observer
            val driveViewModelFactory = DriveViewModelFactory(it, miApplication)
            val driveViewModel = ViewModelProvider(activity, driveViewModelFactory).get(DriveViewModel::class.java)

            driveViewModel.currentDirectory.observe(viewLifecycleOwner, Observer {directory ->
                viewModel.currentFolder.postValue(directory.id)
            })

            mViewModel = viewModel
            viewModel.isRefreshing.observe(viewLifecycleOwner, Observer { isRefreshing ->
                refresh.isRefreshing = isRefreshing
            })

            val adapter = FileListAdapter(fileDiffUtilCallback, viewModel,viewLifecycleOwner)
            files_view.adapter = adapter

            viewModel.filesLiveData.observe(viewLifecycleOwner, Observer {files ->
                adapter.submitList(files)
            })
            viewModel.currentFolder.observe(viewLifecycleOwner, Observer {
                viewModel.loadInit()
            })

            refresh.setOnRefreshListener {
                viewModel.loadInit()
            }
        })
        files_view.addOnScrollListener(mScrollListener)
    }

    private val fileDiffUtilCallback = object : DiffUtil.ItemCallback<FileViewData>(){
        override fun areContentsTheSame(oldItem: FileViewData, newItem: FileViewData): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: FileViewData, newItem: FileViewData): Boolean {
            return oldItem.id == newItem.id
        }
    }

    private val mScrollListener = object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            val firstVisibleItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition()?: -1
            val endVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition()?: -1
            val itemCount = mLinearLayoutManager.itemCount?: -1

            if(firstVisibleItemPosition == 0){
                Log.d("", "先頭")
            }

            if(endVisibleItemPosition == (itemCount - 1)){
                Log.d("", "後ろ")
                //mTimelineViewModel?.getOldTimeline()
                mViewModel?.loadNext()

            }

        }
    }
}