package jp.panta.misskeyandroidclient.viewmodel.users

import jp.panta.misskeyandroidclient.api.users.UserDTO

interface ShowUserDetails {

    fun show(user: UserDTO?)
}