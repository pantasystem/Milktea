package jp.panta.misskeyandroidclient.model.auth

import jp.panta.misskeyandroidclient.api.auth.AccessToken
import jp.panta.misskeyandroidclient.api.auth.Session
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.model.users.User


/**
 * 認証の状態
 */
sealed class Authorization {
    object BeforeAuthentication : Authorization()

    data class Waiting4UserAuthorization(
        val instanceBaseURL: String,
        val app: App,
        val session: Session
    ) : Authorization()

    data class Approved(
        val instanceBaseURL: String,
        val app: App,
        val accessToken: AccessToken
    ) : Authorization()

    data class Finish(
        val account: Account, val user: User.Detail
    ) : Authorization()

}