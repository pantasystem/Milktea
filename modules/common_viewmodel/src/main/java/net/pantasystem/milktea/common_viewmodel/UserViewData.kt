package net.pantasystem.milktea.common_viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

open class UserViewData(
    val userId: User.Id?,
    val userName: String? = null,
    val host: String? = null,
    val accountId: Long? = null,
    val userDataSource: UserDataSource,
    val userRepository: UserRepository,
    val logger: Logger,
    coroutineScope: CoroutineScope,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    @Singleton
    class Factory @Inject constructor(
        val userRepository: UserRepository,
        val userDataSource: UserDataSource,
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

    constructor(
        user: User,
        userDataSource: UserDataSource,
        userRepository: UserRepository,
        logger: Logger,
        coroutineScope: CoroutineScope,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) : this(
        user.id,
        userDataSource,
        userRepository,
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
        logger,
        coroutineScope,
        dispatcher
    )

    constructor(
        userId: User.Id,
        userDataSource: UserDataSource,
        userRepository: UserRepository,
        logger: Logger,
        coroutineScope: CoroutineScope,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) : this(userId, null, null, null, userDataSource, userRepository, logger, coroutineScope, dispatcher)

    init {


        coroutineScope.launch(dispatcher) {
            initLoad()
        }

    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun initLoad() {

        if (user.value == null) {
            runCatching {
                if (userId == null) {
                    require(accountId != null)
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
