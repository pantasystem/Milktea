package net.pantasystem.milktea.common_viewmodel.viewmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common_viewmodel.UserViewData
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

class AccountViewData(
    val account: Account,
    coroutineScope: CoroutineScope,
    logger: Logger,
    userRepository: UserRepository,
    userDataSource: UserDataSource,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserViewData(
    User.Id(account.accountId, account.remoteId),
    userDataSource,
    userRepository,
    logger,
    coroutineScope,
    coroutineDispatcher
) {
    @Singleton
    class Factory @Inject constructor(
        val userDataSource: UserDataSource,
        val userRepository: UserRepository,
        val loggerFactory: Logger.Factory
    ) {

        fun create(
            account: Account,
            coroutineScope: CoroutineScope
        ): AccountViewData {
            return AccountViewData(
                account,
                coroutineScope,
                loggerFactory.create("AccountViewData"),
                userDataSource = userDataSource,
                userRepository = userRepository
            )
        }
    }
}