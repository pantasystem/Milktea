package net.pantasystem.milktea.data.infrastructure.instance

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.model.instance.FeatureEnables
import net.pantasystem.milktea.model.instance.FeatureType
import net.pantasystem.milktea.model.instance.InstanceInfoService
import net.pantasystem.milktea.model.instance.InstanceInfoType
import net.pantasystem.milktea.model.instance.Version
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.nodeinfo.getVersion
import java.net.URL
import javax.inject.Inject

class FeatureEnablesImpl @Inject constructor(
    val nodeInfoRepository: NodeInfoRepository,
    private val instanceInfoService: InstanceInfoService,
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
            val instanceInfo = instanceInfoService.find(instanceDomain).getOrNull()
            val nodeInfo = nodeInfoRepository.find(URL(instanceDomain).host).getOrNull() ?: return@withContext emptySet()
            val version = instanceInfo?.version ?: nodeInfo.type.getVersion()
            val isMisskey = instanceInfo is InstanceInfoType.Misskey
            val isCalckey = isMisskey && nodeInfo.type is NodeInfo.SoftwareType.Misskey.Calckey
            val isMastodon = instanceInfo is InstanceInfoType.Mastodon
            val isFirefish = instanceInfo is InstanceInfoType.Firefish
            setOfNotNull(
                if (isMisskey && version >= Version("12.75.0") || isFirefish) FeatureType.Gallery else null,
                if (isMisskey && version >= Version("12") || isFirefish) FeatureType.Channel else null,
                if (isMisskey && version >= Version("11") && version <= Version("13.6.1") || isCalckey || isFirefish) FeatureType.Group else null,
                if (isMisskey && version >= Version("12.75.0") || isFirefish) FeatureType.Antenna else null,
                if (isMisskey && version >= Version("12") || isFirefish) FeatureType.UserReactionHistory else null,
                if (isMisskey && version >= Version("12") || isFirefish) FeatureType.Clip else null,
                if (isMisskey && version <= Version("13.6.1") || isCalckey || isFirefish) FeatureType.Messaging else null,
                if (isMisskey || isFirefish) FeatureType.Drive else null,
                if (isMastodon) FeatureType.Bookmark else null,
                if (nodeInfo.type is NodeInfo.SoftwareType.Misskey.Normal && version >= Version("13")) FeatureType.ReactionAcceptance else null,
                if (isMisskey || isFirefish) FeatureType.PostReactionUsers else null,
                if (isMisskey || isFirefish) FeatureType.PostLocalOnlyVisibility else null,
            )
        }
    }
}