package jp.panta.misskeyandroidclient.model.auth

import jp.panta.misskeyandroidclient.api.users.UserDTO

data class AccessToken(val accessToken: String, val user: UserDTO)

