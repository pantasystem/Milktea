package jp.panta.misskeyandroidclient.model.instance.db

import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.instance.MetaRepository
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
        lock.withLock {
            return inMemoryMetaRepository.get(instanceDomain)?: roomMetaRepository.get(instanceDomain)
        }
    }

}