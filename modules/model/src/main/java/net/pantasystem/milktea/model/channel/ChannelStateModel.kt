package net.pantasystem.milktea.model.channel

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

data class ChannelState(
    val channels: Map<Channel.Id, Channel>
) {
    fun add(channel: Channel): ChannelState {
        return this.copy(
            channels = channels.toMutableMap().also { map ->
                map[channel.id] = channel
            }
        )
    }

    fun addAll(channels: Collection<Channel>): ChannelState {
        return this.copy(
            channels = this.channels.toMutableMap().also { map ->
                map.putAll(channels.map { it.id to it })
            }
        )
    }

    fun remove(channel: Channel.Id): ChannelState {
        return this.copy(
            channels = this.channels.toMutableMap().also {
                it.remove(channel)
            }
        )
    }

    fun get(id: Channel.Id): Channel? {
        return channels[id]
    }

    fun getIn(ids: Collection<Channel.Id>): List<Channel> {
        return channels.filter {
            ids.contains(it.key)
        }.values.toList()
    }

}
interface ChannelStateModel  {
    val state: StateFlow<ChannelState>

    suspend fun addAll(channels: List<Channel>): List<Channel>

    suspend fun add(channel: Channel): Channel

    suspend fun get(id: Channel.Id): Channel?

    suspend fun remove(id: Channel.Id)

    fun observeOne(id: Channel.Id): Flow<Channel?>

    fun observeAll(ids: List<Channel.Id>): Flow<List<Channel>>

}


class ChannelStateModelOnMemory @Inject constructor(): ChannelStateModel {
    private val _state = MutableStateFlow(ChannelState(emptyMap()))
    override val state: StateFlow<ChannelState>
        get() = _state

    private val lock = Mutex()

    override suspend fun add(channel: Channel): Channel {
         lock.withLock {
             _state.value = state.value.add(channel)
         }
        return channel
    }

    override suspend fun addAll(channels: List<Channel>): List<Channel> {
        lock.withLock {
            _state.value = state.value.addAll(channels)
        }
        return channels
    }

    override suspend fun get(id: Channel.Id): Channel? {
        return state.value.get(id)
    }

    override fun observeAll(ids: List<Channel.Id>): Flow<List<Channel>> {
        val idSets = ids.toSet()
        return state.map { state ->
            state.getIn(idSets)
        }
    }

    override fun observeOne(id: Channel.Id): Flow<Channel?> {
        return state.map {
            it.get(id)
        }
    }

    override suspend fun remove(id: Channel.Id) {
        lock.withLock {
            _state.value = state.value.remove(id)
        }
    }
}