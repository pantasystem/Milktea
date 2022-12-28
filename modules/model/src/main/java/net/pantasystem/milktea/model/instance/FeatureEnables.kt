package net.pantasystem.milktea.model.instance

interface FeatureEnables {
    suspend fun isEnable(instanceDomain: String, type: FeatureType, default: Boolean = true): Boolean
}

enum class FeatureType {
    Gallery, Channel, Group, Antenna, UserReactionHistory,
}