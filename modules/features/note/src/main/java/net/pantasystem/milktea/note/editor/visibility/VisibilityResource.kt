package net.pantasystem.milktea.note.editor.visibility

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import net.pantasystem.milktea.model.note.Visibility
import net.pantasystem.milktea.note.R

@Composable
fun painterVisibilityIconResource(visibility: Visibility): Painter {
    return painterResource(
        id = when (visibility) {
            is Visibility.Followers -> R.drawable.ic_lock_black_24dp
            is Visibility.Home -> R.drawable.ic_home_black_24dp
            is Visibility.Public -> R.drawable.ic_language_black_24dp
            is Visibility.Specified -> R.drawable.ic_email_black_24dp
            is Visibility.Limited -> net.pantasystem.milktea.common_android.R.drawable.ic_groups
            Visibility.Mutual -> net.pantasystem.milktea.common_android.R.drawable.ic_sync_alt_24px
            Visibility.Personal -> net.pantasystem.milktea.common_android.R.drawable.ic_person_black_24dp
        }
    )
}

@Composable
fun stringVisibilityText(visibility: Visibility): String {
    return when (visibility) {
        is Visibility.Followers -> stringResource(id = R.string.visibility_follower)
        is Visibility.Home -> stringResource(id = R.string.visibility_home)
        is Visibility.Public -> stringResource(id = R.string.visibility_public)
        is Visibility.Specified -> stringResource(id = R.string.visibility_specified)
        is Visibility.Limited -> stringResource(id = R.string.visibility_limited)
        Visibility.Mutual -> stringResource(id = R.string.visibility_mutual)
        Visibility.Personal -> stringResource(id = R.string.visibility_personal)
    }
}