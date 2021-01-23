package jp.panta.misskeyandroidclient.viewmodel.users

import jp.panta.misskeyandroidclient.api.users.User

interface ShowUserDetails {

    fun show(user: User?)
}