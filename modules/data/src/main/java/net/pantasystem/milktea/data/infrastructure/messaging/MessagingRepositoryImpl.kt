package net.pantasystem.milktea.data.infrastructure.messaging

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.converters.UserDTOEntityConverter
import net.pantasystem.milktea.data.infrastructure.toGroup
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.group.GroupDataSource
import net.pantasystem.milktea.model.messaging.MessageRelation
import net.pantasystem.milktea.model.messaging.MessagingRepository
import net.pantasystem.milktea.model.messaging.RequestMessageHistory
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject

class MessagingRepositoryImpl @Inject constructor(
    private val getAccount: GetAccount,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val groupDataSource: GroupDataSource,
    private val userDataSource: UserDataSource,
    private val messageAdder: MessageAdder,
    private val userDTOEntityConverter: UserDTOEntityConverter,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : MessagingRepository {

    override suspend fun findMessageSummaries(
        accountId: Long,
        isGroup: Boolean
    ): Result<List<MessageRelation>> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(accountId)
            val request = RequestMessageHistory(
                i = account.token, group = isGroup, limit = 100
            )

            val res = misskeyAPIProvider.get(account).getMessageHistory(request)
            res.throwIfHasError()
            res.body()!!.map {
                it.group?.let { groupDTO ->
                    groupDataSource.add(groupDTO.toGroup(account.accountId))
                }
                it.recipient?.let { userDTO ->
                    userDataSource.add(userDTOEntityConverter.convert(account, userDTO))
                }
                messageAdder.add(account, it)
            }
        }
    }
}