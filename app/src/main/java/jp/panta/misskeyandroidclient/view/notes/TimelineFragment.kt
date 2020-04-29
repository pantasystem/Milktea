package jp.panta.misskeyandroidclient.view.notes

import android.content.Context
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
import jp.panta.misskeyandroidclient.KeyStore
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.util.getPreferenceName
import jp.panta.misskeyandroidclient.view.ScrollableTop
import jp.panta.misskeyandroidclient.viewmodel.notes.*
import kotlinx.android.synthetic.main.fragment_swipe_refresh_recycler_view.*

class TimelineFragment : Fragment(R.layout.fragment_swipe_refresh_recycler_view), ScrollableTop{

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
    private var mNotesViewModel: NotesViewModel? = null

    //private var isViewCreated: Boolean = false
    //private var isLoadInited: Boolean = false
    private var isLoadInit: Boolean = false

    private var mSetting: NoteRequest.Setting? = null
    private var isShowing: Boolean = false

    private var mFirstVisibleItemPosition: Int? = null

    private lateinit var sharedPreference: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreference = view.context.getSharedPreferences(view.context.getPreferenceName(), MODE_PRIVATE)
        //sharedPreference = view.context.getSharedPreferences()

        mLinearLayoutManager = LinearLayoutManager(this.requireContext())
        list_view.layoutManager = mLinearLayoutManager

        //データ受け取り

        mSetting = arguments?.getSerializable(EXTRA_TIMELINE_FRAGMENT_NOTE_REQUEST_SETTING) as NoteRequest.Setting

        val miApplication = context?.applicationContext as MiApplication

        list_view.addOnScrollListener(mScrollListener)
        list_view.layoutManager = mLinearLayoutManager

        miApplication.currentAccount.observe(viewLifecycleOwner, Observer { accountRelation ->
            val factory = TimelineViewModelFactory(accountRelation, mSetting!!, miApplication, SettingStore(requireContext().getSharedPreferences(requireContext().getPreferenceName(), MODE_PRIVATE)))
            val vm = mViewModel
            if(vm == null || vm.accountRelation.getCurrentConnectionInformation() != accountRelation.getCurrentConnectionInformation()){
                //Log.d("TimelineFragment", "初期化処理をします: vm is null:${vm == null}, CI非一致:${vm?.connectionInstance != ci}")
                mViewModel = ViewModelProvider(this, factory).get("$accountRelation",TimelineViewModel::class.java)
                mViewModel?.loadInit()

                val notesViewModelFactory = NotesViewModelFactory(accountRelation, miApplication)
                mNotesViewModel = ViewModelProvider(requireActivity(), notesViewModelFactory).get(NotesViewModel::class.java)
                mNotesViewModel?.accountRelation = accountRelation

            }

            //mViewModel?.streamingStop()
            //mViewModel?.streamingStart()
            mViewModel?.stop()
            mViewModel?.start()

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

                mLinearLayoutManager.scrollToPosition(mViewModel?.position?.value?: 0)
                val adapter = list_view.adapter as TimelineListAdapter?
                    ?: TimelineListAdapter(diffUtilCallBack, viewLifecycleOwner, nvm)
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
        mViewModel?.stop()
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