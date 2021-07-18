package jp.panta.misskeyandroidclient.viewmodel.notes.editor

import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.api.drive.FilePropertyDTO
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.drive.filePropertyPagingStore
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.model.notes.*
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNoteDao
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.poll.PollEditor
import jp.panta.misskeyandroidclient.viewmodel.users.UserViewData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException

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

    private val currentAccount = MutableLiveData<Account>().apply{
        miCore.getCurrentAccount().onEach {
            this.postValue(it)
        }.launchIn(viewModelScope + dispatcher)
    }
    @FlowPreview
    @ExperimentalCoroutinesApi
    private val mCurrentUser = MutableLiveData<UserViewData>().apply{
        miCore.getCurrentAccount().filterNotNull().onEach {
            val userId = User.Id(it.accountId, it.remoteId)
            this.postValue(
                UserViewData(
                    userId,
                    miCore,
                    viewModelScope,
                    dispatcher

                )
            )
        }.launchIn(viewModelScope + dispatcher)
    }
    @FlowPreview
    @ExperimentalCoroutinesApi
    val currentUser: LiveData<UserViewData> = mCurrentUser

    val draftNote = MutableLiveData<DraftNote>(dn)

    //val replyToNoteId = MutableLiveData<Note.Id>(replyId)
    private val mReply = MutableStateFlow<Note?>(null)
    val reply: StateFlow<Note?> = mReply
    val replyToNoteId = MutableStateFlow(replyId).also { replyId ->
        replyId.map { id ->
            id?.let{
                miCore.getNoteRepository().find(it)
            }
        }.onEach {
            mReply.value = it
        }.launchIn(viewModelScope + Dispatchers.IO)
    }



    //val renoteId = MutableLiveData<Note.Id>(quoteToNoteId)
    val renoteId = MutableStateFlow(quoteToNoteId)

    val cw = MediatorLiveData<String>().apply {
        addSource(draftNote) {
            value = it?.cw
        }
    }


    val hasCw = MediatorLiveData<Boolean>().apply {
        addSource(cw){
            value = !it.isNullOrBlank()
        }
    }
    val text = MediatorLiveData<String>().apply {

        addSource(draftNote) {
            if(it?.text != null) {
                value = it.text
            }
        }
    }
    var maxTextLength = Transformations.map(currentAccount){
        miCore.getCurrentInstanceMeta()?.maxNoteTextLength?: 1500
    }
    /*val textRemaining = Transformations.map(text){ t: String? ->
        (maxTextLength.value?: 1500) - (t?.codePointCount(0, t.length)?: 0)
    }*/
    val textRemaining = MediatorLiveData<Int>().apply{
        addSourceChain(maxTextLength){ maxSize ->
            val t = text.value
            value = (maxSize?: 1500) - (t?.codePointCount(0, t.length)?: 0)
        }
        addSource(text){ t ->
            val max = maxTextLength.value?: 1500
            value = max - (t?.codePointCount(0, t.length)?: 0)
        }
    }


    val files = MediatorLiveData<List<File>>().apply{
        this.postValue(
            dn?.files?: emptyList<File>()
        )
    }

    val totalImageCount = MediatorLiveData<Int>().apply{

        this.addSource(files){
            logger.debug( "list$it, sizeは: ${it.size}")
            this.value = it.size
        }
    }


    val isPostAvailable = MediatorLiveData<Boolean>().apply{
        this.addSource(textRemaining){
            val totalImageTmp = totalImageCount.value
            this.value =  it in 0 until (maxTextLength.value?: 1500)
                    || (totalImageTmp != null && totalImageTmp > 0 && totalImageTmp <= 4)
                    || quoteToNoteId != null
        }
        this.addSource(totalImageCount){
            val tmpTextSize = textRemaining.value
            this.value = tmpTextSize in 0 until (maxTextLength.value?: 1500)
                    || (it != null && it > 0 && it <= 4)
                    || quoteToNoteId != null
        }
    }

    // FIXME リモートのVisibilityを参照するようにする
    val visibility = MediatorLiveData<Visibility>().apply {

        addSource(draftNote) {
            if(it != null) {
                val local = it.localOnly
                val type = it.visibility
                value = Visibility(type, local ?: false)
            }
        }

        reply.filterNotNull().onEach {
            postValue(it.visibility)
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
    val address = MutableLiveData(
        dn?.visibleUserIds?.mapNotNull {
            miCore.getCurrentAccount().value?.accountId?.let { ac ->
                User.Id(ac, it)
            }
        }?.map(::setUpUserViewData) ?: emptyList()
    )


    @FlowPreview
    @ExperimentalCoroutinesApi
    private fun setUpUserViewData(userId: User.Id) : UserViewData{
        return UserViewData(userId, miCore, viewModelScope, dispatcher)
    }

    val isSpecified = Transformations.map(visibility){
        it is Visibility.Specified
    }

    val poll = MutableLiveData<PollEditor?>(
        dn?.draftPoll?.let{
            PollEditor(it)
        }
    )

    //val noteTask = MutableLiveData<PostNoteTask>()
    val isPost = EventBus<Boolean>()

    val showPollDatePicker = EventBus<Unit>()
    val showPollTimePicker = EventBus<Unit>()



    val isSaveNoteAsDraft = EventBus<Long?>()
    init{
        currentAccount.observeForever {
            miCore.getCurrentInstanceMeta()
        }
    }

    fun post(){
        currentAccount.value?.let{ account ->

            // FIXME 本来はreplyToNoteIdの時点でNote.Idを使うべきだが現状は厳しい
            val replyId = replyToNoteId.value

            val renoteId = renoteId.value
            // FIXME viaMobileを設定できるようにする
            val createNote = CreateNote(
                author = account,
                visibility = visibility.value?: Visibility.Public(false),
                text = text.value,
                cw = cw.value,
                viaMobile = false,
                files = files.value,
                replyId = replyId,
                renoteId = renoteId,
                poll = poll.value?.buildCreatePoll(),
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
        this.files.value = files
    }

    fun add(fp: FileProperty){
        add(fp.toFile())
    }



    private fun addAllFileProperty(fpList: List<FileProperty>){
        val files = files.value.toArrayList()
        files.addAll(fpList.map{
            it.toFile()
        })
        this.files.postValue(files)
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
        val files = files.value.toArrayList()
        files.remove(file)
        this.files.value = files
    }

    fun localFileTotal(): Int{
        return files.value?.filter{
            it.remoteFileId == null
        }?.size?: 0
    }

    fun driveFileTotal(): Int{
        return files.value?.filter{
            it.remoteFileId != null
        }?.size?: 0
    }

    fun fileTotal(): Int{
        return files.value?.size?: 0
    }

    fun remoteFiles(): List<File>{
        return files.value?.filter{
            it.remoteFileId != null
        }?: emptyList()
    }



    fun changeCwEnabled(){
        hasCw.value = !(hasCw.value?: false)
        if(hasCw.value == false){
            cw.value = ""
        }
    }

    fun enablePoll(){
        val p = poll.value
        if(p == null){
            poll.value = PollEditor()
        }
    }

    fun disablePoll(){
        val p = poll.value
        if(p != null){
            poll.value = null
        }
    }

    fun showVisibilitySelection(){
        showVisibilitySelectionEvent.event = Unit
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
        val list = address.value?.let{
            ArrayList(it)
        }?: ArrayList()

        list.addAll(
            added.map(::setUpUserViewData)
        )

        list.removeAll { uv ->
            removed.any{
                uv.userId == it
            }
        }
        address.postValue(list)
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
        text.value = builder.toString()
        return pos + mentionBuilder.length
    }

    fun addEmoji(emoji: Emoji, pos: Int): Int{
        return addEmoji(":${emoji.name}:", pos)
    }

    fun addEmoji(emoji: String, pos: Int): Int{
        val builder = StringBuilder(text.value?: "")
        builder.insert(pos, emoji)
        text.value = builder.toString()
        logger.debug( "position:${pos + emoji.length - 1}")
        return pos + emoji.length
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    fun toDraftNote(): DraftNote{
        return DraftNote(
            accountId = currentAccount.value?.accountId!!,
            text = text.value,
            cw = cw.value,
            visibleUserIds = address.value?.mapNotNull {
                it.userId?.id ?: it.user.value?.id?.id
            },
            draftPoll = poll.value?.toDraftPoll(),
            visibility = visibility.value?.type()?: "public",
            localOnly = visibility.value?.isLocalOnly(),
            renoteId = quoteToNoteId?.noteId,
            replyId = replyToNoteId.value?.noteId,
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
        return !(cw.value.isNullOrBlank()
                && text.value.isNullOrBlank()
                && files.value.isNullOrEmpty()
                && poll.value?.choices?.value.isNullOrEmpty()
                && address.value.isNullOrEmpty())
    }

    private fun <T, S>MediatorLiveData<T>.addSourceChain(liveData: LiveData<S>, observer: (out: S)-> Unit): MediatorLiveData<T>{
        this.addSource(liveData, observer)
        return this
    }

    private fun getInstanceBaseUrl(): String?{
        return currentAccount.value?.instanceDomain
    }
    @FlowPreview
    @ExperimentalCoroutinesApi
    fun clear(){
        text.value = ""
        cw.value = ""
        files.value = emptyList()
        address.value = emptyList()
        poll.value = null
    }

    private fun getCurrentInformation(): Account?{
        return miCore.getCurrentAccount().value
    }


    /*private fun<T> MediatorLiveData<T>.addSourceFromNoteAndDraft(observer: (Note?, DraftNote?)->Unit) {
        addSource(note) {
            observer(it, draftNote.value)
        }

        addSource(draftNote) {
            observer(note.value, it)
        }
    }*/


}