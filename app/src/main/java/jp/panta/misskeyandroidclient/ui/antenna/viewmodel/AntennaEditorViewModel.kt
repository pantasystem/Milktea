package jp.panta.misskeyandroidclient.ui.antenna.viewmodel

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.panta.misskeyandroidclient.ui.users.viewmodel.UserViewData
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.api.misskey.I
import net.pantasystem.milktea.api.misskey.list.UserListDTO
import net.pantasystem.milktea.api.misskey.v12.MisskeyAPIV12
import net.pantasystem.milktea.api.misskey.v12.antenna.AntennaQuery
import net.pantasystem.milktea.api.misskey.v12.antenna.AntennaToAdd
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.antenna.Antenna
import net.pantasystem.milktea.model.group.Group
import net.pantasystem.milktea.model.user.User
import java.util.regex.Pattern
import javax.inject.Inject



@Suppress("BlockingMethodInNonBlockingContext")
@HiltViewModel
class AntennaEditorViewModel @Inject constructor(
    val userViewDataFactory: UserViewData.Factory,
    val loggerFactory: Logger.Factory,
    val accountRepository: AccountRepository,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val encryption: Encryption,
) : ViewModel(){


    private val logger = loggerFactory.create("AntennaEditorViewModel")


    private val _antennaId = MutableStateFlow<Antenna.Id?>(null)


    private val mAntenna = MutableStateFlow<Antenna?>(null).apply {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                fetch()
            }.onFailure {
                logger.debug("アンテナ取得エラー", e = it)
            }.onSuccess {
                logger.debug("取得成功:$it")
                value = it
            }
        }
    }

    val antenna = MutableLiveData<Antenna?>().apply {
        mAntenna.filterNotNull().onEach {
            postValue(it)
        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    enum class Source(val remote: String){
        HOME("home"), ALL("all"), USERS("users"), LIST("list"), GROUP("group")
    }

    val name = MediatorLiveData<String>().apply{
        addSource(this@AntennaEditorViewModel.antenna){
            this.value = it?.name?: ""
        }
    }
    val source = MediatorLiveData<Source>().apply{
        addSource(this@AntennaEditorViewModel.antenna){ a ->
            Log.d("AntennaEditorVM", "antenna:$a")
            this.value = Source.values().firstOrNull {
                a?.src ==  it.remote
            }?: Source.ALL
        }
    }
    val keywords = MediatorLiveData<String>().apply{
        addSource(this@AntennaEditorViewModel.antenna){
            this.value = setupKeywords(it?.keywords)
        }
    }

    val excludeKeywords = MediatorLiveData<String>().apply{
        addSource(this@AntennaEditorViewModel.antenna){
            this.value = setupKeywords(it?.excludeKeywords)
        }
    }

    private val userNames = MutableStateFlow<List<String>>(emptyList()).apply {
        mAntenna.filterNotNull().onEach {
            value = it.users
        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    @ExperimentalCoroutinesApi
    private val mUsers = userNames.map { list ->
        list.map { userName: String ->
            val userNameAndHost = userName.split("@").filterNot { it == "" }
            userViewDataFactory.create(userNameAndHost[0], userNameAndHost.getOrNull(1), getAccount().accountId, viewModelScope, Dispatchers.IO)

        }
    }

    @ExperimentalCoroutinesApi
    val users = MutableStateFlow<List<UserViewData>>(emptyList()).apply {
        mUsers.onEach { list ->
            value = list
        }.launchIn(viewModelScope + Dispatchers.IO)
    }


    val userListList = MediatorLiveData<List<UserListDTO>>().apply{
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val account = getAccount()
                misskeyAPIProvider.get(account).userList(
                    I(
                        account.getI(encryption)
                    )
                ).body()
            }.onSuccess {
                postValue(it)
            }
        }
    }


    val userList = MediatorLiveData<UserListDTO?>().apply{
        addSource(userListList){ list ->
            this.value = list.firstOrNull {
                it.id == this@AntennaEditorViewModel.antenna.value?.userListId
            }?: list.firstOrNull()
        }
        addSource(antenna) { antenna ->
            this.value = this@AntennaEditorViewModel.userListList.value?.firstOrNull {
                it.id == antenna?.userListId
            }
        }
    }

    val groupList = MediatorLiveData<List<Group>?>().apply{
        addSource(this@AntennaEditorViewModel.source){
            /*if(it == Source.GROUP && this.value.isNullOrEmpty()){
                miCore.getMisskeyAPI(accountRelation)
            }*/
        }
    }

    val group = MediatorLiveData<Group>().apply{
        addSource(groupList){
            this.value = it?.firstOrNull { g ->
                g.id == this@AntennaEditorViewModel.antenna.value?.userGroupId
            }?: it?.firstOrNull()
        }
    }

    /**
     * 新しいノートを通知します
     */
    val notify = MediatorLiveData<Boolean?>().apply{
        addSource(this@AntennaEditorViewModel.antenna){
            this.value = it?.notify?: false
        }
    }

    /**
     * ファイルが添付されたノートのみ
     */
    val withFile =  MediatorLiveData<Boolean?>().apply{
        addSource(this@AntennaEditorViewModel.antenna){
            this.value = it?.withFile?: false
        }
    }

    /**
     * 大文字と小文字を区別します
     */
    val caseSensitive =  MediatorLiveData<Boolean?>().apply{
        addSource(this@AntennaEditorViewModel.antenna){
            this.value = it?.caseSensitive?: false
        }
    }

    /**
     * 返信を含める
     */
    val withReplies = MediatorLiveData<Boolean>().apply{
        addSource(this@AntennaEditorViewModel.antenna){
            this.value = it?.withReplies?: false
        }
    }

    val antennaAddedStateEvent = EventBus<Boolean>()
    
    fun createOrUpdate(){

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val account = getAccount()
                val api = misskeyAPIProvider.get(account) as MisskeyAPIV12
                val antenna = mAntenna.value
                val request = AntennaToAdd(
                    account.getI(encryption),
                    _antennaId.value?.antennaId,
                    name.value ?: antenna?.name ?: "",
                    source.value?.remote!!,
                    userList.value?.id,
                    null,
                    toListKeywords(keywords.value ?: ""),
                    toListKeywords(excludeKeywords.value ?: ""),
                    userNames.value,
                    caseSensitive.value ?: false,
                    withFile.value ?: false,
                    withReplies.value ?: false,
                    notify.value ?: false,

                    )
                val res = if(_antennaId.value == null) {
                    api.createAntenna(request)
                }else{
                    api.updateAntenna(request)
                }

                res.body()?.toEntity(account)
            }.onSuccess {
                withContext(Dispatchers.Main) {
                    antennaAddedStateEvent.event = it != null
                    mAntenna.value = it
                }

            }.onFailure {
                withContext(Dispatchers.Main) {
                    antennaAddedStateEvent.event = false
                }
            }


        }

    }

    val antennaRemovedEvent = EventBus<Unit>()

    fun removeRemote(){
        val antennaId = _antennaId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val account = getAccount()
                (misskeyAPIProvider.get(account) as MisskeyAPIV12).deleteAntenna(
                    AntennaQuery(
                        antennaId = antennaId.antennaId,
                        i = account.getI(encryption),
                        limit = null
                    )
                )
            }.onSuccess {
                if(it.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        antennaRemovedEvent.event = Unit
                    }
                }
            }
        }
    }
    val selectUserEvent = EventBus<List<User.Id>>()
    @ExperimentalCoroutinesApi
    fun selectUser(){
        selectUserEvent.event = users.value.mapNotNull {
            it.user.value?.id
        }
    }

    
    private fun setupKeywords(keywords: List<List<String>>?): String{
        val builder = StringBuilder()
        keywords?.forEach {  list ->
            list.forEach {  word ->
                builder.append(word)
            }
            builder.append("\n")
        }
        return builder.toString()
    }

    private fun toListKeywords(keywords: String): List<List<String>>{
        return keywords.split('\n').map{
            it.split(Pattern.compile("""[ 　]""")).filter{ str ->
                str.isNotEmpty()

            }
        }.filter{ list ->
            list.isNotEmpty()
        }
    }

    fun setUserNames(userNames: List<String>){
        logger.debug("setUserNames: $userNames")
        this.userNames.value = userNames
    }

    fun setAntennaId(antennaId: Antenna.Id) {
        _antennaId.value = antennaId
    }

    private suspend fun fetch(): Antenna? {
        return _antennaId.value?.let{ antennaId ->
            val account = accountRepository.get(antennaId.accountId).getOrThrow()
            val api = misskeyAPIProvider.get(account) as? MisskeyAPIV12
                ?: return null
            val res = api.showAntenna(
                AntennaQuery(
                    i = account.getI(
                        encryption
                    ), antennaId = antennaId.antennaId, limit = null
                )
            )
            res.throwIfHasError()
            res.body()?.toEntity(account)

        }
    }

    private var mAccount: Account? = null
    private suspend fun getAccount(): Account {
        if(mAccount == null) {
            mAccount = _antennaId.value?.let{
                accountRepository.get(it.accountId).getOrThrow()
            }?: accountRepository.getCurrentAccount().getOrThrow()
        }
        require(mAccount != null)
        return mAccount!!
    }
}