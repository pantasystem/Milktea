package net.pantasystem.milktea.model.instance

interface FeatureEnables {
    suspend fun isEnable(
        instanceDomain: String,
        type: FeatureType,
        default: Boolean = true
    ): Boolean

    suspend fun enableFeatures(instanceDomain: String): Set<FeatureType>
}

enum class FeatureType {
    Gallery, Channel, Group, Antenna, UserReactionHistory, Drive, Bookmark, Clip,
}