package net.pantasystem.milktea.data.infrastructure.instance.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.instance.MetaDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediatorMetaDataSource @Inject constructor(
    private val roomMetaRepository: RoomMetaDataSource,
    private val inMemoryMetaRepository: InMemoryMetaDataSource,
) : MetaDataSource {

    override suspend fun add(meta: Meta): Meta {
        return inMemoryMetaRepository.add(roomMetaRepository.add(meta))
    }

    override suspend fun delete(meta: Meta) {
        inMemoryMetaRepository.delete(meta)
        roomMetaRepository.delete(meta)
    }

    override suspend fun get(instanceDomain: String): Meta? {

        val inMem = inMemoryMetaRepository.get(instanceDomain)
        if(inMem != null) {
            return inMem
        }
        val dbMeta = roomMetaRepository.get(instanceDomain)

        if(dbMeta != null) {
            inMemoryMetaRepository.add(dbMeta)
        }

        return inMemoryMetaRepository.get(instanceDomain)
    }

    override fun observe(instanceDomain: String): Flow<Meta?> {
        val inMemoryFlow = inMemoryMetaRepository.observe(instanceDomain)
        val dbFlow = roomMetaRepository.observe(instanceDomain)
        return combine(inMemoryFlow, dbFlow) { mem, db ->
            db ?: mem
        }.distinctUntilChanged()
    }
}