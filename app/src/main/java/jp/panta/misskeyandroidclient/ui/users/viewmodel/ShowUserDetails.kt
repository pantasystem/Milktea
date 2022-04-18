package jp.panta.misskeyandroidclient.ui.users.viewmodel
import net.pantasystem.milktea.model.user.User

interface ShowUserDetails {

    fun show(userId: net.pantasystem.milktea.model.user.User.Id?)
}