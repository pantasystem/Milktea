package jp.panta.misskeyandroidclient.viewmodel.users
import jp.panta.misskeyandroidclient.model.users.User

interface ShowUserDetails {

    fun show(userId: User.Id?)
}