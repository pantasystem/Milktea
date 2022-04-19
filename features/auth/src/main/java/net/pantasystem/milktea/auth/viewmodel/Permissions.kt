package net.pantasystem.milktea.auth.viewmodel

import net.pantasystem.milktea.model.instance.Version

object Permissions{
    private val defaultPermission = listOf(
        "write:user-groups",
        "read:user-groups",
        "read:page-likes",
        "write:page-likes",
        "write:pages",
        "read:pages",
        "write:votes",
        "write:reactions",
        "read:reactions",
        "write:notifications",
        "read:notifications",
        "write:notes",
        "write:mutes",
        "read:mutes",
        "read:account",
        "write:account",
        "read:blocks",
        "write:blocks",
        "read:drive",
        "write:drive",
        "read:favorites",
        "write:favorites",
        "read:following",
        "write:following",
        "read:messaging",
        "write:messaging"
    )

    private val V_12_47_0: List<String> = defaultPermission.toMutableList().apply {
        addAll(listOf("read:channels", "write:channels"))
    }

    private val V_12_75_0: List<String> = V_12_47_0.toMutableList().apply {
        addAll(
            listOf(
                "read:gallery",
                "write:gallery",
                "read:gallery-likes",
                "write:gallery-likes"
            )
        )
    }

    fun getPermission(version: Version) : List<String>{
        return when {
            version >= Version("12.75.0") -> {
                V_12_75_0
            }
            version >= Version("12.47.0") -> {
                V_12_47_0
            }
            else -> {
                defaultPermission
            }
        }
    }
}