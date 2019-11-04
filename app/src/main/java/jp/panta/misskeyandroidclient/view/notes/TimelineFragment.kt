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
import jp.panta.misskeyandroidclient.viewmodel.notes.TimelineState
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import jp.panta.misskeyandroidclient.viewmodel.notes.TimelineViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.TimelineViewModelFactory
import kotlinx.android.synthetic.main.fragment_swipe_refresh_recycler_view.*

class TimelineFragment : Fragment(R.layout.fragment_swipe_refresh_recycler_view){

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



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mLinearLayoutManager = LinearLayoutManager(this.context!!)
        list_view.layoutManager = mLinearLayoutManager

        //データ受け取り

        mSetting = arguments?.getSerializable(EXTRA_TIMELINE_FRAGMENT_NOTE_REQUEST_SETTING) as NoteRequest.Setting

        val miApplication = context?.applicationContext as MiApplication

        list_view.addOnScrollListener(mScrollListener)
        list_view.layoutManager = mLinearLayoutManager

        miApplication.currentConnectionInstanceLiveData.observe(viewLifecycleOwner, Observer {ci ->
            val factory = TimelineViewModelFactory(ci, mSetting!!, miApplication, true)
            mViewModel = ViewModelProvider(this, factory).get(TimelineViewModel::class.java)

            val notesViewModel = ViewModelProvider(activity!!).get(NotesViewModel::class.java)

            val adapter = TimelineListAdapter(diffUtilCallBack, viewLifecycleOwner, notesViewModel)
            list_view.adapter = adapter

            var  timelineState: TimelineState.State? = null
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

            mViewModel?.isLoading?.observe(viewLifecycleOwner, Observer<Boolean> {
                if(it != null && !it){
                    refresh?.isRefreshing = false
                }
            })
        })

        refresh.setOnRefreshListener {
            mViewModel?.loadNew()
        }


    }

    override fun onResume() {
        super.onResume()

        isShowing = true

        if(isLoadInit){

        }else{
            mViewModel?.loadInit()
            isLoadInit = true
        }

        //(activity as MainActivity).changeTitle(TabFragment.localizationTitle(mSetting!!))
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

            if(firstVisibleItemPosition == 0){
                Log.d("", "先頭")
            }

            if(endVisibleItemPosition == (itemCount - 1)){
                Log.d("", "後ろ")
                mViewModel?.loadOld()

            }

        }
    }

}