package jp.panta.misskeyandroidclient.view.notes

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.SecretConstant
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import jp.panta.misskeyandroidclient.viewmodel.notes.TimelineViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.TimelineViewModelFactory
import kotlinx.android.synthetic.main.fragment_swipe_refresh_recycler_view.*

class TimelineFragment : Fragment(){



    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private lateinit var mViewModel: TimelineViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_swipe_refresh_recycler_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //val adapter = TimelineListAdapter(diffUtilCallBack)
        //list_view.adapter = adapter
        mLinearLayoutManager = LinearLayoutManager(this.context!!)
        list_view.layoutManager = mLinearLayoutManager

        val baseTimelineRequest = NoteRequest(i = SecretConstant.i())

        mViewModel = ViewModelProviders.of(this, TimelineViewModelFactory(TimelineViewModel.Type.GLOBAL, baseTimelineRequest)).get(TimelineViewModel::class.java)

        list_view.adapter = TimelineListAdapter(mViewModel.observableTimelineList)
        list_view.addOnScrollListener(mScrollListener)

        refresh.setOnRefreshListener {
            mViewModel.loadNew()
        }

        /*mViewModel.timeline.observe(viewLifecycleOwner, Observer {
            Log.d("", it.toString())
            adapter.submitList(it)
        })*/

        mViewModel.isLoading.observe(viewLifecycleOwner, Observer<Boolean> {
            if(it != null && !it){
                refresh?.isRefreshing = false
            }
        })

        mViewModel.loadInit()

    }




    private val diffUtilCallBack = object : DiffUtil.ItemCallback<PlaneNoteViewData>(){
        override fun areContentsTheSame(p0: PlaneNoteViewData, p1: PlaneNoteViewData): Boolean {
            return p0.id == p1.id
        }

        override fun areItemsTheSame(p0: PlaneNoteViewData, p1: PlaneNoteViewData): Boolean {
            return p0 == p1
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