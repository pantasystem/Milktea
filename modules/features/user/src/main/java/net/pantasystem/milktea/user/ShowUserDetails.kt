package net.pantasystem.milktea.user
import net.pantasystem.milktea.model.user.User

interface ShowUserDetails {

    fun show(userId: User.Id?)
}