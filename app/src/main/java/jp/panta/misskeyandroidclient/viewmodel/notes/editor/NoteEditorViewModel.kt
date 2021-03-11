package jp.panta.misskeyandroidclient.viewmodel.notes.editor

import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNoteDao
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.model.notes.*
import jp.panta.misskeyandroidclient.model.notes.poll.CreatePoll
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.poll.PollEditor
import jp.panta.misskeyandroidclient.viewmodel.users.UserViewData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.io.IOException
import java.lang.NullPointerException
import java.util.*
import kotlin.collections.ArrayList

class NoteEditorViewModel(
    //private val accountRelation: AccountRelation,
    //private val misskeyAPI: MisskeyAPI,
    private val miCore: MiCore,
    private val draftNoteDao: DraftNoteDao,
    //meta: Meta,
    replyId: String? = null,
    private val quoteToNoteId: String? = null,
    private val encryption: Encryption = miCore.getEncryption(),
    private val loggerFactory: Logger.Factory,
    n: Note? = null,
    dn: DraftNote? = null,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel(){

    private val logger = loggerFactory.create("NoteEditorViewModel")

    private val currentAccount = MutableLiveData<Account>().apply{
        miCore.getCurrentAccount().onEach {
            this.postValue(it)
        }.launchIn(viewModelScope + dispatcher)
    }

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
    val currentUser: LiveData<UserViewData> = mCurrentUser

    val draftNote = MutableLiveData<DraftNote>(dn)
    val note = MutableLiveData<Note>(n)

    // FIXME Note.Idを使用するようにすること
    val replyToNoteId = MutableLiveData<String>(replyId)

    // FIXME Note.Idを使用するようにすること
    val renoteId = MutableLiveData<String>(quoteToNoteId)

    val cw = MediatorLiveData<String>().apply {
        addSourceFromNoteAndDraft { noteDTO, draftNote ->
            value = noteDTO?.cw ?: draftNote?.cw
        }
    }


    val hasCw = MediatorLiveData<Boolean>().apply {
        addSource(cw){
            value = !it.isNullOrBlank()
        }
    }
    val text = MediatorLiveData<String>().apply {
        addSourceFromNoteAndDraft { noteDTO, draftNote ->
            value = noteDTO?.text ?: draftNote?.text
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
            n?.files?.map{
                it.toFile(getInstanceBaseUrl()?: "")
            }?:dn?.files?: emptyList<File>()
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
        addSourceFromNoteAndDraft { noteDTO, draftNote ->
            val type = noteDTO?.visibility?: draftNote?.visibility
            val local = noteDTO?.localOnly ?: draftNote?.localOnly
            value = if(type == null) Visibility.Public(false) else Visibility(type, local?: false)
        }
    }

    val isLocalOnly = MediatorLiveData<Boolean>().apply{
        addSource(visibility){
            value = it.isLocalOnly()
        }
    }

    val isLocalOnlyEnabled = MediatorLiveData<Boolean>().apply{
        addSource(visibility){
            value = it is CanLocalOnly
        }
    }

    val showVisibilitySelectionEvent = EventBus<Unit>()
    val visibilitySelectedEvent = EventBus<Unit>()

    val address = MutableLiveData(
        n?.visibleUserIds?.map(::setUpUserViewData)
            ?: dn?.visibleUserIds?.mapNotNull {
            miCore.getCurrentAccount().value?.accountId?.let { ac ->
                User.Id(ac, it)
            }
        }?.map(::setUpUserViewData) ?: emptyList()
    )


    private fun setUpUserViewData(userId: User.Id) : UserViewData{
        return UserViewData(userId, miCore, viewModelScope, dispatcher)
    }

    val isSpecified = Transformations.map(visibility){
        it is Visibility.Specified
    }

    val poll = MutableLiveData<PollEditor?>(
        n?.poll?.let{
            PollEditor(it)
        }?: dn?.draftPoll?.let{
            PollEditor(it)
        }
    )

    val noteTask = MutableLiveData<PostNoteTask>()

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
            val rtn = replyToNoteId.value
            val replyId = if(rtn == null) null else Note.Id(noteId = rtn, accountId = account.accountId)

            val rnn = renoteId.value
            val renoteId = if(rnn == null) null else Note.Id(noteId = rnn, accountId = account.accountId)
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
                poll = poll.value?.buildCreatePoll()
            )
            val noteTask = PostNoteTask(encryption, createNote, draftNote.value, account, loggerFactory)

            // FIXME Model層に依頼すべきだがServiceを呼び出したいがためにViewへ通知してしまっている
            this.noteTask.postValue(noteTask)
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
        add(fp.toFile(getInstanceBaseUrl()?: ""))
    }



    fun addAllFileProperty(fpList: List<FileProperty>){
        val files = files.value.toArrayList()
        files.addAll(fpList.map{
            it.toFile(getInstanceBaseUrl()?: "")
        })
        this.files.value = files
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
        this.visibility.value = visibility
        this.visibilitySelectedEvent.event = Unit
    }

    fun toggleLocalOnly() {
        if(this.isLocalOnlyEnabled.value == true){
            this.isLocalOnly.value = !(this.isLocalOnly.value?: false)
        }
    }


    private fun List<File>?.toArrayList(): ArrayList<File>{
        return if(this == null){
            ArrayList<File>()
        }else{
            ArrayList<File>(this)
        }
    }

    fun setAddress(added: Array<User.Id>, removed: Array<User.Id>){
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


    fun addMentionUsers(users: List<UserDTO>, pos: Int): Int{
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


    fun toDraftNote(): DraftNote{
        return DraftNote(
            accountId = currentAccount.value?.accountId!!,
            text = text.value,
            cw = cw.value,
            visibleUserIds = address.value?.map{
                it.userId.id
            },
            draftPoll = poll.value?.toDraftPoll(),
            visibility = visibility.value?.type()?: "public",
            localOnly = visibility.value?.isLocalOnly(),
            renoteId = quoteToNoteId,
            replyId = replyToNoteId.value,
            files = files.value
        ).apply{
            this.draftNoteId = draftNote.value?.draftNoteId
        }
    }
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

    private fun<T> MediatorLiveData<T>.addSourceFromNoteAndDraft(observer: (Note?, DraftNote?)->Unit) {
        addSource(note) {
            observer(it, draftNote.value)
        }

        addSource(draftNote) {
            observer(note.value, it)
        }
    }


}