package jp.panta.misskeyandroidclient.view.notes

import android.os.Bundle

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.MainActivity
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.SecretConstant
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
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
    private lateinit var mViewModel: TimelineViewModel

    private var isViewCreated: Boolean = false
    private var isLoadInited: Boolean = false

    private var mSetting: NoteRequest.Setting? = null

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

        mSetting = arguments?.getSerializable(EXTRA_TIMELINE_FRAGMENT_NOTE_REQUEST_SETTING) as NoteRequest.Setting?



        //val requestSetting = NoteRequest.Setting(i = SecretConstant.i(), type = NoteType.SOCIAL)

        mViewModel = ViewModelProviders.of(activity!!, TimelineViewModelFactory(mSetting)).get(TimelineViewModel::class.java)

        list_view.adapter = TimelineListAdapter(mViewModel.observableTimelineList)
        list_view.addOnScrollListener(mScrollListener)

        refresh.setOnRefreshListener {
            mViewModel.loadNew()
        }

        mViewModel.isLoading.observe(viewLifecycleOwner, Observer<Boolean> {
            if(it != null && !it){
                refresh?.isRefreshing = false
            }
        })

        if(userVisibleHint && !isLoadInited){
            mViewModel.loadInit()
            isLoadInited = true

            Log.d("", "title変更中")
            (activity as MainActivity).changeTitle(TabFragment.localizationTitle(mSetting!!))


        }
        isViewCreated = true

    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        val setting = mSetting
        if(isVisibleToUser && setting != null){
            //activity?.title = TabFragment.localizationTitle(setting)
            (activity as MainActivity).changeTitle(TabFragment.localizationTitle(setting))
        }
        //表示中
        if(isVisibleToUser && isViewCreated && !isLoadInited){
            mViewModel.loadInit()
            isLoadInited = true
        }
    }

    private val mScrollListener = object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            val firstVisibleItemPosition = mLinearLayoutManager?.findFirstVisibleItemPosition()?: -1
            val endVisibleItemPosition = mLinearLayoutManager?.findLastVisibleItemPosition()?: -1
            val itemCount = mLinearLayoutManager?.itemCount?: -1
            //val childCount = recyclerView.childCount
            //Log.d("", "firstVisibleItem: $firstVisibleItemPosition, itemCount: $itemCount, childCount: $childCount")
            //Log.d("", "first:$firstVisibleItemPosition, end:$endVisibleItemPosition, itemCount:$itemCount")
            if(firstVisibleItemPosition == 0){
                Log.d("", "先頭")
            }

            if(endVisibleItemPosition == (itemCount - 1)){
                Log.d("", "後ろ")
                //mTimelineViewModel?.getOldTimeline()
                mViewModel.loadOld()

            }

        }
    }
}