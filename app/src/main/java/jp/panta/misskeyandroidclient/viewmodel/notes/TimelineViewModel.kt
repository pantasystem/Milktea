package jp.panta.misskeyandroidclient.viewmodel.notes

import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.model.streming.TimelineCapture
import jp.panta.misskeyandroidclient.model.streming.note.NoteCapture
import jp.panta.misskeyandroidclient.model.streming.note.NoteRegister
import jp.panta.misskeyandroidclient.util.BodyLessResponse
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.notes.favorite.FavoriteNotePagingStore
import jp.panta.misskeyandroidclient.viewmodel.url.UrlPreviewLoadTask
import kotlinx.coroutines.*
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.*
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountNotFoundException
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI

// TODO ゲストとアカウントでの両方の方法を考え実現する
class TimelineViewModel(
    private val page: Page,
    include: NoteRequest.Include,
    private val miCore: MiCore,
    private val settingStore: SettingStore,
    encryption: Encryption,
    val isGuest: Boolean = false
) : ViewModel(){

    data class Request(
        val sinceId: String? = null,
        val untilId: String? = null,
        var substitute: Request? = null
    ){
        companion object{
            @JvmStatic
            fun makeUntilIdRequest(timelineState: TimelineState?, substituteSize: Int = 2): Request?{
                val ids = timelineState?.getUntilIds(substituteSize)
                return makeUntilIdRequest(ids?: emptyList())
            }

            @JvmStatic
            private fun makeUntilIdRequest(ids: List<String>, index: Int = 0): Request?{
                if(ids.size <= index){
                    return null
                }
                val now = Request(
                    untilId = ids[index]
                )
                now.substitute = makeUntilIdRequest(ids,index + 1)
                return now
            }

            @JvmStatic
            fun makeSinceIdRequest(timelineState: TimelineState?, substituteSize: Int = 2): Request?{
                return makeSinceIdRequest(
                    timelineState?.getSinceIds(substituteSize)?: emptyList()
                )
            }

            @JvmStatic
            private fun makeSinceIdRequest(ids: List<String>, index: Int = 0): Request?{
                if(ids.size <= index){
                    return null
                }
                val now = Request(
                    sinceId = ids[index]
                )
                now.substitute = makeUntilIdRequest(ids,index + 1)
                return now
            }
        }

    }

    enum class Errors{
        AUTHENTICATION,
        I_AM_AI,
        SERVER_ERROR,
        PARAMETER_ERROR,
        NETWORK,
        TIMEOUT
    }

    val tag = "TimelineViewModel"
    val errorState = MutableLiveData<Errors>()

    private val noteCaptureRegister = NoteRegister()
    //private val streamingAdapter = miCore.getStreamingAdapter(accountRelation)

    private val mNoteDeletedListener = NoteDeletedListener()

    private var noteCapture: NoteCapture? = null
    private var timelineCapture: TimelineCapture? = null


    val position = MutableLiveData<Int>()

    private var notePagingStore: NotePagedStore? = null

    private var isInitialized: Boolean = false


    var account: Account? = null

    var misskeyAPI: MisskeyAPI? = null

    init{
        viewModelScope.launch(Dispatchers.IO) {
            try{
                val account = miCore.getAccount(page.accountId)
                timelineCapture = if(settingStore.isAutoLoadTimeline){
                    miCore.getTimelineCapture(account)
                }else{
                    null
                }
                this@TimelineViewModel.account = account

                noteCapture = miCore.getNoteCapture(account)
                noteCapture?.addNoteDeletedListener(mNoteDeletedListener)
                notePagingStore = when(page.pageable()){
                    is Pageable.Favorite -> FavoriteNotePagingStore(account, page, miCore, encryption)
                    else -> NoteTimelineStore(
                        account,
                        page,
                        include,
                        miCore,
                        encryption
                    )
                }
                isInitialized = true
            }catch(e: AccountNotFoundException){
            }
        }
    }


    private var isActive: Boolean = false
    //private var mBeforeAccount: Account? = null
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
            if( nowAr != mBeforeAccount || value?.notes.isNullOrEmpty()){
                loadInit()
            }
        }
    }


    private var mObserver: TimelineCapture.TimelineObserver? = null



    val isLoading = MutableLiveData<Boolean>()
    val isInitLoading = MutableLiveData<Boolean>()

    private var isLoadingFlag = false



    fun getTimelineLiveData() : LiveData<TimelineState>{
        return timelineLiveData
    }

    fun loadNew(){
        Log.d("TimelineLiveData", "loadNew")
        if( ! isLoadingFlag ){
            isLoadingFlag = true
            //val sinceId = observableTimelineList.firstOrNull()?.id
            val request = Request.makeSinceIdRequest(timelineLiveData.value)
            if(request?.sinceId == null){
                isLoadingFlag = false
                isLoading.postValue(false)
                return loadInit()
            }
            viewModelScope.launch(Dispatchers.IO){
                try{
                    val res = syncLoad(request)
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
                } catch(e: IOException){
                    errorState.postValue(Errors.NETWORK)
                } catch(e: SocketTimeoutException){
                    errorState.postValue(Errors.TIMEOUT)
                } catch (e: Exception){
                    Log.d("TimelineLiveData", "タイムライン取得中にエラー発生", e)
                    errorState.postValue(Errors.NETWORK)
                }finally {
                    isLoading.postValue(false)
                    isLoadingFlag = false
                }

            }

        }
    }

    fun loadOld(){
        val request = Request.makeUntilIdRequest(timelineLiveData.value)
        request?.untilId
            ?: return loadInit()
        if( isLoadingFlag){
            return
        }
        isLoadingFlag = true
        viewModelScope.launch(Dispatchers.IO){
            try {
                val res = syncLoad(request)
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
                errorState.postValue(Errors.NETWORK)
            }catch(e: SocketTimeoutException){
                errorState.postValue(Errors.TIMEOUT)
            }catch(e: Exception){
                errorState.postValue(Errors.NETWORK)
                Log.d("TimelineLiveData", "タイムライン取得中にエラー発生", e)
            }finally {
                isLoadingFlag = false

            }
        }
    }

    fun loadInit(){
        Log.d("TimelineViewModel", "初期読み込みを開始します")
        this.isLoading.postValue(true)
        this.isInitLoading.postValue(true)

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

                    loadUrlPreviews(list)

                    timelineLiveData.postValue(state)

                    if(settingStore.isAutoLoadTimeline){
                        startTimelineCapture()
                    }

                } catch(e: IOException){
                    Log.d("TimelineLiveData", "タイムライン取得中にIOエラー発生", e)
                    errorState.postValue(Errors.NETWORK)
                    timelineLiveData.postValue(null)
                } catch(e: SocketTimeoutException){
                    errorState.postValue(Errors.TIMEOUT)
                    timelineLiveData.postValue(null)
                } catch (e: Exception){
                    Log.d("TimelineLiveData", "タイムライン取得中にエラー発生", e)
                    errorState.postValue(Errors.NETWORK)
                    timelineLiveData.postValue(null)

                } finally{
                    isLoading.postValue(false)
                    isLoadingFlag = false
                    isInitLoading.postValue(false)
                }

            }

        }
    }

    private fun syncLoad(request: Request?, isRetry: Boolean = false): Pair<BodyLessResponse, List<PlaneNoteViewData>?>?{
        if(request == null && isRetry){
            return null
        }

        val res = when {
            request?.untilId != null -> {
                notePagingStore?.loadOld(untilId = request.untilId)
            }
            request?.sinceId != null -> {
                notePagingStore?.loadNew(sinceId = request.sinceId)
            }
            else -> {
                notePagingStore?.loadInit()
            }
        }
        val notes = res?.second

        if(notes?.isNotEmpty() == true){
            return res
        }

        val targetNoteId = request?.untilId?: request?.sinceId ?: return null

        val targetNote = try{
            miCore.getMisskeyAPI(account).showNote(
                NoteRequest(
                    i = account?.getI(miCore.getEncryption()),
                    noteId = targetNoteId
                )
            ).execute()
        }catch(e: Throwable){
            null
        }

        if(targetNote != null){
            return res
        }

        return syncLoad(request?.substitute, true)

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
        val store = accountRelation.value?.let { ac ->
            miCore.getUrlPreviewStore(ac)
        }?: return

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