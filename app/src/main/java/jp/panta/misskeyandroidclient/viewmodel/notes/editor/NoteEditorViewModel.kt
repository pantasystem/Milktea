package jp.panta.misskeyandroidclient.viewmodel.notes.editor

import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.model.notes.*
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNoteDao
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.users.UserViewData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class NoteEditorViewModel(
    private val miCore: MiCore,
    private val draftNoteDao: DraftNoteDao,
    replyId: Note.Id? = null,
    private val quoteToNoteId: Note.Id? = null,
    loggerFactory: Logger.Factory,
    dn: DraftNote? = null,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel(){

    private val logger = loggerFactory.create("NoteEditorViewModel")

    private val _state = MutableStateFlow(
        dn?.toNoteEditingState()
            ?: NoteEditingState(
                renoteId = quoteToNoteId,
                replyId = replyId
            )
    )
    val state: StateFlow<NoteEditingState> = _state

    val text = _state.map {
        it.text
    }.stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = null)
    val cw = _state.map {
        it.cw
    }.stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = null)
    //val text = MutableStateFlow("")
    //val cw = MutableStateFlow("")

    private val currentAccount = MutableLiveData<Account>().apply{
        miCore.getCurrentAccount().onEach {
            this.postValue(it)
        }.launchIn(viewModelScope + dispatcher)
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    val currentUser: LiveData<UserViewData> = miCore.getCurrentAccount().filterNotNull().map {
        val userId = User.Id(it.accountId, it.remoteId)
        UserViewData(
            userId,
            miCore,
            viewModelScope,
            dispatcher
        )
    }.asLiveData(Dispatchers.IO)

    val draftNote = MutableLiveData<DraftNote>(dn)

    //val replyToNoteId = MutableLiveData<Note.Id>(replyId)
    val reply = _state.map {
        it.replyId?.let { noteId ->
            runCatching {
                miCore.getNoteRepository().find(noteId)
            }.getOrNull()
        }
    }.stateIn(viewModelScope + Dispatchers.IO, started = SharingStarted.Lazily, initialValue = null)




    val renoteId = _state.map {
        it.renoteId
    }.asLiveData()



    val hasCw = _state.map {
        it.hasCw
    }.asLiveData()

    /*val text = MediatorLiveData<String>().apply {

        addSource(draftNote) {
            if(it?.text != null) {
                value = it.text
            }
        }
    }*/


    var maxTextLength = miCore.getCurrentAccount().map {
        miCore.getCurrentInstanceMeta()?.maxNoteTextLength?: 1500
    }.stateIn(viewModelScope + Dispatchers.IO, started = SharingStarted.Lazily, initialValue = 1500)

    /*val textRemaining = Transformations.map(text){ t: String? ->
        (maxTextLength.value?: 1500) - (t?.codePointCount(0, t.length)?: 0)
    }*/
    /*val textRemaining = text.map {
        (maxTextLength.value?: 1500) - (it.codePointCount(0, it.length))
    }*/
    /*val textRemaining = MediatorLiveData<Int>().apply{
        addSourceChain(maxTextLength){ maxSize ->
            val t = text.value
            value = (maxSize?: 1500) - (t?.codePointCount(0, t.length)?: 0)
        }
        addSource(text){ t ->
            val max = maxTextLength.value?: 1500
            value = max - (t?.codePointCount(0, t.length)?: 0)
        }
    }*/

    val textRemaining = combine(maxTextLength, text) { max, t ->
        max - (t?.codePointCount(0, t.length)?:0)
    }.stateIn(viewModelScope + Dispatchers.IO, started = SharingStarted.Lazily, initialValue = 1500)




    val files = _state.map {
        it.files
    }.asLiveData()

    val totalImageCount = _state.map {
        it.totalFilesCount
    }.asLiveData()


    val isPostAvailable = _state.map {
        it.checkValidate(textMaxLength = maxTextLength.value)
    }.asLiveData()

    // TODO: stateのVisiblityをベースにする
    val visibility = MediatorLiveData<Visibility>().apply {

        addSource(draftNote) {
            if(it != null) {
                val local = it.localOnly
                val type = it.visibility
                val visibleUserIds = it.visibleUserIds
                value = Visibility(type, local ?: false, visibleUserIds = visibleUserIds?.map { userId ->
                    User.Id(it.accountId, userId)
                })
            }
        }

        reply.filterNotNull().onEach {
            val visibility = if(it.visibility is Visibility.Specified) Visibility.Specified(listOf(it.userId)) else it.visibility
            postValue(visibility)
        }.launchIn(viewModelScope + Dispatchers.IO)


        if(replyId == null || renoteId.value == null) {
            miCore.getCurrentAccount().filterNotNull().onEach {
                this.value = miCore.getSettingStore().getNoteVisibility(it.accountId)
                logger.debug("公開範囲:${this.value}")
            }.launchIn(viewModelScope)
        }
    }

    val isLocalOnly = Transformations.map(visibility) {
        it.isLocalOnly()
    }


    val isLocalOnlyEnabled = MediatorLiveData<Boolean>().apply{
        addSource(visibility){
            value = it is CanLocalOnly
        }
    }



    val showVisibilitySelectionEvent = EventBus<Unit>()
    val visibilitySelectedEvent = EventBus<Unit>()


    @FlowPreview
    @ExperimentalCoroutinesApi
    val address = visibility.map {
        it as? Visibility.Specified
    }.map {
        it?.visibleUserIds?.map { uId ->
            setUpUserViewData(uId)
        }?: emptyList()
    }.asFlow().asLiveData(viewModelScope.coroutineContext)


    @FlowPreview
    @ExperimentalCoroutinesApi
    private fun setUpUserViewData(userId: User.Id) : UserViewData{
        return UserViewData(userId, miCore, viewModelScope, dispatcher)
    }

    val isSpecified = Transformations.map(visibility){
        it is Visibility.Specified
    }

    /*val poll = MutableLiveData<PollEditor?>(
        dn?.draftPoll?.let{
            PollEditor(it)
        }
    )*/

    val poll = _state.map {
        it.poll
    }.distinctUntilChanged().stateIn(viewModelScope, started = SharingStarted.Lazily, initialValue = null)
    //val noteTask = MutableLiveData<PostNoteTask>()
    val isPost = EventBus<Boolean>()

    val showPollDatePicker = EventBus<Unit>()
    val showPollTimePicker = EventBus<Unit>()



    val isSaveNoteAsDraft = EventBus<Long?>()
    init{
        currentAccount.observeForever {
            miCore.getCurrentInstanceMeta()
        }

        miCore.getCurrentAccount().filterNotNull().onEach {
            _state.value = runCatching {
                _state.value.setAccount(it)
            }.getOrElse {
                NoteEditingState()
            }
        }.launchIn(viewModelScope + Dispatchers.IO)

    }

    fun changeText(text: String) {
        _state.value = _state.value.changeText(text)
    }

    fun addPollChoice() {
        _state.value = _state.value.addPollChoice()
    }
    
    fun changePollChoice(id: UUID, text: String) {
        _state.value = _state.value.updatePollChoice(id, text)
    }
    
    fun removePollChoice(id: UUID) {
        _state.value = _state.value.removePollChoice(id)
    }

    fun updateState(state: NoteEditingState) {
        _state.value = state
    }

    fun togglePollMultiple() {
        _state.value = state.value.copy(
            poll = state.value.poll?.toggleMultiple()
        )
    }

    fun post(){
        currentAccount.value?.let{ account ->

            // FIXME 本来はreplyToNoteIdの時点でNote.Idを使うべきだが現状は厳しい
            val replyId = _state.value.replyId

            val renoteId = renoteId.value
            // FIXME viaMobileを設定できるようにする
            val createNote = CreateNote(
                author = account,
                visibility = _state.value.visibility,
                text = text.value,
                cw = cw.value,
                viaMobile = false,
                files = files.value,
                replyId = replyId,
                renoteId = renoteId,
                poll = poll.value?.toCreatePoll(),
                draftNoteId = draftNote.value?.draftNoteId
            )

            miCore.getTaskExecutor().dispatch(createNote.task(miCore.getNoteRepository()))


            this.isPost.event = true
        }

    }

    fun add(file: File){
        val files = files.value.toArrayList()
        files.add(
            file
        )
        _state.value = _state.value.addFile(file)
    }

    fun add(fp: FileProperty){
        add(fp.toFile())
    }



    private fun addAllFileProperty(fpList: List<FileProperty>){
        val files = files.value.toArrayList()
        files.addAll(fpList.map{
            it.toFile()
        })
        _state.value = _state.value.copy(
            files = files
        )
    }

    fun addFilePropertyFromIds(ids: List<FileProperty.Id>) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                miCore.getFilePropertyDataSource().findIn(ids)
            }.onSuccess {
                addAllFileProperty(it)
            }
        }
    }

    fun removeFileNoteEditorData(file: File){
        _state.value = _state.value.removeFile(file)
    }



    fun fileTotal(): Int{
        return files.value?.size?: 0
    }


    fun changeCwEnabled(){
        _state.value = _state.value.toggleCw()
        logger.debug("cw:${cw.value}")
    }

    fun enablePoll(){
        _state.value = _state.value.togglePoll()

    }

    fun disablePoll(){
        _state.value = _state.value.togglePoll()
    }

    fun showVisibilitySelection(){
        showVisibilitySelectionEvent.event = Unit
    }

    fun setText(text: String) {
        _state.value = _state.value.changeText(text)
    }

    fun setCw(text: String?) {
        _state.value = _state.value.changeCw(text)
    }

    fun setVisibility(visibility: Visibility){
        logger.debug("公開範囲がセットされた:$visibility")
        this.visibility.value = visibility
        this.visibilitySelectedEvent.event = Unit
    }


    private fun List<File>?.toArrayList(): ArrayList<File>{
        return if(this == null){
            ArrayList<File>()
        }else{
            ArrayList<File>(this)
        }
    }


    @FlowPreview
    @ExperimentalCoroutinesApi
    fun setAddress(added: List<User.Id>, removed: List<User.Id>){
        /*val list = address.value?.let{
            ArrayList(it)
        }?: ArrayList()*/
        val list = ((visibility.value as? Visibility.Specified)?.visibleUserIds?: emptyList()).toMutableList()

        list.addAll(
            added
        )

        list.removeAll {
            removed.any()
        }

        visibility.value = Visibility.Specified(
            list
        )
    }


    fun addMentionUsers(users: List<User>, pos: Int): Int{
        val mentionBuilder = StringBuilder()
        users.forEachIndexed { index, it ->
            val userName = it.getDisplayUserName()
            if(index < users.size - 1){
                mentionBuilder.appendLine(userName)
            }else{
                mentionBuilder.append(userName)
            }
        }
        val builder = StringBuilder(text.value?: "")
        builder.insert(pos, mentionBuilder.toString())
        _state.value = _state.value.changeText(builder.toString())
        return pos + mentionBuilder.length
    }

    fun addEmoji(emoji: Emoji, pos: Int): Int{
        return addEmoji(":${emoji.name}:", pos)
    }

    fun addEmoji(emoji: String, pos: Int): Int{
        val builder = StringBuilder(_state.value.text?: "")
        builder.insert(pos, emoji)
        _state.value = _state.value.changeText(builder.toString())
        logger.debug( "position:${pos + emoji.length - 1}")
        return pos + emoji.length
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    fun toDraftNote(): DraftNote{
        return DraftNote(
            accountId = currentAccount.value?.accountId!!,
            text = _state.value.text,
            cw = _state.value.cw,
            visibleUserIds = address.value?.mapNotNull {
                it.userId?.id ?: it.user.value?.id?.id
            },
            draftPoll = poll.value?.toDraftPoll(),
            visibility = visibility.value?.type()?: "public",
            localOnly = visibility.value?.isLocalOnly(),
            renoteId = quoteToNoteId?.noteId,
            replyId = _state.value.replyId?.noteId,
            files = files.value
        ).apply{
            this.draftNoteId = draftNote.value?.draftNoteId
        }
    }
    @ExperimentalCoroutinesApi
    @FlowPreview
    fun saveDraft(){
        if(!canSaveDraft()){
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try{
                val dfNote = toDraftNote()

                try{
                    isSaveNoteAsDraft.event = draftNoteDao.fullInsert(dfNote)
                }catch(e: Exception){
                    logger.error( "下書き書き込み中にエラー発生：失敗してしまった", e)
                }
            }catch(e: IOException){

            }catch(e: NullPointerException){
                logger.error( "下書き保存に失敗した", e)

            }catch (e: Throwable){
                logger.error( "下書き保存に失敗した", e)

            }

        }
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    fun canSaveDraft(): Boolean{
        return !(_state.value.text.isNullOrBlank()
                && files.value.isNullOrEmpty()
                && poll.value?.choices.isNullOrEmpty()
                && address.value.isNullOrEmpty())
    }

    private fun <T, S>MediatorLiveData<T>.addSourceChain(liveData: LiveData<S>, observer: (out: S)-> Unit): MediatorLiveData<T>{
        this.addSource(liveData, observer)
        return this
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    fun clear(){
        _state.value = _state.value.clear()
    }




}