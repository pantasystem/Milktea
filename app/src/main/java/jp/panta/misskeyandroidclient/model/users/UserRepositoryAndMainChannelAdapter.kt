package jp.panta.misskeyandroidclient.model.users

import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.streaming.ChannelBody
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPI
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPIWithAccountProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


/**
 * ChannelAPIのMainのイベントをUserRepositoryへ適応するAdapter
 */
class UserRepositoryAndMainChannelAdapter(
    private val userRepository: UserRepository,
    private val channelAPIWithAccountProvider: ChannelAPIWithAccountProvider,
    private val coroutineScope: CoroutineScope,
    private val coroutineDispatcher: CoroutineDispatcher
) {

    private val accountAndJob = mutableMapOf<Long, Job>()

    fun listen(account: Account) {
        synchronized(accountAndJob) {
            if(accountAndJob[account.accountId] != null) {
                return
            }
            accountAndJob[account.accountId] = channelAPIWithAccountProvider.get(account).connect(ChannelAPI.Type.MAIN).map{
                it as? ChannelBody.Main.HavingUserBody
            }.filterNotNull().onEach {
                userRepository.add(it.body.toUser(account, true))
            }.launchIn(coroutineScope + coroutineDispatcher)
        }
    }

    fun unListen(account: Account) {
        synchronized(accountAndJob) {
            accountAndJob.remove(account.accountId)?.cancel()
        }
    }
}