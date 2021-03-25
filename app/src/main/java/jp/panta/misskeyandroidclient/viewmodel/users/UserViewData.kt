package jp.panta.misskeyandroidclient.viewmodel.users

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

    private var mUser: User.Detail? = null
        set(value) {
            field = value
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
        }.onEach {
            when(it) {
                is UserDataSource.Event.Created -> {
                    (it.user as? User.Detail)?.let{ detail ->
                        mUser = detail
                    }
                }
                is UserDataSource.Event.Updated -> {
                    (it.user as? User.Detail)?.let { detail ->
                        mUser = detail
                    }
                }
                is UserDataSource.Event.Removed -> {
                    mUser = null
                }
            }
        }.launchIn(coroutineScope + dispatcher)

        coroutineScope.launch(dispatcher) {
            initLoad()
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun initLoad() {

        if(user.value == null){
            val u : User.Detail? = runCatching {
                if(userId == null) {
                    require(accountId != null)
                    require(userName != null)
                    miCore.getUserRepository().findByUserName(accountId, userName, host)
                }else{
                    miCore.getUserRepository().find(userId, true)
                }
            }.onFailure {
                Log.d("UserViewData", "取得エラー", it)
            }.getOrNull() as? User.Detail

            u?.let{
                mUser = it
            }
        }
    }



}