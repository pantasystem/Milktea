package jp.panta.misskeyandroidclient.model.auth

import jp.panta.misskeyandroidclient.api.users.User

data class AccessToken(val accessToken: String, val user: User)

