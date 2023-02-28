package net.pantasystem.milktea.data.infrastructure.instance

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.instance.FeatureEnables
import net.pantasystem.milktea.model.instance.FeatureType
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.instance.Version
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.nodeinfo.getVersion
import java.net.URL
import javax.inject.Inject

class FeatureEnablesImpl @Inject constructor(
    val misskeyAPIProvider: MisskeyAPIProvider,
    val metaRepository: MetaRepository,
    val nodeInfoRepository: NodeInfoRepository,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
): FeatureEnables {
    override suspend fun isEnable(instanceDomain: String, type: FeatureType, default: Boolean): Boolean {
        val features = enableFeatures(instanceDomain)
        return if (features.isEmpty()) {
            default
        } else {
            features.contains(type)
        }
    }

    override suspend fun enableFeatures(instanceDomain: String): Set<FeatureType> {
        return withContext(ioDispatcher) {
            val meta = metaRepository.find(instanceDomain).getOrNull()
            val nodeInfo = nodeInfoRepository.find(URL(instanceDomain).host).getOrNull() ?: return@withContext emptySet()
            val version = meta?.getVersion() ?: nodeInfo.type.getVersion()
            val isMisskey = meta != null || nodeInfo.type is NodeInfo.SoftwareType.Misskey
            val isMastodon = nodeInfo.type is NodeInfo.SoftwareType.Mastodon
            setOfNotNull(
                if (isMisskey && version >= Version("12.75.0")) FeatureType.Gallery else null,
                if (isMisskey && version >= Version("12")) FeatureType.Channel else null,
                if (isMisskey && version >= Version("11")) FeatureType.Group else null,
                if (isMisskey && version >= Version("12.75.0")) FeatureType.Antenna else null,
                if (isMisskey && version >= Version("12")) FeatureType.UserReactionHistory else null,
                if (isMisskey && version >= Version("12")) FeatureType.Clip else null,
                if (isMisskey && version <= Version("13.6.1")) FeatureType.Messaging else null,
                if (isMisskey) FeatureType.Drive else null,
                if (isMastodon) FeatureType.Bookmark else null,
            )
        }
    }
}