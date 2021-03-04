package jp.panta.misskeyandroidclient.viewmodel.notes

import android.util.Log
import androidx.lifecycle.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import jp.panta.misskeyandroidclient.api.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.settings.SettingStore
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
import jp.panta.misskeyandroidclient.streaming.ChannelBody
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPI
import kotlinx.coroutines.flow.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.math.acos

@ExperimentalCoroutinesApi
class TimelineViewModel(
    val account: Account,
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

    val position = MutableLiveData<Int>()


    private var notePagingStore: NotePagedStore = when(pageable){
        is Pageable.Favorite -> FavoriteNotePagingStore(account, pageable, miCore, miCore.getNoteCaptureAdapter(),
            viewModelScope,
            Dispatchers.IO)
        else -> NoteTimelineStore(
            account,
            pageable,
            include,
            miCore,
            miCore.getNoteCaptureAdapter(),
            viewModelScope,
            Dispatchers.IO
        )
    }

    private var isInitialized: Boolean = false

    val misskeyAPI: MisskeyAPI = miCore.getMisskeyAPI(account)


    private var mNoteIds = HashSet<Note.Id>()

    private val timelineLiveData = MediatorLiveData<TimelineState>()


    val isLoading = MutableLiveData<Boolean>()
    val isInitLoading = MutableLiveData<Boolean>()

    private var isLoadingFlag = false

    init {
        val flow = when(pageable) {
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
            else -> null
        }
        flow?.map {
          it as? ChannelBody.ReceiveNote
        }?.filterNotNull()?.map{
            miCore.getGetters().noteRelationGetter.get(account, it.body)
        }?.filter {
           !mNoteIds.contains(it.note.id) && !this.isLoadingFlag
        }?.map{
            PlaneNoteViewData(it, account, DetermineTextLengthSettingStore(miCore.getSettingStore()), miCore.getNoteCaptureAdapter())
        }?.onEach {
            this.mNoteIds.add(it.id)
            captureNotes(listOf(it))
            val list = ArrayList<PlaneNoteViewData>(this.timelineLiveData.value?.notes?: emptyList<PlaneNoteViewData>())
            list.add(0, it)
            this.timelineLiveData.postValue(
                TimelineState(
                    state = TimelineState.State.RECEIVED_NEW,
                    notes = list
                )
            )
        }?.launchIn(viewModelScope)
    }




    fun getTimelineLiveData() : LiveData<TimelineState>{
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
                        captureNotes(list)
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
                    captureNotes(list)
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
                    val response = notePagingStore?.loadInit()!!
                    val list = response.second?: emptyList()
                    val state = TimelineState(
                        list,
                        TimelineState.State.INIT
                    )
                    //noteCapture?.subscribeAll(noteCaptureRegister.registerId, list)
                    captureNotes(list)

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

        val res = when {
            request?.untilId != null -> {
                notePagingStore.loadOld(untilId = request.untilId)
            }
            request?.sinceId != null -> {
                notePagingStore.loadNew(sinceId = request.sinceId)
            }
            else -> {
                notePagingStore.loadInit()
            }
        }
        val notes = res.second

        if(notes?.isNotEmpty() == true){
            return res
        }

        val targetNoteId = request?.untilId?: request?.sinceId ?: return null

        val targetNote = try{
            misskeyAPI.showNote(
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


    private fun loadUrlPreviews(list: List<PlaneNoteViewData>) {


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



    private fun captureNotes(notes: List<PlaneNoteViewData>) {
        val scope = viewModelScope + Dispatchers.IO
        viewModelScope.launch(Dispatchers.IO) {
            notes.forEach { note ->
                note.eventFlow.onEach {
                    if(it is NoteRepository.Event.Deleted) {
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



    fun TimelineState?.makeUntilIdRequest(substituteSize: Int = 2): TimelineViewModel.Request? {
        val ids = this?.getUntilIds(substituteSize)?: emptyList()
        return ids.makeUntilIdRequest()
    }

    private fun List<String>.makeUntilIdRequest(index: Int = 0): TimelineViewModel.Request? {
        if(this.size <= index) {
            return null
        }

        val now = TimelineViewModel.Request(
            untilId = this[index]
        )
        now.substitute = this.makeUntilIdRequest(index + 1)
        return now
    }

    fun TimelineState?.makeSinceIdRequest(substituteSize: Int = 2): TimelineViewModel.Request?{
        return (this?.getSinceIds(substituteSize)?: emptyList()).makeSinceIdRequest()
    }

    private fun List<String>.makeSinceIdRequest(index: Int = 0): TimelineViewModel.Request?{
        if(this.size <= index){
            return null
        }
        val now = TimelineViewModel.Request(
            sinceId = this[index]
        )
        now.substitute = makeUntilIdRequest(index + 1)
        return now
    }


}