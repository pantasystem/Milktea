package jp.panta.misskeyandroidclient.viewmodel.notes

import android.util.Log
import androidx.lifecycle.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.model.streming.TimelineCapture
import jp.panta.misskeyandroidclient.model.streming.note.v2.NoteCapture
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
import jp.panta.misskeyandroidclient.model.notes.*
import jp.panta.misskeyandroidclient.model.streming.note.v2.captureAll
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashMap

class TimelineViewModel : ViewModel{
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


    private var noteCapture: NoteCapture? = null
    private var noteCaptureClient: NoteCapture.Client? = null

    private var timelineCapture: TimelineCapture? = null
    //private var noteRepository: NoteRepository? = null
    private var noteEventStore: NoteEventStore? = null


    val position = MutableLiveData<Int>()

    private var notePagingStore: NotePagedStore? = null

    private var isInitialized: Boolean = false

    private val usingAccount = MediatorLiveData<Account>()


    /**
     * このViewModelに紐づけられたAccountです。
     * 特にアカウントが紐づけられていない場合はNullになります。
     */
    private var reservedAccount: Account? = null

    var misskeyAPI: MisskeyAPI? = null
        private set

    val miCore: MiCore

    val settingStore: SettingStore

    val include: NoteRequest.Include

    val pageable: Pageable

    private val mCompositeDisposable = CompositeDisposable()

    private var stoppedAt: Date = Date()
    private var mNoteEventDisposable: Disposable? = null

    constructor(page: Page, miCore: MiCore, include: NoteRequest.Include)
            : this(page.pageable(),miCore, include) {

        viewModelScope.launch(Dispatchers.IO) {
            try{
                val account = try{
                    miCore.getAccount(page.accountId)
                }catch(e: AccountNotFoundException){
                    miCore.getCurrentAccount().value
                }?: return@launch

                this@TimelineViewModel.reservedAccount = account
                applyAccount(account, page.pageable())

                isInitialized = true
            }catch(e: AccountNotFoundException){

            }
        }
    }
    constructor(pageable: Pageable, miCore: MiCore, account: Account?, include: NoteRequest.Include)
            : this(pageable, miCore, include){
        usingAccount.postValue(account)
        this.reservedAccount = account
    }

    constructor(pageable: Pageable, miCore: MiCore, include: NoteRequest.Include) : super(){
        this.miCore = miCore
        this.settingStore = miCore.getSettingStore()
        this.include = include
        this.pageable = pageable
        usingAccount.addSource(miCore.getCurrentAccount()){ current ->
            if(reservedAccount == null || reservedAccount?.accountId == current.accountId){
                Log.d("TimelineVM", "アカウント初期化: $current,")
                usingAccount.postValue(current)
                applyAccount(current, pageable)
            }
        }

    }

    /**
     * アカウントを変数やデータストアやイベントストリームなどに適応し
     * 状態を初期化します
     */
    private fun applyAccount(account: Account, pageable: Pageable){
        Log.d("TimelineVM", "pageable:$pageable, param:${pageable.toParams()}")

        if(mBeforeAccount != null){
            noteCaptureClient?.let{
                noteCapture?.detachClient(it)
            }
        }
        timelineCapture = if(settingStore.isAutoLoadTimeline){
            miCore.getTimelineCapture(account)
        }else{
            null
        }

        noteCapture = miCore.getNoteCapture(account)
        noteCaptureClient = NoteCapture.Client()
        //noteCapture?.addNoteDeletedListener(mNoteDeletedListener)
        noteEventStore = miCore.getNoteEventStore(account)
        notePagingStore = when(pageable){
            is Pageable.Favorite -> FavoriteNotePagingStore(account, pageable, miCore)
            else -> NoteTimelineStore(
                account,
                pageable,
                include,
                miCore
            )
        }

        misskeyAPI = miCore.getMisskeyAPI(account)
        loadInit()



        noteEventStore?.getEventStream()
        //this.account = account

    }

    private var mNoteIds = HashSet<String>()

    private var isActive: Boolean = false
    private var mBeforeAccount: Account? = null
    private val timelineLiveData = object : MediatorLiveData<TimelineState>(){
        override fun onActive() {
            super.onActive()

            active(this.value?.notes)
        }

        override fun onInactive() {
            super.onInactive()

            inactive()
        }
    }.apply{
        addSource(usingAccount){ using ->
            if(isActive){
                startNoteCapture()
            }
            if( using != mBeforeAccount || value?.notes.isNullOrEmpty()){
                loadInit()
            }
            mBeforeAccount = using
        }
    }

    private fun active(notes: List<PlaneNoteViewData>?){
        isActive = true

        if(!settingStore.isUpdateTimelineInBackground){
            startNoteCapture()
        }
        if(isInitialized && notes.isNullOrEmpty()){
            loadInit()
        }
    }

