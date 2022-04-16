package jp.panta.misskeyandroidclient.ui.users.viewmodel
import net.pantasystem.milktea.data.model.users.User

interface ShowUserDetails {

    fun show(userId: User.Id?)
}