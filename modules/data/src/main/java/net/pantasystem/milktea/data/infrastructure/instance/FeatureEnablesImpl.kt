package net.pantasystem.milktea.data.infrastructure.instance

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.instance.FeatureEnables
import net.pantasystem.milktea.model.instance.FeatureType
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.instance.Version
import javax.inject.Inject

class FeatureEnablesImpl @Inject constructor(
    val misskeyAPIProvider: MisskeyAPIProvider,
    val metaRepository: MetaRepository,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
): FeatureEnables {
    override suspend fun isEnable(instanceDomain: String, type: FeatureType, default: Boolean): Boolean {
        return withContext(ioDispatcher) {
            val meta = metaRepository.find(instanceDomain).getOrNull()?: return@withContext default
            when(type) {
                FeatureType.Gallery -> meta.getVersion() >= Version("12.75.0")
                FeatureType.Channel -> meta.getVersion() >= Version("12")
                FeatureType.Group -> meta.getVersion() >= Version("11")
                FeatureType.Antenna -> meta.getVersion() >= Version("12.75.0")
                FeatureType.UserReactionHistory -> meta.getVersion() >= Version("12")
            }
        }
    }

    override suspend fun enableFeatures(instanceDomain: String): Set<FeatureType> {
        return withContext(ioDispatcher) {
            val meta = metaRepository.find(instanceDomain).getOrNull()?: return@withContext emptySet()
            setOfNotNull(
                if (meta.getVersion() >= Version("12.75.0")) FeatureType.Gallery else null,
                if (meta.getVersion() >= Version("12")) FeatureType.Channel else null,
                if (meta.getVersion() >= Version("11")) FeatureType.Group else null,
                if (meta.getVersion() >= Version("12.75.0")) FeatureType.Antenna else null,
                if (meta.getVersion() >= Version("12")) FeatureType.UserReactionHistory else null,
            )
        }
    }
}