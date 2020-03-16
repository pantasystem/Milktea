package jp.panta.misskeyandroidclient.model.auth.custom

import jp.panta.misskeyandroidclient.model.auth.Session
import java.util.*

data class CustomAuthBridge(
    val secret: String,
    val instanceDomain: String,
    val session: Session,
    val enabledDateEnd: Date,
    val viaName: String?
)