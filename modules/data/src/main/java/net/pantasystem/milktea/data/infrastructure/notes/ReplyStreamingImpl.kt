package net.pantasystem.milktea.data.infrastructure.notes

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.api_streaming.ChannelBody
import net.pantasystem.milktea.api_streaming.channel.ChannelAPI
import net.pantasystem.milktea.api_streaming.mastodon.Event
import net.pantasystem.milktea.data.streaming.ChannelAPIWithAccountProvider
import net.pantasystem.milktea.data.streaming.StreamingAPIProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.ReplyStreaming
import javax.inject.Inject

class ReplyStreamingImpl @Inject constructor(
    private val channelAPIProvider: ChannelAPIWithAccountProvider,
    private val noteDataSourceAdder: NoteDataSourceAdder,
    private val streamingAPIProvider: StreamingAPIProvider,
) : ReplyStreaming {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun connect(getAccount: suspend () -> Account): Flow<Note> {
        return flow {
            emit(getAccount())
        }.flatMapLatest { ac ->
            when(ac.instanceType) {
                Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                    requireNotNull(channelAPIProvider.get(ac)).connect(ChannelAPI.Type.Main).map {
                        it as ChannelBody.Main.Reply
                    }.map {
                        it.body
                    }.map {
                        noteDataSourceAdder.addNoteDtoToDataSource(ac, it)
                    }
                }
                Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                    requireNotNull(streamingAPIProvider.get(ac)).connectUser().mapNotNull {
                        (it as? Event.Update)?.status
                    }.filter {
                        it.inReplyToId != null
                    }.map {
                        noteDataSourceAdder.addTootStatusDtoIntoDataSource(ac, it)
                    }
                }
            }
        }
    }
}