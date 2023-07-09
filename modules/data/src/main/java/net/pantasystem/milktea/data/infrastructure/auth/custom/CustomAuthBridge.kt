package net.pantasystem.milktea.data.infrastructure.auth.custom

import net.pantasystem.milktea.api.misskey.auth.Session
import net.pantasystem.milktea.model.app.AppType
import java.util.*


sealed interface TemporarilyAuthState {
    val enabledDateEnd: Date
    val instanceDomain: String
    data class Misskey(
        val secret: String,
        override val instanceDomain: String,
        val session: Session,
        override val enabledDateEnd: Date,
        val viaName: String?
    ) : TemporarilyAuthState

    data class Mastodon(
        override val instanceDomain: String,
        override val enabledDateEnd: Date,
        val app: AppType.Mastodon,
        val scope: String,

    ) : TemporarilyAuthState

    data class Pleroma(
        override val instanceDomain: String,
        override val enabledDateEnd: Date,
        val app: AppType.Pleroma,
        val scope: String,
    ) : TemporarilyAuthState
}

fun AppType.Misskey.createAuth(instanceDomain: String, session: Session, timeLimit: Date = Date(System.currentTimeMillis() + 3600 * 1000)): TemporarilyAuthState.Misskey {
    requireNotNull(secret)
    return TemporarilyAuthState.Misskey(
        secret = secret!!,
        instanceDomain = instanceDomain,
        session = session,
        enabledDateEnd = timeLimit,
        viaName = name
    )
}

fun AppType.Mastodon.createAuth(instanceDomain: String, scope: String, timeLimit: Date = Date(System.currentTimeMillis() + 3600 * 1000)): TemporarilyAuthState.Mastodon {
    return TemporarilyAuthState.Mastodon(
        scope = scope,
        instanceDomain = instanceDomain,
        enabledDateEnd = timeLimit,
        app = this
    )
}

fun AppType.Pleroma.createAuth(instanceDomain: String, scope: String, timeLimit: Date = Date(System.currentTimeMillis() + 3600 * 1000)): TemporarilyAuthState.Pleroma {
    return TemporarilyAuthState.Pleroma(
        scope = scope,
        instanceDomain = instanceDomain,
        enabledDateEnd = timeLimit,
        app = this
    )
}