package jp.panta.misskeyandroidclient.view.drive

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.DriveActivity
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentFileBinding
import jp.panta.misskeyandroidclient.model.drive.DriveStore
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.util.PageableState
import jp.panta.misskeyandroidclient.util.StateContent
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveSelectableMode
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewData
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class FileFragment : Fragment(R.layout.fragment_file){

    companion object{

        private const val MAX_SIZE = "MAX_SIZE"
        private const val ACCOUNT_ID = "ACCOUNT_ID"
        private const val SELECTED_FILE_IDS = "SELECTED_FILE_IDS"
        fun newInstance(mode: DriveSelectableMode?) : Fragment{
            return FileFragment().also { fragment ->
                fragment.arguments = Bundle().also { bundle ->
                    bundle.putInt(MAX_SIZE, mode?.selectableMaxSize?: -1)
                    bundle.putSerializable(SELECTED_FILE_IDS, mode?.let {
                        ArrayList(it.selectedFilePropertyIds)
                    })
                    bundle.putLong(ACCOUNT_ID, mode?.accountId?: mode?.selectedFilePropertyIds?.map {
                        it.accountId
                    }?.toSet()?.lastOrNull()?: -1)
                }

            }
        }
    }

    private val binding: FragmentFileBinding by dataBinding()
    @ExperimentalCoroutinesApi
    private lateinit var _viewModel: FileViewModel
    private lateinit var mLinearLayoutManager: LinearLayoutManager

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val maxSize = arguments?.getInt(MAX_SIZE, -1)?.let {
            if(it == -1) null else it
        }
        val selectedFileIds = (arguments?.getSerializable(SELECTED_FILE_IDS) as? ArrayList<*>)?.let { list ->
            list.map { it as FileProperty.Id }
        }
        val accountId = arguments?.getLong(ACCOUNT_ID, - 1)?.let {
            if(it == -1L) null else it
        }

        val mode = if(maxSize == null || selectedFileIds.isNullOrEmpty() || accountId == null) {
            null
        }else{
            DriveSelectableMode(
                accountId = accountId,
                selectedFilePropertyIds = selectedFileIds,
                selectableMaxSize = maxSize,
            )
        }

        mLinearLayoutManager = LinearLayoutManager(context)
        binding.filesView.layoutManager = mLinearLayoutManager

        val miApplication = context?.applicationContext as MiApplication

        val driveViewModel = ViewModelProvider(requireActivity(), DriveViewModelFactory(mode))[DriveViewModel::class.java]
        _viewModel = ViewModelProvider(requireActivity(), FileViewModelFactory(accountId, miApplication, driveViewModel.driveStore))[FileViewModel::class.java]
        val adapter = FileListAdapter(fileDiffUtilCallback, _viewModel,driveViewModel, viewLifecycleOwner)
        binding.filesView.adapter = adapter

        _viewModel.state.onEach {
            binding.refresh.isRefreshing = it is PageableState.Loading
            val list = if(it.content is StateContent.Exist) it.content.rawContent else emptyList()
            adapter.submitList(list)
            if(it is PageableState.Error) {
                Log.w("FileFragment", "読み込みエラー", it.throwable)
            }

        }.catch {

        }.launchIn(lifecycleScope)

        binding.refresh.setOnRefreshListener {
            _viewModel.loadInit()
        }

        binding.filesView.addOnScrollListener(mScrollListener)
    }

    override fun onResume() {
        super.onResume()

        val a = activity
        if(a is DriveActivity){
            a.setCurrentFragment(DriveActivity.Type.FILE)
        }

    }
    private val fileDiffUtilCallback = object : DiffUtil.ItemCallback<FileViewData>(){
        override fun areContentsTheSame(oldItem: FileViewData, newItem: FileViewData): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: FileViewData, newItem: FileViewData): Boolean {
            return oldItem.fileProperty.id == newItem.fileProperty.id
        }
    }

    @ExperimentalCoroutinesApi
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
                _viewModel.loadNext()

            }

        }
    }
}