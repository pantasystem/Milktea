package net.pantasystem.milktea.common_viewmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

class UserViewData private constructor(
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
        @IODispatcher val defaultDispatcher: CoroutineDispatcher,
    ) {


        private val l = logger.create("UserViewData")

        fun create(
            userId: User.Id,
            coroutineScope: CoroutineScope,
            dispatcher: CoroutineDispatcher = defaultDispatcher
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
            dispatcher: CoroutineDispatcher = defaultDispatcher
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
            dispatcher: CoroutineDispatcher = defaultDispatcher
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


    val user: StateFlow<User.Detail?> = if (userId != null) {
        userRepository.observe(userId)
    } else {
        require(userName != null)
        userRepository.observe(userName = userName, host = host, accountId = accountId)
    }.filterNotNull().map {
        it as? User.Detail
    }.stateIn(
        coroutineScope,
        SharingStarted.WhileSubscribed(5_000),
        null,
    )

    val account: StateFlow<Account?> = suspend {
        accountRepository.get(accountId).getOrNull()
    }.asFlow().stateIn(
        coroutineScope,
        SharingStarted.WhileSubscribed(5_000),
        null,
    )

    private constructor(
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

    private constructor(
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

    private constructor(
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
                logger.debug(e = it) { "取得エラー" }
            }.getOrNull()

        }
    }

}
