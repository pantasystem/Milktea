package jp.panta.misskeyandroidclient.model.auth.custom

import jp.panta.misskeyandroidclient.api.misskey.auth.Session
import java.util.*

data class CustomAuthBridge(
    val secret: String,
    val instanceDomain: String,
    val session: Session,
    val enabledDateEnd: Date,
    val viaName: String?
)

fun App.createAuth(instanceDomain: String, session: Session, timeLimit: Date = Date(System.currentTimeMillis() + 3600 * 1000)): CustomAuthBridge {
    requireNotNull(secret)
    return CustomAuthBridge(
        secret = secret,
        instanceDomain = instanceDomain,
        session = session,
        enabledDateEnd = timeLimit,
        viaName = name
    )
}