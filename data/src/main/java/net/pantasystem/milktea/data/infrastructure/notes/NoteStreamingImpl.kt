package net.pantasystem.milktea.data.infrastructure.notes

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.data.streaming.ChannelBody
import net.pantasystem.milktea.data.streaming.channel.ChannelAPI
import net.pantasystem.milktea.data.streaming.channel.ChannelAPIWithAccountProvider
import net.pantasystem.milktea.data.streaming.channel.connectUserTimeline
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteStreaming
import javax.inject.Inject

class NoteStreamingImpl @Inject constructor(
    private val channelAPIProvider: ChannelAPIWithAccountProvider,
    val noteDataSourceAdder: NoteDataSourceAdder,
) : NoteStreaming {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun connect(getAccount: suspend ()-> Account, pageable: Pageable): Flow<Note> {
        return flow {
            emit(getAccount())
        }.filter {
            pageable is Pageable.GlobalTimeline
                    || pageable is Pageable.HybridTimeline
                    || pageable is Pageable.LocalTimeline
                    || pageable is Pageable.HomeTimeline
                    || pageable is Pageable.UserListTimeline
                    || pageable is Pageable.Antenna
                    || pageable is Pageable.UserTimeline
                    || pageable is Pageable.ChannelTimeline
        }.flatMapLatest { ac ->
            when (pageable) {
                is Pageable.GlobalTimeline -> {
                    channelAPIProvider.get(ac).connect(ChannelAPI.Type.Global)
                }
                is Pageable.HybridTimeline -> {
                    channelAPIProvider.get(ac).connect(ChannelAPI.Type.Hybrid)
                }
                is Pageable.LocalTimeline -> {
                    channelAPIProvider.get(ac).connect(ChannelAPI.Type.Local)
                }
                is Pageable.HomeTimeline -> {
                    channelAPIProvider.get(ac).connect(ChannelAPI.Type.Home)
                }
                is Pageable.UserListTimeline -> {
                    channelAPIProvider.get(ac)
                        .connect(ChannelAPI.Type.UserList(userListId = pageable.listId))
                }
                is Pageable.Antenna -> {
                    channelAPIProvider.get(ac)
                        .connect(ChannelAPI.Type.Antenna(antennaId = pageable.antennaId))
                }
                is Pageable.UserTimeline -> {
                    channelAPIProvider.get(ac)
                        .connectUserTimeline(pageable.userId)
                }
                is Pageable.ChannelTimeline -> {
                    channelAPIProvider.get(ac)
                        .connect(ChannelAPI.Type.Channel(channelId = pageable.channelId))
                }
                else -> throw IllegalStateException("Global, Hybrid, Local, Homeは以外のStreamは対応していません。")
            }
        }.map {
            it as? ChannelBody.ReceiveNote
        }.filterNotNull().map {
            noteDataSourceAdder.addNoteDtoToDataSource(getAccount(), it.body)
        }
    }
}