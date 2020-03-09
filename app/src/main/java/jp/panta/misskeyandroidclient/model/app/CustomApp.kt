package jp.panta.misskeyandroidclient.model.app

import androidx.room.Entity

@Entity(primaryKeys = ["instanceDomain", "appSecret"])
data class CustomApp(
    val instanceDomain: String,
    val appSecret: String,
    val appName: String
)