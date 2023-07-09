package net.pantasystem.milktea.api.misskey.drive

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.Directory
import net.pantasystem.milktea.model.drive.DirectoryId

@Serializable
data class DirectoryNetworkDTO(
    @SerialName("id") val id: String,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("name") val name: String,
    @SerialName("foldersCount") val foldersCount: Int? = null,
    @SerialName("filesCount") val filesCount: Int? = null,
    @SerialName("parentId") val parentId: String? = null,
    @SerialName("parent") val parent: DirectoryNetworkDTO? = null
) {
    fun toModel(account: Account): Directory {
        return Directory(
            id = DirectoryId(
                accountId = account.accountId,
                directoryId = id
            ),
            createdAt = createdAt,
            name = name,
            foldersCount = foldersCount,
            filesCount = filesCount,
            parentId = parentId?.let {
                DirectoryId(
                    accountId = account.accountId,
                    directoryId = it
                )
            },
            parent = parent?.toModel(account)
        )
    }
}