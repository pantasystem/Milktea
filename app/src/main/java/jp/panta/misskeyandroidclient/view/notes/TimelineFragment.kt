package jp.panta.misskeyandroidclient.view.notes

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.setMenuTint
import jp.panta.misskeyandroidclient.util.getPreferenceName
import jp.panta.misskeyandroidclient.view.PageableView
import jp.panta.misskeyandroidclient.view.ScrollableTop
import jp.panta.misskeyandroidclient.viewmodel.notes.*
import kotlinx.android.synthetic.main.fragment_swipe_refresh_recycler_view.*
import java.lang.Exception
import java.util.*
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.account.page.Pageable

class TimelineFragment : Fragment(R.layout.fragment_swipe_refresh_recycler_view), ScrollableTop, PageableView{

    companion object{
        private const val EXTRA_TIMELINE_FRAGMENT_PAGEABLE_TIMELINE = "jp.panta.misskeyandroidclient.view.notes.TimelineFragment.pageable_timeline"

        private const val EXTRA_PAGE = "jp.panta.misskeyandroidclient.EXTRA_PAGE"
        private const val EXTRA_PAGEABLE = "jp.panta.misskeyandroidclient.EXTRA_PAGEABLE"

        private const val EXTRA_FIRST_VISIBLE_NOTE_DATE = "jp.panta.misskeyandroidclient.view.notes.TimelineFragment.EXTRA_FIRST_VISIBLE_NOTE_DATE"


        fun newInstance(page: Page): TimelineFragment{
            return TimelineFragment().apply {
                arguments = Bundle().apply{
                    putSerializable(EXTRA_PAGE, page)
                }
            }
        }

        fun newInstance(pageable: Pageable) : TimelineFragment{
            return TimelineFragment().apply{
                arguments = Bundle().apply{
                    putSerializable(EXTRA_PAGEABLE, pageable)
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

    //private var mPageableTimeline: Page.Timeline? = null
    private var mPage: Page? = null

    private var mPageable: Pageable? = null

    private var isShowing: Boolean = false

    private var mFirstVisibleItemPosition: Int? = null

    private lateinit var sharedPreference: SharedPreferences



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setHasOptionsMenu(true)

        sharedPreference = requireContext().getSharedPreferences(requireContext().getPreferenceName(), MODE_PRIVATE)
        //sharedPreference = view.context.getSharedPreferences()

        mLinearLayoutManager = LinearLayoutManager(this.requireContext())
        list_view.layoutManager = mLinearLayoutManager

        //データ受け取り
        mPage = arguments?.getSerializable(EXTRA_PAGE) as Page

        mPageable = arguments?.getSerializable(EXTRA_PAGEABLE) as? Pageable

        val miApplication = context?.applicationContext as MiApplication

        list_view.addOnScrollListener(mScrollListener)
        list_view.layoutManager = mLinearLayoutManager

        val notesViewModelFactory = NotesViewModelFactory(miApplication)
        val notesViewModel = ViewModelProvider(requireActivity(), notesViewModelFactory).get(NotesViewModel::class.java)
        mNotesViewModel = notesViewModel
        val factory = TimelineViewModelFactory(mPage, null, mPage?.pageable()?: mPageable!!, miApplication)
        mViewModel = ViewModelProvider(this, factory).get("timelineFragment:$mPage",TimelineViewModel::class.java)



        refresh.setOnRefreshListener {
            mViewModel?.loadNew()
        }

        mViewModel?.isLoading?.observe(viewLifecycleOwner, Observer<Boolean> {
            if(it != null && !it){
                refresh?.isRefreshing = false
            }
        })

        //mLinearLayoutManager.scrollToPosition(mViewModel?.position?.value?: 0)
        val adapter = TimelineListAdapter(diffUtilCallBack, viewLifecycleOwner, notesViewModel)
        list_view.adapter = adapter

        var  timelineState: TimelineState.State? = null
        mViewModel?.getTimelineLiveData()?.observe(viewLifecycleOwner, Observer { tm ->
            if( tm != null){

                adapter.submitList(tm.notes)
                timelineState = tm.state
            }
            if(tm?.notes.isNullOrEmpty()){
                timelineEmptyView.visibility = View.VISIBLE
                refresh.visibility = View.GONE
            }else{
                timelineEmptyView.visibility = View.GONE
                refresh.visibility = View.VISIBLE
            }
            timelineProgressBar.visibility = View.GONE


        })

        mViewModel?.isInitLoading?.observe( viewLifecycleOwner, Observer {
            if(it){

                timelineProgressBar.visibility = View.VISIBLE
                refresh.visibility = View.GONE
                timelineEmptyView.visibility = View.GONE
            } else {
                timelineProgressBar.visibility = View.GONE

            }
        })

        mViewModel?.errorState?.observe( viewLifecycleOwner, Observer { error ->
            Log.d("TimelineFragment", "error:$error")
            when(error){
                TimelineViewModel.Errors.AUTHENTICATION ->{
                    Toast.makeText(requireContext(), R.string.auth_error, Toast.LENGTH_LONG).show()
                }
                TimelineViewModel.Errors.I_AM_AI ->{
                    Toast.makeText(requireContext(), R.string.bot_error, Toast.LENGTH_LONG).show()
                }
                TimelineViewModel.Errors.PARAMETER_ERROR ->{
                    Toast.makeText(requireContext(), R.string.parameter_error, Toast.LENGTH_LONG).show()
                }
                TimelineViewModel.Errors.SERVER_ERROR ->{
                    Toast.makeText(requireContext(), R.string.auth_error, Toast.LENGTH_LONG).show()
                }
                TimelineViewModel.Errors.NETWORK ->{
                    Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_LONG).show()
                }
                TimelineViewModel.Errors.TIMEOUT ->{
                    Toast.makeText(requireContext(), R.string.timeout_error, Toast.LENGTH_LONG).show()
                }
                else ->{
                    Log.d("TimelineViewModel", "不明なエラー")
                }
            }
        })

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver(){

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                if(timelineState == TimelineState.State.RECEIVED_NEW && positionStart == 0 && mFirstVisibleItemPosition == 0 && isShowing && itemCount == 1){
                    mLinearLayoutManager.scrollToPosition(0)
                }
            }
        })

        mViewModel?.position?.value?.let{
            try{
                mLinearLayoutManager.scrollToPosition(it)
            }catch(e: Exception){

            }
        }

        retryLoadButton.setOnClickListener {
            Log.d("TimelineFragment", "リトライボタンを押しました")
            mViewModel?.loadInit()
        }

        //mViewModel?.loadInit()

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
        if(mViewModel?.getTimelineLiveData()?.value?.notes?.isNotEmpty() == true){
            mViewModel?.loadNew()
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_timeline, menu)
        requireContext().setMenuTint(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.refresh_timeline ->{
                mViewModel?.loadInit()
            }
        }
        return super.onOptionsItemSelected(item)
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