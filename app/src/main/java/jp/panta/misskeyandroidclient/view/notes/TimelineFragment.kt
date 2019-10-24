package jp.panta.misskeyandroidclient.view.notes

import android.os.Bundle

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.MainActivity
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.streming.NoteCapture
import jp.panta.misskeyandroidclient.model.streming.TimelineCapture
import jp.panta.misskeyandroidclient.viewmodel.TimelineState
import jp.panta.misskeyandroidclient.viewmodel.main.MainViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import jp.panta.misskeyandroidclient.viewmodel.notes.TimelineViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.TimelineViewModelFactory
import kotlinx.android.synthetic.main.fragment_swipe_refresh_recycler_view.*

class TimelineFragment : Fragment(){

    companion object{
        private const val EXTRA_TIMELINE_FRAGMENT_NOTE_REQUEST_SETTING = "jp.panta.misskeyandroidclient.view.notes.TimelineFragment.setting"
        fun newInstance(setting: NoteRequest.Setting): TimelineFragment{
            return TimelineFragment().apply{
                arguments = Bundle().apply{
                    this.putSerializable(EXTRA_TIMELINE_FRAGMENT_NOTE_REQUEST_SETTING, setting)
                }
            }
        }
    }

    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private var mViewModel: TimelineViewModel? = null

    //private var isViewCreated: Boolean = false
    //private var isLoadInited: Boolean = false
    private var isLoadInit: Boolean = false

    private var mSetting: NoteRequest.Setting? = null
    private var isShowing: Boolean = false

    private var mFirstVisibleItemPosition: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_swipe_refresh_recycler_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //val adapter = TimelineListAdapter(diffUtilCallBack)
        //list_view.adapter = adapter
        mLinearLayoutManager = LinearLayoutManager(this.context!!)
        list_view.layoutManager = mLinearLayoutManager

        //データ受け取り

        mSetting = arguments?.getSerializable(EXTRA_TIMELINE_FRAGMENT_NOTE_REQUEST_SETTING) as NoteRequest.Setting

        val miApplication = context?.applicationContext as MiApplication
        val nowConnectionInstance = miApplication.currentConnectionInstanceLiveData.value

        if(nowConnectionInstance != null){
            initTimeline(nowConnectionInstance, miApplication, true)

        }

        miApplication.currentConnectionInstanceLiveData.observe(viewLifecycleOwner, Observer {
            if(mViewModel != null){
                initTimeline(it, miApplication, true)
            }
        })

        initViewModelListener()
    }

    private fun initTimeline(nowConnectionInstance: ConnectionInstance, miApplication: MiApplication, isAutoLoad: Boolean){
        val a = TimelineViewModelFactory(nowConnectionInstance, mSetting!!, miApplication, isAutoLoad)
        Log.d("TimelineFragment", "setting: $mSetting")
        val store = activity?.viewModelStore
        if(store == null){
            Log.e("TimelineFragment", "activity#viewModelStore is null")
            return
        }
        mViewModel = ViewModelProvider(store, a).get(mSetting?.toString()!!,TimelineViewModel::class.java)

        val adapter = TimelineListAdapter(diffUtilCallBack, viewLifecycleOwner, mViewModel!!)
        list_view.adapter = adapter
        list_view.addOnScrollListener(mScrollListener)

        var timelineState: TimelineState.State? = null
        mViewModel?.getTimelineLiveData()?.observe(viewLifecycleOwner, Observer {

            adapter.submitList(it.notes)
            timelineState = it.state

        })

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver(){

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                if(timelineState == TimelineState.State.RECEIVED_NEW && positionStart == 0 && mFirstVisibleItemPosition == 0 && isShowing && itemCount == 1){
                    mLinearLayoutManager.scrollToPosition(0)
                }
                //Log.d("onItemRangeInserted", "positionStart: $positionStart")
            }
        })



        refresh.setOnRefreshListener {
            mViewModel?.loadNew()
        }

        mViewModel?.isLoading?.observe(viewLifecycleOwner, Observer<Boolean> {
            if(it != null && !it){
                refresh?.isRefreshing = false
            }
        })
    }

    private fun initViewModelListener(){
        mViewModel?.replyTarget?.observe(viewLifecycleOwner, Observer{
            Log.d("TimelineFragment", "reply clicked :$it")
        })

        mViewModel?.reNoteTarget?.observe(viewLifecycleOwner, Observer{
            Log.d("TimelineFragment", "renote clicked :$it")
            val dialog = RenoteBottomSheetDialog()
            val ft = activity?.supportFragmentManager
            if(ft != null){
                dialog.show(ft, "timelineFragment")
            }
        })

        mViewModel?.shareTarget?.observe(viewLifecycleOwner, Observer{
            Log.d("TimelineFragment", "share clicked :$it")
        })

        mViewModel?.targetUser?.observe(viewLifecycleOwner, Observer{
            Log.d("TimelineFragment", "user clicked :$it")
        })
    }

    override fun onResume() {
        super.onResume()

        isShowing = true

        if(isLoadInit){

        }else{
            mViewModel?.loadInit()
            isLoadInit = true
        }

        (activity as MainActivity).changeTitle(TabFragment.localizationTitle(mSetting!!))
    }

    override fun onPause() {
        super.onPause()

        isShowing = false
        Log.d("TimelineFragment", "onPause")
    }


    private val diffUtilCallBack = object : DiffUtil.ItemCallback<PlaneNoteViewData>(){
        override fun areContentsTheSame(
            oldItem: PlaneNoteViewData,
            newItem: PlaneNoteViewData
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areItemsTheSame(
            oldItem: PlaneNoteViewData,
            newItem: PlaneNoteViewData
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }

    private val mScrollListener = object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            val firstVisibleItemPosition = mLinearLayoutManager?.findFirstVisibleItemPosition()?: -1
            val endVisibleItemPosition = mLinearLayoutManager?.findLastVisibleItemPosition()?: -1
            val itemCount = mLinearLayoutManager?.itemCount?: -1

            mFirstVisibleItemPosition = firstVisibleItemPosition
            //val childCount = recyclerView.childCount
            //Log.d("", "firstVisibleItem: $firstVisibleItemPosition, itemCount: $itemCount, childCount: $childCount")
            //Log.d("", "first:$firstVisibleItemPosition, end:$endVisibleItemPosition, itemCount:$itemCount")
            if(firstVisibleItemPosition == 0){
                Log.d("", "先頭")
            }

            if(endVisibleItemPosition == (itemCount - 1)){
                Log.d("", "後ろ")
                //mTimelineViewModel?.getOldTimeline()
                mViewModel?.loadOld()

            }

        }
    }

}