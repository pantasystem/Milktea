package net.pantasystem.milktea.data.infrastructure.instance

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.infrastructure.instance.db.FedibirdCapabilitiesRecord
import net.pantasystem.milktea.data.infrastructure.instance.db.MastodonInstanceInfoDAO
import net.pantasystem.milktea.data.infrastructure.instance.db.MastodonInstanceInfoRecord
import net.pantasystem.milktea.data.infrastructure.instance.db.PleromaMetadataFeatures
import net.pantasystem.milktea.data.infrastructure.instance.db.from
import net.pantasystem.milktea.data.infrastructure.instance.db.toModel
import net.pantasystem.milktea.data.infrastructure.toModel
import net.pantasystem.milktea.model.instance.MastodonInstanceInfo
import net.pantasystem.milktea.model.instance.MastodonInstanceInfoRepository
import java.net.URL
import javax.inject.Inject

class MastodonInstanceInfoRepositoryImpl @Inject constructor(
    private val mastodonInstanceInfoDAO: MastodonInstanceInfoDAO,
    private val mastodonAPIProvider: MastodonAPIProvider,
    private val cache: MastodonInstanceInfoCache,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : MastodonInstanceInfoRepository {

    override suspend fun find(instanceDomain: String): Result<MastodonInstanceInfo> =
        runCancellableCatching {
            withContext(ioDispatcher) {
                var info = cache.get(instanceDomain)
                if (info != null) {
                    return@withContext info
                }
                info = mastodonInstanceInfoDAO.findBy(URL(instanceDomain).host)?.toModel()
                if (info != null) {
                    cache.put(instanceDomain, info)
                    return@withContext info
                }

                info = mastodonAPIProvider.get(instanceDomain).getInstance().toModel()
                upInsert(info)
                cache.put(instanceDomain, info)
                info
            }
        }


    override fun observe(instanceDomain: String): Flow<MastodonInstanceInfo?> {
        return mastodonInstanceInfoDAO.observeBy(URL(instanceDomain).host).map {
            it?.toModel()
        }.onEach {
            if (it != null) {
                cache.put(instanceDomain, it)
            }
        }.flowOn(ioDispatcher)
    }

    override suspend fun sync(instanceDomain: String): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val model = mastodonAPIProvider.get(instanceDomain).getInstance().toModel()
            cache.put(instanceDomain, model)
            upInsert(model)
        }
    }

    private suspend fun upInsert(instanceInfo: MastodonInstanceInfo) {
        val exists = mastodonInstanceInfoDAO.findBy(instanceInfo.uri)
        if (exists == null) {
            mastodonInstanceInfoDAO.insert(MastodonInstanceInfoRecord.from(instanceInfo))
            instanceInfo.fedibirdCapabilities?.let { capabilities ->
                mastodonInstanceInfoDAO.insertFedibirdCapabilities(
                    capabilities.map {
                        FedibirdCapabilitiesRecord(it, instanceInfo.uri)
                    }
                )
            }
            instanceInfo.pleroma?.let { pleroma ->
                mastodonInstanceInfoDAO.insertPleromaMetadataFeatures(
                    pleroma.metadata.features.map {
                        PleromaMetadataFeatures(
                            type = it,
                            uri = instanceInfo.uri,
                        )
                    }
                )
            }
        } else {
            mastodonInstanceInfoDAO.update(MastodonInstanceInfoRecord.from(instanceInfo))
            mastodonInstanceInfoDAO.clearFedibirdCapabilities(instanceInfo.uri)
            mastodonInstanceInfoDAO.clearPleromaMetadataFeatures(instanceInfo.uri)
            instanceInfo.fedibirdCapabilities?.let { capabilities ->
                mastodonInstanceInfoDAO.insertFedibirdCapabilities(
                    capabilities.map {
                        FedibirdCapabilitiesRecord(it, instanceInfo.uri)
                    }
                )
            }
            instanceInfo.pleroma?.let { pleroma ->
                mastodonInstanceInfoDAO.insertPleromaMetadataFeatures(
                    pleroma.metadata.features.map {
                        PleromaMetadataFeatures(
                            type = it,
                            uri = instanceInfo.uri,
                        )
                    }
                )
            }
        }
    }
}