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
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewData
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewModelFactory
import kotlinx.android.synthetic.main.fragment_file.*

class FileFragment : Fragment(R.layout.fragment_file){

    companion object{
    }

    private var mViewModel: FileViewModel? = null
    private lateinit var mLinearLayoutManager: LinearLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mLinearLayoutManager = LinearLayoutManager(context)
        files_view.layoutManager = mLinearLayoutManager

        val miApplication = context?.applicationContext as MiApplication
        miApplication.currentConnectionInstanceLiveData.observe(viewLifecycleOwner, Observer {
            val factory  = FileViewModelFactory(it, miApplication)
            val viewModel  = ViewModelProvider(this, factory).get(FileViewModel::class.java)
            mViewModel = viewModel
            viewModel.isRefreshing.observe(viewLifecycleOwner, Observer { isRefreshing ->
                refresh.isRefreshing = isRefreshing
            })

            val adapter = FileListAdapter(fileDiffUtilCallback, viewModel)
            files_view.adapter = adapter

            viewModel.filesLiveData.observe(viewLifecycleOwner, Observer {files ->
                adapter.submitList(files)
            })
            viewModel.loadInit()
            refresh.setOnRefreshListener {
                viewModel.loadInit()
            }
        })
        files_view.addOnScrollListener(mScrollListener)
    }

    private val fileDiffUtilCallback = object : DiffUtil.ItemCallback<FileViewData>(){
        override fun areContentsTheSame(oldItem: FileViewData, newItem: FileViewData): Boolean {
            return oldItem.name == newItem.name
                    && oldItem.id == newItem.id
                    && oldItem.md5 == newItem.md5
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