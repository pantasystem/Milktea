package net.pantasystem.milktea.data.model.streaming

import jp.panta.misskeyandroidclient.api.misskey.users.toUser
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import jp.panta.misskeyandroidclient.streaming.ChannelBody

/**
 * StreamingAPIのMainイベントを各種DataSourceに適応します。
 */
class StreamingMainUserEventDispatcher(
    private val userDataSource: UserDataSource,
) : StreamingMainEventDispatcher{

    override suspend fun dispatch(account: Account, mainEvent: ChannelBody.Main): Boolean {
        return (mainEvent as? ChannelBody.Main.HavingUserBody)?.let {
            userDataSource.add(mainEvent.body.toUser(account, true))
        } != null
    }
}