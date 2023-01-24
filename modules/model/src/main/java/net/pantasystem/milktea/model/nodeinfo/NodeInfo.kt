package net.pantasystem.milktea.model.nodeinfo

import net.pantasystem.milktea.model.instance.Version


data class NodeInfo(
    val host: String,
    val version: String,
    val software: Software,
) {

    data class Software(
        val name: String,
        val version: String,
    )

    sealed interface SoftwareType {
        val version: String
        val name: String
        sealed interface Misskey : SoftwareType {
            data class Normal(
                override val name: String,
                override val version: String,
            ) : Misskey

            data class Calckey(
                override val name: String,
                override val version: String,
            ) : Misskey
        }

        sealed interface Mastodon : SoftwareType {
            data class Normal(
                override val name: String,
                override val version: String,
            ) : Mastodon

            data class Fedibird(
                override val name: String,
                override val version: String,
            ) : Mastodon

        }

        data class Other(
            override val version: String,
            override val name: String
        ) : SoftwareType
    }

    val type = when(software.name) {
        "calckey" -> SoftwareType.Misskey.Calckey(version = software.version, name = software.name)
        "misskey" -> SoftwareType.Misskey.Normal(version = software.version, name = software.name)
        "mastodon" -> SoftwareType.Mastodon.Normal(version = software.version, name = software.name)
        "fedibird" -> SoftwareType.Mastodon.Fedibird(version = software.version, name = software.name)
        else -> SoftwareType.Other(version = software.version, name = software.name)
    }
}

fun NodeInfo.SoftwareType.getVersion(): Version {
    return Version(this.version)
}