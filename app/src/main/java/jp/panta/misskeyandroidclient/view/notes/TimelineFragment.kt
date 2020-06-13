package jp.panta.misskeyandroidclient.view.notes

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.Page
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.util.getPreferenceName
import jp.panta.misskeyandroidclient.view.PageableView
import jp.panta.misskeyandroidclient.view.ScrollableTop
import jp.panta.misskeyandroidclient.viewmodel.notes.*
import kotlinx.android.synthetic.main.fragment_swipe_refresh_recycler_view.*
import java.util.*

class TimelineFragment : Fragment(R.layout.fragment_swipe_refresh_recycler_view), ScrollableTop, PageableView{

    companion object{
        private const val EXTRA_TIMELINE_FRAGMENT_PAGEABLE_TIMELINE = "jp.panta.misskeyandroidclient.view.notes.TimelineFragment.pageable_timeline"

        private const val EXTRA_FIRST_VISIBLE_NOTE_DATE = "jp.panta.misskeyandroidclient.view.notes.TimelineFragment.EXTRA_FIRST_VISIBLE_NOTE_DATE"

        fun newInstance(pageableTimeline: Page.Timeline): TimelineFragment{
            return TimelineFragment().apply{
                arguments = Bundle().apply{
                    this.putSerializable(EXTRA_TIMELINE_FRAGMENT_PAGEABLE_TIMELINE, pageableTimeline)
                }
            }
        }
    }

    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private var mViewModel: TimelineViewModel? = null
    private var mNotesViewModel: NotesViewModel? = null

    //private var isViewCreated: Boolean = false
    //private var isLoadInited: Boolean = false
    private var isLoadInit: Boolean = false

    private var mPageableTimeline: Page.Timeline? = null
    private var isShowing: Boolean = false

    private var mFirstVisibleItemPosition: Int? = null

    private lateinit var sharedPreference: SharedPreferences


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        sharedPreference = requireContext().getSharedPreferences(requireContext().getPreferenceName(), MODE_PRIVATE)
        //sharedPreference = view.context.getSharedPreferences()

        mLinearLayoutManager = LinearLayoutManager(this.requireContext())
        list_view.layoutManager = mLinearLayoutManager

        //データ受け取り

        mPageableTimeline = arguments?.getSerializable(EXTRA_TIMELINE_FRAGMENT_PAGEABLE_TIMELINE) as Page.Timeline?

        val miApplication = context?.applicationContext as MiApplication

        list_view.addOnScrollListener(mScrollListener)
        list_view.layoutManager = mLinearLayoutManager

       /* val firstVisibleNoteDate = savedInstanceState?.let{
            it.getSerializable(EXTRA_FIRST_VISIBLE_NOTE_DATE) as? Date?
        }*/
        val notesViewModelFactory = NotesViewModelFactory(miApplication)
        mNotesViewModel = ViewModelProvider(requireActivity(), notesViewModelFactory).get(NotesViewModel::class.java)

        miApplication.currentAccount.observe(viewLifecycleOwner, Observer { accountRelation ->
            val factory = TimelineViewModelFactory(accountRelation, mPageableTimeline!!, miApplication, SettingStore(requireContext().getSharedPreferences(requireContext().getPreferenceName(), MODE_PRIVATE)))
            val vm = mViewModel
            if(vm == null || vm.accountRelation.getCurrentConnectionInformation() != accountRelation.getCurrentConnectionInformation()){
                Log.d("TimelineFragment", "初期化処理をします: vm is null:${vm == null}, アカウント不一致:${vm?.accountRelation?.account != accountRelation?.account}")
                mViewModel = ViewModelProvider(this, factory).get("$accountRelation",TimelineViewModel::class.java)
                mViewModel?.stop()
                mViewModel?.start()
                mViewModel?.loadInit()


            }

            //mViewModel?.streamingStop()
            //mViewModel?.streamingStart()

            refresh.setOnRefreshListener {
                mViewModel?.loadNew()
            }

            mViewModel?.isLoading?.observe(viewLifecycleOwner, Observer<Boolean> {
                if(it != null && !it){
                    refresh?.isRefreshing = false
                }
            })

            val nvm = mNotesViewModel
            if(nvm != null){

                //mLinearLayoutManager.scrollToPosition(mViewModel?.position?.value?: 0)
                val adapter = TimelineListAdapter(diffUtilCallBack, viewLifecycleOwner, nvm)
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
                    }
                })
            }

        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        mFirstVisibleItemPosition?.let{ firstVisibleItemPosition ->


            val firstVisibleNote = if(firstVisibleItemPosition > 0 && firstVisibleItemPosition <= mViewModel?.getTimelineLiveData()?.value?.notes?.size?: 0){
                try{
                    mViewModel?.getTimelineLiveData()?.value?.notes?.get(firstVisibleItemPosition)
                }catch(t: Throwable){
                    Log.e("TimelineFragment", "先端に表示されているノートを取得しようとしたら失敗した", t)
                    null
                }
            }else{
                null
            }

            if(firstVisibleNote != null){
                outState.putSerializable(EXTRA_FIRST_VISIBLE_NOTE_DATE, firstVisibleNote.note.createdAt)
            }
        }

    }

    override fun onResume() {
        super.onResume()

        isShowing = true

        if(!isLoadInit){
            isLoadInit = true

        }

        //(activity as MainActivity).changeTitle(TabFragment.localizationTitle(mSetting!!))
    }

    override fun onPause() {
        super.onPause()

        isShowing = false
        Log.d("TimelineFragment", "onPause")
    }

    override fun onDestroyView() {
        super.onDestroyView()

        //mViewModel?.streamingStop()
        //mViewModel?.stop()
        Log.d("TimelineFragment", "onDestroyView")
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

            val firstVisibleItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition()
            val endVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition()
            val itemCount = mLinearLayoutManager.itemCount

            mFirstVisibleItemPosition = firstVisibleItemPosition
            mViewModel?.position?.value = firstVisibleItemPosition

            if(firstVisibleItemPosition == 0){
                Log.d("", "先頭")
            }

            if(endVisibleItemPosition == (itemCount - 1)){
                Log.d("", "後ろ")
                mViewModel?.loadOld()

            }

        }
    }

    override fun showTop() {
        if(lifecycle.currentState == Lifecycle.State.RESUMED){
            mLinearLayoutManager.scrollToPosition(0)
        }
    }

}