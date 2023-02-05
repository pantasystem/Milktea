package net.pantasystem.milktea.data.infrastructure.streaming

import net.pantasystem.milktea.api_streaming.ChannelBody
import net.pantasystem.milktea.data.converters.UserDTOEntityConverter
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.user.UserDataSource

/**
 * StreamingAPIのMainイベントを各種DataSourceに適応します。
 */
class StreamingMainUserEventDispatcher(
    private val userDataSource: UserDataSource,
    private val userDTOEntityConverter: UserDTOEntityConverter,
) : StreamingMainEventDispatcher{

    override suspend fun dispatch(account: Account, mainEvent: ChannelBody.Main): Boolean {
        return (mainEvent as? ChannelBody.Main.HavingUserBody)?.let {
            userDataSource.add(userDTOEntityConverter.convert(account, mainEvent.body, true))
        } != null
    }
}