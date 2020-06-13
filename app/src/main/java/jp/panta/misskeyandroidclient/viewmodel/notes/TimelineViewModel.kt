package jp.panta.misskeyandroidclient.viewmodel.notes


import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.Page
import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.model.streming.TimelineCapture
import jp.panta.misskeyandroidclient.model.streming.note.NoteCapture
import jp.panta.misskeyandroidclient.model.streming.note.NoteRegister
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.notes.favorite.FavoriteNotePagingStore
import jp.panta.misskeyandroidclient.viewmodel.url.UrlPreviewLoadTask
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*

class TimelineViewModel(
    val account: Account?,
    private val pageableTimeline: Page.Timeline,
    include: NoteRequest.Include,
    private val miCore: MiCore,
    private val settingStore: SettingStore,
    encryption: Encryption
) : ViewModel(){


    val tag = "TimelineViewModel"
    val errorState = MediatorLiveData<String>()

    private val noteCaptureRegister = NoteRegister()
    //private val streamingAdapter = miCore.getStreamingAdapter(accountRelation)

    private val mNoteDeletedListener = NoteDeletedListener()

    private var noteCapture: NoteCapture? = null
    private var timelineCapture: TimelineCapture? = null


    val position = MutableLiveData<Int>()

    private var notePagingStore: NotePagedStore? = null

    private var isInitialized: Boolean = false

    private val accountRelation = MediatorLiveData<AccountRelation>().apply{
        fun init(ar: AccountRelation?){
            ar?: return
            timelineCapture = if(settingStore.isAutoLoadTimeline){
                miCore.getTimelineCapture(ar)
            }else{
                null
            }
            noteCapture = miCore.getNoteCapture(ar)
            noteCapture?.addNoteDeletedListener(mNoteDeletedListener)
            notePagingStore = when(pageableTimeline){
                is Page.Favorite -> FavoriteNotePagingStore(ar, pageableTimeline, miCore, encryption)
                else -> NoteTimelineStore(
                    ar,
                    pageableTimeline,
                    include,
                    miCore,
                    encryption
                )
            }
            //noteCapture?.addNoteDeletedListener(noteRemovedListener)
            isInitialized = true

        }
        addSource(miCore.currentAccount){
            if( account != null ) return@addSource
            init(it)
            value = it
        }
        addSource(miCore.accounts){
            if( account == null ) return@addSource
            if( value?.account == account ) return@addSource
            val ar = it.firstOrNull { ar ->
                ar.account == account
            }
            init(ar)
            value = ar
        }


    }

    private var isActive: Boolean = false
    private var mBeforeAccountRelation: AccountRelation? = null
    private val timelineLiveData = object : MediatorLiveData<TimelineState>(){
        override fun onActive() {
            super.onActive()
            isActive = true

            if(!settingStore.isUpdateTimelineInBackground){
                startNoteCapture()
            }
            if(isInitialized && value?.notes.isNullOrEmpty()){
                loadInit()
            }
        }

        override fun onInactive() {
            super.onInactive()
            isActive = false

            if(settingStore.isAutoLoadTimeline && !settingStore.isUpdateTimelineInBackground){
                stopTimelineCapture()
                stopNoteCapture()
            }
        }
    }.apply{
        addSource(accountRelation){ nowAr ->
            if(isActive){
                startNoteCapture()
            }
            if( nowAr != mBeforeAccountRelation || value?.notes.isNullOrEmpty()){
                loadInit()
            }
            mBeforeAccountRelation = nowAr
        }
    }


    private var mObserver: TimelineCapture.TimelineObserver? = null



    val isLoading = MutableLiveData<Boolean>()

    private var isLoadingFlag = false



    fun getTimelineLiveData() : LiveData<TimelineState>{
        return timelineLiveData
    }

    fun loadNew(){
        Log.d("TimelineLiveData", "loadNew")
        if( ! isLoadingFlag ){
            isLoadingFlag = true
            //val sinceId = observableTimelineList.firstOrNull()?.id
            val sinceId = timelineLiveData.value?.getSinceId()
            if(sinceId == null){
                isLoadingFlag = false
                isLoading.postValue(false)
                return loadInit()
            }
            viewModelScope.launch(Dispatchers.IO){
                try{
                    val res = notePagingStore?.loadNew(sinceId)
                        ?: return@launch
                    val list = res.second
                    if(list.isNullOrEmpty()){
                        return@launch
                    }else{
                        noteCapture?.subscribeAll(noteCaptureRegister.registerId, list)
                            ?: return@launch
                        loadUrlPreviews(list)

                        val state = timelineLiveData.value
                        val newState = if(state == null){
                            startTimelineCapture()

                            TimelineState(
                                list,
                                TimelineState.State.LOAD_NEW
                            )

                        }else{
                            if(settingStore.isAutoLoadTimeline && !settingStore.isUpdateTimelineInBackground && list.size < 20){
                                startTimelineCapture()
                            }
                            val newList = ArrayList<PlaneNoteViewData>(state.notes).apply {
                                addAll(0, list)
                            }
                            TimelineState(
                                newList,
                                TimelineState.State.LOAD_NEW
                            )
                        }

                        timelineLiveData.postValue(newState)
                    }
                }catch(e: IOException){
                }catch (e: Exception){
                    Log.d("TimelineLiveData", "タイムライン取得中にエラー発生", e)
                }finally {
                    isLoading.postValue(false)
                    isLoadingFlag = false
                }

            }

        }
    }

    fun loadOld(){
        val untilId = timelineLiveData.value?.getUntilId() ?: return loadInit()
        if( isLoadingFlag){
            return
        }
        isLoadingFlag = true
        viewModelScope.launch(Dispatchers.IO){
            try {
                val res = notePagingStore?.loadOld(untilId)
                    ?: return@launch
                val list = res.second
                if(list.isNullOrEmpty()){
                    return@launch
                }else{
                    noteCapture?.subscribeAll(noteCaptureRegister.registerId, list)
                        ?: return@launch
                    loadUrlPreviews(list)
                    val state = timelineLiveData.value

                    val newState = if(state == null){
                        TimelineState(
                            list,
                            TimelineState.State.LOAD_OLD
                        )
                    }else{
                        val newList = ArrayList<PlaneNoteViewData>(state.notes).apply{
                            addAll(list)
                        }
                        TimelineState(
                            newList,
                            TimelineState.State.LOAD_OLD
                        )
                    }

                    timelineLiveData.postValue(newState)
                }
            }catch (e: IOException){

            }catch(e: Exception){
                Log.d("TimelineLiveData", "タイムライン取得中にエラー発生", e)
            }finally {
                isLoadingFlag = false

            }
        }
    }

    fun loadInit(){
        Log.d("TimelineViewModel", "初期読み込みを開始します")
        this.isLoading.postValue(true)

        if( ! isLoadingFlag ){

            isLoadingFlag = true

            viewModelScope.launch(Dispatchers.IO){
                try{
                    val response = notePagingStore?.loadInit()!!
                    val list = response.second?: emptyList()
                    val state = TimelineState(
                        list,
                        TimelineState.State.INIT
                    )
                    noteCapture?.subscribeAll(noteCaptureRegister.registerId, list)
                        ?: return@launch
                    loadUrlPreviews(list)

                    timelineLiveData.postValue(state)

                    if(settingStore.isAutoLoadTimeline){
                        startTimelineCapture()
                    }

                }catch(e: IOException){
                    Log.d("TimelineLiveData", "タイムライン取得中にIOエラー発生", e)
                }catch (e: Exception){
                    Log.d("TimelineLiveData", "タイムライン取得中にエラー発生", e)
                }finally{
                    isLoading.postValue(false)
                    isLoadingFlag = false
                }

            }

        }
    }

    fun start(){

        if(settingStore.isUpdateTimelineInBackground){
            startNoteCapture()
        }
    }

    fun stop(){

        if(settingStore.isUpdateTimelineInBackground){
            stopTimelineCapture()
            stopNoteCapture()
        }
    }


    private fun startTimelineCapture(){
        if(timelineCapture != null){
            //observer?.updateId()
            val observer = TimelineCapture.TimelineObserver.create(pageableTimeline, this.timelineObserver)
            if(observer != null && mObserver == null){
                Log.d(tag, "タイムラインキャプチャーを開始しようとしている")
                timelineCapture?.addChannelObserver(observer)
                mObserver = observer
            }
        }
    }

    private fun stopTimelineCapture(){
        if(timelineCapture != null){
            val observer = mObserver
            if(observer != null){
                Log.d("TimelineViewModel" , "タイムラインキャプチャー停止中")
                timelineCapture?.removeChannelObserver(observer)
                mObserver = null
            }

        }
    }

    private fun startNoteCapture(){
        Log.d(tag, "ノートのキャプチャーを開始しようとしている")

        noteCapture?.attach(noteCaptureRegister)

    }

    private fun stopNoteCapture(){
        noteCapture?.detach(noteCaptureRegister)
    }



    private val timelineObserver = object : TimelineCapture.Observer{
        override fun onReceived(note: PlaneNoteViewData) {
            if(isLoadingFlag){
                return
            }
            noteCapture?.subscribe(noteCaptureRegister.registerId, note)
            loadUrlPreviews(listOf(note))
            val notes = timelineLiveData.value?.notes
            val list = if(notes == null){
                arrayListOf(note)
            }else{
                ArrayList<PlaneNoteViewData>(notes).apply{
                    add(0, note)
                }
            }
            timelineLiveData.postValue(
                TimelineState(
                    list,
                    TimelineState.State.RECEIVED_NEW
                )
            )
        }
    }

    inner class NoteDeletedListener : NoteCapture.DeletedListener(){
        override fun onDeleted(noteId: String) {
            val list = timelineLiveData.value?.notes
            if(list == null){
                return
            }else{
                val timeline = ArrayList<PlaneNoteViewData>(list)
                timeline.filter{
                    it.toShowNote.id == noteId
                }.forEach{
                    timeline.remove(it)
                }
                timelineLiveData.postValue(
                    TimelineState(
                        timeline,
                        TimelineState.State.REMOVED
                    )
                )
            }

        }
    }

    private fun loadUrlPreviews(list: List<PlaneNoteViewData>) {
        val store = miCore.urlPreviewStore?: return
        list.forEach{ note ->
            note.textNode?.getUrls()?.let{ urls ->
                UrlPreviewLoadTask(
                    store,
                    urls,
                    viewModelScope
                ).load(note.urlPreviewLoadTaskCallback)
            }

        }

    }





}