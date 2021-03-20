package jp.panta.misskeyandroidclient.viewmodel.notes

import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.api.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.notes.*
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.streaming.ChannelBody
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPI
import jp.panta.misskeyandroidclient.util.BodyLessResponse
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.notes.favorite.FavoriteNotePagingStore
import jp.panta.misskeyandroidclient.viewmodel.url.UrlPreviewLoadTask
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.math.log

@ExperimentalCoroutinesApi
class TimelineViewModel(
    val account: Account?,
    val accountId: Long? = account?.accountId,
    val pageable: Pageable,
    val miCore: MiCore,
    val settingStore: SettingStore,
    val include: NoteRequest.Include
) : ViewModel(){

    data class Request(
        val sinceId: String? = null,
        val untilId: String? = null,
        var substitute: Request? = null
    )

    enum class Errors{
        AUTHENTICATION,
        I_AM_AI,
        SERVER_ERROR,
        PARAMETER_ERROR,
        NETWORK,
        TIMEOUT
    }

    val tag = "TimelineViewModel"
    private val mErrorEvent = MutableSharedFlow<Errors>()
    val errorEvent: SharedFlow<Errors> = mErrorEvent

    private val accountRepository = miCore.getAccountRepository()

    val position = MutableLiveData<Int>()

    private var mNoteIds = HashSet<Note.Id>()

    private val timelineLiveData = MediatorLiveData<TimelineState>()


    val isLoading = MutableLiveData<Boolean>()
    val isInitLoading = MutableLiveData<Boolean>()

    private var isLoadingFlag = false

    private val logger = miCore.loggerFactory.create("TimelineViewModel")

    init {
        flow {
            emit(getAccount())
        }.filter {
            pageable is Pageable.GlobalTimeline
                    || pageable is Pageable.HybridTimeline
                    || pageable is Pageable.LocalTimeline
                    || pageable is Pageable.HomeTimeline
        }.flatMapLatest { account ->
            when(pageable) {
                is Pageable.GlobalTimeline -> {
                    miCore.getChannelAPI(account).connect(ChannelAPI.Type.GLOBAL)
                }
                is Pageable.HybridTimeline -> {
                    miCore.getChannelAPI(account).connect(ChannelAPI.Type.HYBRID)

                }
                is Pageable.LocalTimeline -> {
                    miCore.getChannelAPI(account).connect(ChannelAPI.Type.LOCAL)

                }
                is Pageable.HomeTimeline -> {
                    miCore.getChannelAPI(account).connect(ChannelAPI.Type.HOME)
                }
                else -> throw IllegalStateException("Global, Hybrid, Local, Homeは以外のStreamは対応していません。")
            }
        }.map {
          it as? ChannelBody.ReceiveNote
        }.filterNotNull().map{
            miCore.getGetters().noteRelationGetter.get(getAccount(), it.body)
        }.filter {
           !mNoteIds.contains(it.note.id) && !this.isLoadingFlag
        }.map{
            PlaneNoteViewData(it, getAccount(), DetermineTextLengthSettingStore(miCore.getSettingStore()), miCore.getNoteCaptureAdapter())
        }.onEach {
            this.mNoteIds.add(it.id)
            listOf(it).captureNotes()
            val list = ArrayList<PlaneNoteViewData>(this.timelineLiveData.value?.notes?: emptyList<PlaneNoteViewData>())
            list.add(0, it)
            this.timelineLiveData.postValue(
                TimelineState(
                    state = TimelineState.State.RECEIVED_NEW,
                    notes = list
                )
            )
        }.launchIn(viewModelScope + Dispatchers.IO)

        loadInit()
    }




    fun getTimelineLiveData() : LiveData<TimelineState?>{
        return timelineLiveData
    }

    fun loadNew(){
        Log.d("TimelineLiveData", "loadNew")
        if( ! isLoadingFlag ){
            isLoadingFlag = true
            //val sinceId = observableTimelineList.firstOrNull()?.id
            val request = timelineLiveData.value.makeSinceIdRequest()
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
                        list.captureNotes()
                        loadUrlPreviews(list)

                        val state = timelineLiveData.value
                        val newState = if(state == null){
                            //startTimelineCapture()

                            TimelineState(
                                list,
                                TimelineState.State.LOAD_NEW
                            )

                        }else{
                            /*if(settingStore.isAutoLoadTimeline && !settingStore.isUpdateTimelineInBackground && list.size < 20){
                                startTimelineCapture()
                            }*/
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
                    mErrorEvent.tryEmit(Errors.NETWORK)
                } catch(e: SocketTimeoutException){
                    mErrorEvent.tryEmit(Errors.TIMEOUT)
                } catch (e: Exception){
                    Log.d("TimelineLiveData", "タイムライン取得中にエラー発生", e)
                    mErrorEvent.tryEmit(Errors.NETWORK)
                }finally {
                    isLoading.postValue(false)
                    isLoadingFlag = false
                }

            }

        }
    }

    fun loadOld(){
        val request = timelineLiveData.value.makeUntilIdRequest()
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
                    list.captureNotes()
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
            } catch(e: IOException){
                mErrorEvent.tryEmit(Errors.NETWORK)
            } catch(e: SocketTimeoutException){
                mErrorEvent.tryEmit(Errors.TIMEOUT)
            } catch (e: Exception){
                Log.d("TimelineLiveData", "タイムライン取得中にエラー発生", e)
                mErrorEvent.tryEmit(Errors.NETWORK)
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
                    val account = getAccount()
                    val response = account.getPagedStore().loadInit()
                    val list = response.second?: emptyList()
                    logger.debug("Networkから受信")
                    val state = TimelineState(
                        list,
                        TimelineState.State.INIT
                    )

                    //noteCapture?.subscribeAll(noteCaptureRegister.registerId, list)
                    list.captureNotes()

                    loadUrlPreviews(list)

                    mNoteIds.clear()
                    mNoteIds.addAll(list.map(::mapId))

                    timelineLiveData.postValue(state)

                    /*if(settingStore.isAutoLoadTimeline){
                        startTimelineCapture()
                    }*/

                } catch(e: IOException){
                    mErrorEvent.tryEmit(Errors.NETWORK)
                    timelineLiveData.postValue(null)

                } catch(e: SocketTimeoutException){
                    mErrorEvent.tryEmit(Errors.TIMEOUT)
                    timelineLiveData.postValue(null)

                } catch (e: Exception){
                    Log.d("TimelineLiveData", "タイムライン取得中にエラー発生", e)
                    mErrorEvent.tryEmit(Errors.NETWORK)
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
        val account = getAccount()
        val res = when {
            request?.untilId != null -> {
                account.getPagedStore().loadOld(untilId = request.untilId)
            }
            request?.sinceId != null -> {
                account.getPagedStore().loadNew(sinceId = request.sinceId)
            }
            else -> {
                account.getPagedStore().loadInit()
            }
        }
        val notes = res.second

        if(notes?.isNotEmpty() == true){
            return res
        }

        val targetNoteId = request?.untilId?: request?.sinceId ?: return null

        val targetNote = try{
            account.getMisskeyAPI().showNote(
                NoteRequest(
                    i = account.getI(miCore.getEncryption()),
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


    private suspend fun loadUrlPreviews(list: List<PlaneNoteViewData>) {
        val account  = getAccount()

        list.forEach{ note ->
            note.textNode?.getUrls()?.let{ urls ->
                UrlPreviewLoadTask(
                    miCore.getUrlPreviewStore(account),
                    urls,
                    viewModelScope
                ).load(note.urlPreviewLoadTaskCallback)
            }

        }
    }



    private fun mapId(planeNoteViewData: PlaneNoteViewData): Note.Id{
        return planeNoteViewData.id
    }

    private fun filterDuplicate(planeNoteViewData: PlaneNoteViewData) : Boolean{
        Log.d("TM-VM-Filter", "重複を発見したため排除しました")
        return ! mNoteIds.contains(planeNoteViewData.id)
    }



    private fun List<PlaneNoteViewData>.captureNotes() {
        val scope = viewModelScope + Dispatchers.IO
        val notes = this
        viewModelScope.launch(Dispatchers.IO) {
            notes.forEach { note ->
                note.eventFlow.onEach {
                    if(it is NoteDataSource.Event.Deleted) {
                        timelineLiveData.postValue(
                            TimelineState(
                                state = TimelineState.State.REMOVED,
                                notes =timelineLiveData.value?.notes?.filterNot { pnvd ->
                                    pnvd.id == it.noteId
                                } ?: emptyList()
                            )
                        )
                    }
                }.launchIn(scope)
            }
        }
    }



    private fun TimelineState?.makeUntilIdRequest(substituteSize: Int = 2): Request? {
        val ids = this?.getUntilIds(substituteSize)?: emptyList()
        return ids.makeUntilIdRequest()
    }

    private fun List<String>.makeUntilIdRequest(index: Int = 0): Request? {
        if(this.size <= index) {
            return null
        }

        val now = Request(
            untilId = this[index]
        )
        now.substitute = this.makeUntilIdRequest(index + 1)
        return now
    }

    fun TimelineState?.makeSinceIdRequest(substituteSize: Int = 2): Request?{
        return (this?.getSinceIds(substituteSize)?: emptyList()).makeSinceIdRequest()
    }

    private fun List<String>.makeSinceIdRequest(index: Int = 0): Request?{
        if(this.size <= index){
            return null
        }
        val now = Request(
            sinceId = this[index]
        )
        now.substitute = makeUntilIdRequest(index + 1)
        return now
    }

    //
    private var mAccountCache: Account? = account
    private suspend fun getAccount(): Account {
        if(mAccountCache != null) {
            mAccountCache
        }

        if(accountId != null && accountId > 0) {
            val ac = accountRepository.get(accountId)
            mAccountCache = ac
            return mAccountCache?: throw IllegalStateException("Accountが取得できませんでした。")
        }

        mAccountCache = accountRepository.getCurrentAccount()
        return mAccountCache?: throw IllegalStateException("Accountが取得できませんでした。")
    }



    private fun Account.getMisskeyAPI(): MisskeyAPI {
        return miCore.getMisskeyAPI(this)
    }

    private fun Account.getPagedStore(): NotePagedStore {
        return when(pageable){
            is Pageable.Favorite -> FavoriteNotePagingStore(
                this,
                pageable,
                miCore,
                miCore.getNoteCaptureAdapter(),
                viewModelScope,
                Dispatchers.IO
            )
            else -> NoteTimelineStore(
                this,
                pageable,
                include,
                miCore,
                miCore.getNoteCaptureAdapter(),
                viewModelScope,
                Dispatchers.IO
            )
        }
    }

}