    private fun inactive(){
        isActive = false

        if(settingStore.isAutoLoadTimeline && !settingStore.isUpdateTimelineInBackground){
            stopTimelineCapture()
            stopNoteCapture()
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
                    val list = res.second?.filter(::filterDuplicate)
                    if(list.isNullOrEmpty()){
                        return@launch
                    }else{
                        releaseAndCapture(list)
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

                        mNoteIds.addAll(list.map(::mapId))

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
                val list = res.second?.filter(::filterDuplicate)
                if(list.isNullOrEmpty()){
                    return@launch
                }else{
                    releaseAndCapture(list)
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
                    mNoteIds.addAll(list.map(::mapId))
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
                    //noteCapture?.subscribeAll(noteCaptureRegister.registerId, list)
                    releaseAndCapture(list)

                    loadUrlPreviews(list)

                    mNoteIds.clear()
                    mNoteIds.addAll(list.map(::mapId))

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

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun syncLoad(request: Request?, isRetry: Boolean = false): Pair<BodyLessResponse, List<PlaneNoteViewData>?>?{
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
            misskeyAPI?.showNote(
                NoteRequest(
                    i = reservedAccount?.getI(miCore.getEncryption()),
                    noteId = targetNoteId
                )
            )?.execute()
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
            val observer = TimelineCapture.TimelineObserver.create(pageable, this.timelineObserver)
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

        //noteCapture?.attach(noteCaptureRegister)
        noteCaptureClient?.let{
            noteCapture?.attachClient(it)
        }

        if(mNoteEventDisposable == null){
            mNoteEventDisposable = noteEventStore?.getEventStream(stoppedAt)?.filter {
                !(it.event as? Event.Added != null && it.authorId == noteCaptureClient?.clientId)
            }?.subscribe {
                noteEventObserver(it)
            }
        }


    }

    private fun stopNoteCapture(){
        noteCaptureClient?.let{
            noteCapture?.detachClient(it)
        }
        stoppedAt = Date()
        mNoteEventDisposable?.dispose()
        mNoteEventDisposable = null
    }



    private val timelineObserver = object : TimelineCapture.Observer{
        override fun onReceived(note: PlaneNoteViewData) {
            if(isLoadingFlag){
                return
            }
            if(mNoteIds.contains(note.id)){
                Log.i("TM-VM", "重複を確認したためキャンセルする")
                return
            }
            noteCaptureClient?.capture(note.id)
            loadUrlPreviews(listOf(note))
            val notes = timelineLiveData.value?.notes
            val list = if(notes == null){
                arrayListOf(note)
            }else{
                ArrayList<PlaneNoteViewData>(notes).apply{
                    add(0, note)
                }
            }
            mNoteIds.add(note.id)
            if(list.size > mNoteIds.size){
                Log.d("TM-VM", "重複が発生しています ${mNoteIds.size}: ${list.size}")
            }
            timelineLiveData.postValue(
                TimelineState(
                    list,
                    TimelineState.State.RECEIVED_NEW
                )
            )
        }
    }

    private fun releaseAndCapture(notes: List<PlaneNoteViewData>){
        val captureNoteIds = HashSet<String>()
        for(note in notes){
            captureNoteIds.add(note.id)
            captureNoteIds.add(note.toShowNote.id)
        }
        val successCount = noteCaptureClient?.captureAll(captureNoteIds.toList())
        if(successCount != captureNoteIds.size){
            Log.d("TM-VM", "Captureへの登録に失敗したようです。試みた件数:${captureNoteIds.size}, 成功した件数:$successCount")
        }
    }

    private fun noteEventObserver(noteEvent: NoteEvent){
        Log.d("TM-VM", "#noteEventObserver $noteEvent")
        val timelineNotes = timelineLiveData.value?.notes
            ?: return

        val account = usingAccount.value
        val updatedNotes = when(noteEvent.event){

            is Event.Deleted ->{
                timelineNotes.filterNot{ note ->
                    note.id == noteEvent.noteId || note.toShowNote.id == noteEvent.noteId
                }
            }
            else -> timelineNotes.map{
                val note: PlaneNoteViewData = it
                if(note.toShowNote.id == noteEvent.noteId){
                    when(noteEvent.event){
                        is Event.Reacted ->{
                            it.addReaction(noteEvent.event.reaction, noteEvent.event.emoji, noteEvent.event.userId == account?.remoteId)
                        }
                        is Event.UnReacted ->{
                            it.takeReaction(noteEvent.event.reaction, noteEvent.event.userId == account?.remoteId)
                        }
                        is Event.Voted ->{
                            it.poll?.update(noteEvent.event.choice, noteEvent.event.userId == account?.remoteId)
                        }
                        is Event.Added ->{
                            note.update(noteEvent.event.note)
                        }
                    }
                }

                note
            }
        }
        if(noteEvent.event is Event.Deleted){
            timelineLiveData.postValue(TimelineState(state = TimelineState.State.REMOVED, notes = updatedNotes))
        }
    }


    private fun loadUrlPreviews(list: List<PlaneNoteViewData>) {
        val store = reservedAccount?.let { ac ->
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



    private fun mapId(planeNoteViewData: PlaneNoteViewData): String{
        return planeNoteViewData.id
    }

    private fun filterDuplicate(planeNoteViewData: PlaneNoteViewData) : Boolean{
        Log.d("TM-VM-Filter", "重複を発見したため排除しました")
        return ! mNoteIds.contains(planeNoteViewData.id)
    }





}