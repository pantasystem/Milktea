package jp.panta.misskeyandroidclient.model.gallery

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.file.File

class CreateGalleryPost (
    val title: String,
    val author: Account,
    val files: List<File>,
    val description: String?,
    val isSensitive: Boolean,
    val tags: List<String>
)