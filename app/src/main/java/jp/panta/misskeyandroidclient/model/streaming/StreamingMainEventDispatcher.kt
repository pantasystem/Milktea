package jp.panta.misskeyandroidclient.model.streaming

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.streaming.ChannelBody

interface StreamingMainEventDispatcher {

    suspend fun dispatch(account: Account, mainEvent: ChannelBody.Main): Boolean
}