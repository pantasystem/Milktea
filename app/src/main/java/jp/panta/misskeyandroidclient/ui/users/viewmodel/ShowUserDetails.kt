package jp.panta.misskeyandroidclient.ui.users.viewmodel
import jp.panta.misskeyandroidclient.model.users.User

interface ShowUserDetails {

    fun show(userId: User.Id?)
}