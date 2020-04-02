package jp.panta.misskeyandroidclient.model.drive


data class CreateFolder(
    val i: String,
    val name: String,
    val parentId: String?
)