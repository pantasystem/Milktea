package jp.panta.misskeyandroidclient.model.auth

import jp.panta.misskeyandroidclient.api.auth.AccessToken
import jp.panta.misskeyandroidclient.api.auth.Session
import jp.panta.misskeyandroidclient.model.auth.custom.App


/**
 * 認証の状態
 */
sealed class Authentication {
    object BeforeAuthentication : Authentication()

    data class Waiting4UserAuthentication(
        val instanceBaseURL: String,
        val app: App,
        val session: Session
    ) : Authentication()

    data class Authenticated(
        val instanceBaseURL: String,
        val app: App,
        val userKey: AccessToken
    )

}