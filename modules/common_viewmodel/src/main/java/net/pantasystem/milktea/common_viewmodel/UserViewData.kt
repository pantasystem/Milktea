package net.pantasystem.milktea.common_viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

open class UserViewData(
    val userId: User.Id?,
    val userName: String? = null,
    val host: String? = null,
    val accountId: Long,
    val userDataSource: UserDataSource,
    val userRepository: UserRepository,
    val accountRepository: AccountRepository,
    val logger: Logger,
    coroutineScope: CoroutineScope,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    @Singleton
    class Factory @Inject constructor(
        val userRepository: UserRepository,
        val userDataSource: UserDataSource,
        val accountRepository: AccountRepository,
        val logger: Logger.Factory,
    ) {


        private val l = logger.create("UserViewData")

        fun create(
            userId: User.Id,
            coroutineScope: CoroutineScope,
            dispatcher: CoroutineDispatcher = Dispatchers.IO
        ): UserViewData {
            return UserViewData(
                userId,
                userDataSource,
                userRepository,
                accountRepository,
                l,
                coroutineScope,
                dispatcher,
            )

        }

        fun create(
            userName: String,
            host: String?,
            accountId: Long,
            coroutineScope: CoroutineScope,
            dispatcher: CoroutineDispatcher = Dispatchers.IO
        ): UserViewData {
            return UserViewData(
                userName,
                host,
                accountId,
                userDataSource,
                userRepository,
                accountRepository,
                l,
                coroutineScope,
                dispatcher
            )
        }

        fun create(
            user: User,
            coroutineScope: CoroutineScope,
            dispatcher: CoroutineDispatcher = Dispatchers.IO
        ): UserViewData {
            return UserViewData(
                user,
                userDataSource,
                userRepository,
                accountRepository,
                l,
                coroutineScope,
                dispatcher,
            )
        }
    }


    val user: LiveData<User.Detail?> = if (userId != null) {
        userDataSource.observe(userId)
    } else {
        require(userName != null)
        userDataSource.observe(userName = userName, host = host, accountId = accountId)
    }.filterNotNull().map {
        it as? User.Detail
    }.asLiveData()

    @OptIn(FlowPreview::class)
    val account: LiveData<Account?> = suspend {
        accountRepository.get(accountId).getOrNull()
    }.asFlow().asLiveData()

    constructor(
        user: User,
        userDataSource: UserDataSource,
        userRepository: UserRepository,
        accountRepository: AccountRepository,
        logger: Logger,
        coroutineScope: CoroutineScope,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) : this(
        user.id,
        userDataSource,
        userRepository,
        accountRepository,
        logger,
        coroutineScope = coroutineScope,
        dispatcher = dispatcher
    )

    constructor(
        userName: String,
        host: String?,
        accountId: Long,
        userDataSource: UserDataSource,
        userRepository: UserRepository,
        accountRepository: AccountRepository,
        logger: Logger,
        coroutineScope: CoroutineScope,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) : this(
        null,
        userName,
        host,
        accountId,
        userDataSource,
        userRepository,
        accountRepository = accountRepository,
        logger,
        coroutineScope,
        dispatcher
    )

    constructor(
        userId: User.Id,
        userDataSource: UserDataSource,
        userRepository: UserRepository,
        accountRepository: AccountRepository,
        logger: Logger,
        coroutineScope: CoroutineScope,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) : this(
        userId,
        null,
        null,
        userId.accountId,
        userDataSource,
        userRepository,
        accountRepository = accountRepository,
        logger,
        coroutineScope,
        dispatcher
    )

    init {


        coroutineScope.launch(dispatcher) {
            initLoad()
        }

    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun initLoad() {

        if (user.value == null) {
            runCancellableCatching {
                if (userId == null) {
                    require(userName != null)
                    userRepository.findByUserName(accountId, userName, host)
                } else {
                    userRepository.find(userId, true)
                }
            }.onFailure {
                logger.debug("取得エラー", e = it)
            }.getOrNull()

        }
    }

}
