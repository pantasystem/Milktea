package jp.panta.misskeyandroidclient.model.auth

import jp.panta.misskeyandroidclient.model.users.User

data class AccessToken(val accessToken: String, val user: User)

