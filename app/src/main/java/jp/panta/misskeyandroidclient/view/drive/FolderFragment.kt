package jp.panta.misskeyandroidclient.view.drive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.composethemeadapter.MdcTheme
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.DriveActivity
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentFolderBinding
import jp.panta.misskeyandroidclient.model.drive.Directory
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.ui.DirectoryListScreen
import jp.panta.misskeyandroidclient.ui.DirectoryListView
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveSelectableMode
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.drive.directory.DirectoryViewData
import jp.panta.misskeyandroidclient.viewmodel.drive.directory.DirectoryViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.directory.DirectoryViewModelFactory

class FolderFragment : Fragment(R.layout.fragment_folder){

    private lateinit var _linearLayoutManager: LinearLayoutManager

    companion object {
        private const val MAX_SIZE = "MAX_SIZE"
        private const val ACCOUNT_ID = "ACCOUNT_ID"
        private const val SELECTED_FILE_IDS = "SELECTED_FILE_IDS"
        fun newInstance(mode: DriveSelectableMode?) : Fragment{
            return FolderFragment().also { fragment ->
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

    private val binding: FragmentFolderBinding by dataBinding()

    private lateinit var _directoryViewModel: DirectoryViewModel
    private lateinit var _driveViewModel: DriveViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        val miCore  = context?.applicationContext as MiCore

        _driveViewModel = ViewModelProvider(requireActivity(), DriveViewModelFactory(mode))[DriveViewModel::class.java]

        _directoryViewModel = ViewModelProvider(this,
            DirectoryViewModelFactory(accountId, miCore,_driveViewModel.driveStore)
        )[DirectoryViewModel::class.java]


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MdcTheme {
                    DirectoryListScreen(_directoryViewModel, _driveViewModel)
                }
            }
        }
    }

    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
        _linearLayoutManager = LinearLayoutManager(context)
        binding.folderView.layoutManager = _linearLayoutManager


        val miCore  = context?.applicationContext as MiCore

        val driveViewModel = ViewModelProvider(requireActivity(), DriveViewModelFactory(mode))[DriveViewModel::class.java]

        val directoryViewModel = ViewModelProvider(this,
            DirectoryViewModelFactory(accountId, miCore,driveViewModel.driveStore)
        )[DirectoryViewModel::class.java]

        val adapter = FolderListAdapter(diffUtilItemCallback, driveViewModel, directoryViewModel)
        binding.folderView.adapter = adapter


        directoryViewModel.isRefreshing.observe(viewLifecycleOwner) {
            binding.refresh.isRefreshing = it
        }


        directoryViewModel.foldersLiveData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        binding.refresh.setOnRefreshListener {
            directoryViewModel.loadInit()
        }

        directoryViewModel.error.filterNotNull().onEach {
            Toast.makeText(requireContext(), "$it", Toast.LENGTH_SHORT).show()
        }.launchIn(lifecycleScope)


        binding.folderView.addOnScrollListener(mScrollListener)

    }*/

    override fun onResume() {
        super.onResume()

        val ac = activity
        if(ac is DriveActivity){
            ac.setCurrentFragment(DriveActivity.Type.FOLDER)
        }
    }
    private val diffUtilItemCallback = object : DiffUtil.ItemCallback<DirectoryViewData>(){
        override fun areContentsTheSame(oldItem: DirectoryViewData, newItem: DirectoryViewData): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: DirectoryViewData, newItem: DirectoryViewData): Boolean {
            return oldItem.id == newItem.id
        }
    }

    /*private val mScrollListener = object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            val firstVisibleItemPosition = _linearLayoutManager.findFirstVisibleItemPosition()
            val endVisibleItemPosition = _linearLayoutManager.findLastVisibleItemPosition()
            val itemCount = _linearLayoutManager.itemCount

            if(firstVisibleItemPosition == 0){
                Log.d("", "先頭")
            }

            if(endVisibleItemPosition == (itemCount - 1)){
                Log.d("", "後ろ")
                //mTimelineViewModel?.getOldTimeline()
                mDirectoryViewModel?.loadNext()

            }

        }
    }*/


}