package jp.panta.misskeyandroidclient.view.notes

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager

import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.KeyStore
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.viewmodel.notes.*
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

        sharedPreference = PreferenceManager.getDefaultSharedPreferences(this.context)

        mLinearLayoutManager = LinearLayoutManager(this.context!!)
        list_view.layoutManager = mLinearLayoutManager

        //データ受け取り

        mSetting = arguments?.getSerializable(EXTRA_TIMELINE_FRAGMENT_NOTE_REQUEST_SETTING) as NoteRequest.Setting

        val miApplication = context?.applicationContext as MiApplication

        list_view.addOnScrollListener(mScrollListener)
        list_view.layoutManager = mLinearLayoutManager

        val isAutoLoad = getBoolean(KeyStore.BooleanKey.AUTO_LOAD_TIMELINE)

        miApplication.currentConnectionInstanceLiveData.observe(viewLifecycleOwner, Observer {ci ->
            val factory = TimelineViewModelFactory(ci, mSetting!!, miApplication, isAutoLoad)
            val vm = mViewModel

            //TimelineViewModelは必ず参照がnullになるか接続先の情報に更新があったときのみ初期化する
            if(vm == null || vm.connectionInstance != ci){
                Log.d("TimelineFragment", "初期化処理をします: vm is null:${vm == null}, CI非一致:${vm?.connectionInstance != ci}")
                mViewModel = ViewModelProvider(this, factory).get("$ci",TimelineViewModel::class.java)
                mViewModel?.loadInit()

                val notesViewModelFactory = NotesViewModelFactory(ci, miApplication)
                mNotesViewModel = ViewModelProvider(activity!!, notesViewModelFactory).get(NotesViewModel::class.java)
                mNotesViewModel?.connectionInstance = ci
                mNotesViewModel?.misskeyAPI = miApplication.misskeyAPIService!!



            }
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

        if(isLoadInit){

        }else{
            //mViewModel?.loadInit()
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

        Log.d("TimelineFragment", "onDestroyView")
    }


    private fun getBoolean(key: KeyStore.BooleanKey): Boolean{
        return sharedPreference.getBoolean(key.name, key.default)
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

}