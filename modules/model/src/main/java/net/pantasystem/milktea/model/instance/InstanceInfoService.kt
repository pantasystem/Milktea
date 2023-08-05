package net.pantasystem.milktea.model.instance

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class InstanceInfoService @Inject constructor(
    private val mastodonInstanceInfoRepository: MastodonInstanceInfoRepository,
    private val metaRepository: MetaRepository,
    private val nodeInfoRepository: NodeInfoRepository,
    private val customEmojiRepository: CustomEmojiRepository,
) {

    open suspend fun find(instanceDomain: String): Result<InstanceInfoType> {
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
                is NodeInfo.SoftwareType.Firefish -> {
                    InstanceInfoType.Firefish(
                        metaRepository.find(instanceDomain).getOrThrow()
                    )
                }
                is NodeInfo.SoftwareType.Pleroma -> {
                    InstanceInfoType.Pleroma(
                        mastodonInstanceInfoRepository.find(instanceDomain).getOrThrow()
                    )
                }
                is NodeInfo.SoftwareType.Other -> throw NoSuchElementException()
            }
        }
    }

    open suspend fun sync(instanceDomain: String): Result<Unit> {
        return nodeInfoRepository.find(URL(instanceDomain).host).mapCancellableCatching {
            when(it.type) {
                is NodeInfo.SoftwareType.Mastodon -> {
                    mastodonInstanceInfoRepository.sync(instanceDomain).getOrThrow()
                    customEmojiRepository.sync(it.host)
                }
                is NodeInfo.SoftwareType.Misskey -> {
                    metaRepository.sync(instanceDomain)
                    customEmojiRepository.sync(it.host).getOrThrow()
                }
                is NodeInfo.SoftwareType.Pleroma -> {
                    mastodonInstanceInfoRepository.sync(instanceDomain).getOrThrow()
                    customEmojiRepository.sync(it.host)
                }
                is NodeInfo.SoftwareType.Firefish -> {
                    metaRepository.sync(instanceDomain)
                    customEmojiRepository.sync(it.host).getOrThrow()
                }
                is NodeInfo.SoftwareType.Other -> throw NoSuchElementException()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    open fun observe(instanceDomain: String): Flow<InstanceInfoType?> {
        return suspend {
            nodeInfoRepository.find(URL(instanceDomain).host).getOrNull()
        }.asFlow().flatMapLatest { nodeInfo ->
            when(nodeInfo?.type) {
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
                is NodeInfo.SoftwareType.Firefish -> {
                    metaRepository.observe(instanceDomain).map {
                        it?.let {
                            InstanceInfoType.Firefish(it)
                        }
                    }
                }
                is NodeInfo.SoftwareType.Pleroma -> {
                    mastodonInstanceInfoRepository.observe(instanceDomain).map {
                        it?.let {
                            InstanceInfoType.Pleroma(it)
                        }
                    }
                }
                is NodeInfo.SoftwareType.Other -> flowOf(null)
                null -> flowOf(null)
            }
        }
    }
}