package net.pantasystem.milktea.common_android_ui

import androidx.fragment.app.Fragment
import net.pantasystem.milktea.model.user.User

interface UserPinnedNotesFragmentFactory {
    fun create(userId: User.Id): Fragment
}