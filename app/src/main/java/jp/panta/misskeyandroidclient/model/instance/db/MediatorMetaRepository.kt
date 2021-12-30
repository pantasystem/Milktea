package jp.panta.misskeyandroidclient.model.instance.db

import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.instance.MetaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MediatorMetaRepository(
    private val roomMetaRepository: RoomMetaRepository,
    private val inMemoryMetaRepository: InMemoryMetaRepository,
) : MetaRepository{

    private val lock = Mutex()

    override suspend fun add(meta: Meta): Meta {
        lock.withLock {
            return inMemoryMetaRepository.add(roomMetaRepository.add(meta))
        }
    }

    override suspend fun delete(meta: Meta) {
        lock.withLock {
            inMemoryMetaRepository.delete(meta)
            roomMetaRepository.delete(meta)
        }
    }

    override suspend fun get(instanceDomain: String): Meta? {

        val inMem = inMemoryMetaRepository.get(instanceDomain)
        if(inMem != null) {
            return inMem
        }
        val dbMeta = roomMetaRepository.get(instanceDomain)

        if(dbMeta != null) {
            lock.withLock {
                inMemoryMetaRepository.add(dbMeta)
            }
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