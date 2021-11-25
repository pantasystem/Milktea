package jp.panta.misskeyandroidclient.model.users.report

import jp.panta.misskeyandroidclient.model.users.User

data class Report(
    val userId: User.Id,
    val comment: String
)