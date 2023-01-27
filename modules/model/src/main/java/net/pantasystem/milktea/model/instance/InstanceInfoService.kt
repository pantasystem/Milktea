package net.pantasystem.milktea.model.instance

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstanceInfoService @Inject constructor(
    private val mastodonInstanceInfoRepository: MastodonInstanceInfoRepository,
    private val metaRepository: MetaRepository,
    private val nodeInfoRepository: NodeInfoRepository,
) {

    suspend fun find(instanceDomain: String): Result<InstanceInfoType> {
        return nodeInfoRepository.find(URL(instanceDomain).host).mapCancellableCatching {
            when(it.type) {
                is NodeInfo.SoftwareType.Mastodon -> {
                    InstanceInfoType.Mastodon(
                        mastodonInstanceInfoRepository.find(instanceDomain).getOrThrow()
                    )
                }
                is NodeInfo.SoftwareType.Misskey -> {
                    InstanceInfoType.Misskey(
                        metaRepository.find(instanceDomain).getOrThrow()
                    )
                }
                is NodeInfo.SoftwareType.Other -> throw NoSuchElementException()
            }
        }
    }

    suspend fun sync(instanceDomain: String): Result<Unit> {
        return nodeInfoRepository.find(URL(instanceDomain).host).mapCancellableCatching {
            when(it.type) {
                is NodeInfo.SoftwareType.Mastodon -> {
                    mastodonInstanceInfoRepository.sync(instanceDomain).getOrThrow()
                }
                is NodeInfo.SoftwareType.Misskey -> {
                    metaRepository.sync(instanceDomain)
                }
                is NodeInfo.SoftwareType.Other -> throw NoSuchElementException()
            }
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    fun observe(instanceDomain: String): Flow<InstanceInfoType?> {
        return suspend {
            nodeInfoRepository.find(URL(instanceDomain).host).getOrNull()
        }.asFlow().filterNotNull().flatMapLatest { nodeInfo ->
            when(nodeInfo.type) {
                is NodeInfo.SoftwareType.Mastodon -> {
                    mastodonInstanceInfoRepository.observe(instanceDomain).map {
                        it?.let {
                            InstanceInfoType.Mastodon(it)
                        }
                    }
                }
                is NodeInfo.SoftwareType.Misskey -> {
                    metaRepository.observe(instanceDomain).map {
                        it?.let {
                            InstanceInfoType.Misskey(it)
                        }
                    }
                }
                is NodeInfo.SoftwareType.Other -> emptyFlow()
            }
        }
    }
}