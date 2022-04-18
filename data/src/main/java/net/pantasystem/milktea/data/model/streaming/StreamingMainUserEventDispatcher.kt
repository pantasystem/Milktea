package net.pantasystem.milktea.data.model.streaming

import net.pantasystem.milktea.data.api.misskey.users.toUser
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.data.streaming.ChannelBody

/**
 * StreamingAPIのMainイベントを各種DataSourceに適応します。
 */
class StreamingMainUserEventDispatcher(
    private val userDataSource: net.pantasystem.milktea.model.user.UserDataSource,
) : StreamingMainEventDispatcher{

    override suspend fun dispatch(account: net.pantasystem.milktea.model.account.Account, mainEvent: ChannelBody.Main): Boolean {
        return (mainEvent as? ChannelBody.Main.HavingUserBody)?.let {
            userDataSource.add(mainEvent.body.toUser(account, true))
        } != null
    }
}