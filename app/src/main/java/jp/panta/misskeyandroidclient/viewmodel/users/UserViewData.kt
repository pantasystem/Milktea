package jp.panta.misskeyandroidclient.viewmodel.users

import android.service.autofill.UserData
import android.util.Log
import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.api.notes.toEntities
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
open class UserViewData(
    val userId: User.Id?,
    val userName: String? = null,
    val host: String? = null,
    val accountId: Long? = null,
    val miCore: MiCore,
    coroutineScope: CoroutineScope,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
){


    val user: MutableLiveData<User.Detail?> = MutableLiveData<User.Detail?>()
    private val userFlow = MutableStateFlow<User.Detail?>(null)

    private val logger = miCore.loggerFactory.create("UserViewData")

    private var mUser: User.Detail? = null
        set(value) {
            field = value
            userFlow.value = value
            user.postValue(value)
        }

    constructor(
        user: User,
        miCore: MiCore,
        coroutineScope: CoroutineScope,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): this(user.id, miCore = miCore, coroutineScope = coroutineScope, dispatcher = dispatcher)

    constructor(
        userName: String,
        host: String?,
        accountId: Long,
        miCore: MiCore,
        coroutineScope: CoroutineScope,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) : this(null, userName, host, accountId, miCore ,coroutineScope, dispatcher)

    constructor(
        userId: User.Id,
        miCore: MiCore,
        coroutineScope: CoroutineScope,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) : this(userId, null, null, null, miCore, coroutineScope, dispatcher)

    init {
        userFlow.filterNotNull().flatMapMerge { user ->
            miCore.getUserRepositoryEventToFlow().from(user.id)
        }.mapNotNull {
            (it as? UserDataSource.Event.Created)?.user
                ?: (it as? UserDataSource.Event.Updated)?.user
        }.mapNotNull {
            it as? User.Detail
        }.onEach {
            mUser = it
        }.catch { e ->
            logger.debug("ユーザー状態キャプチャー中にエラー発生", e = e)
        }.launchIn(coroutineScope + dispatcher)

        coroutineScope.launch(dispatcher) {
            initLoad()
        }

    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun initLoad() {

        if(user.value == null){
            logger.debug("User読み込み開始")
            val u : User.Detail? = runCatching {
                val u = if(userId == null) {
                    require(accountId != null)
                    require(userName != null)
                    miCore.getUserRepository().findByUserName(accountId, userName, host)
                }else{
                    miCore.getUserRepository().find(userId, true)
                }
                u as User.Detail
            }.onFailure {
                logger.debug("取得エラー", e = it)
            }.getOrNull()

            u?.let{
                mUser = it
            }
        }
    }



}