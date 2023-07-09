package net.pantasystem.milktea.note.editor.visibility

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.note.R


@Composable
fun VisibilitySelectionTile(
    item: Visibility,
    isSelected: Boolean,
    onClick: (item: Visibility) -> Unit,
) {

    val title = when (item) {
        is Visibility.Followers -> stringResource(id = R.string.visibility_follower)
        is Visibility.Home -> stringResource(id = R.string.visibility_home)
        is Visibility.Public -> stringResource(id = R.string.visibility_public)
        is Visibility.Specified -> stringResource(id = R.string.visibility_specified)
        is Visibility.Limited -> stringResource(id = R.string.visibility_limited)
        Visibility.Mutual -> stringResource(id = R.string.visibility_mutual)
        Visibility.Personal -> stringResource(id = R.string.visibility_personal)
    }

    val iconDrawable = when (item) {
        is Visibility.Followers -> R.drawable.ic_lock_black_24dp
        is Visibility.Home -> R.drawable.ic_home_black_24dp
        is Visibility.Public -> R.drawable.ic_language_black_24dp
        is Visibility.Specified -> R.drawable.ic_email_black_24dp
        is Visibility.Limited -> net.pantasystem.milktea.common_android.R.drawable.ic_groups
        Visibility.Mutual -> net.pantasystem.milktea.common_android.R.drawable.ic_sync_alt_24px
        Visibility.Personal -> net.pantasystem.milktea.common_android.R.drawable.ic_person_black_24dp
    }

    Surface(
        Modifier.clickable {
            onClick(item)
        },
        color = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(iconDrawable), contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text(title)
        }
    }
}


@Composable
fun VisibilityLocalOnlySwitch(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onChanged: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clickable {
                onChanged.invoke(!checked)
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.local_only))
        }

        Switch(checked = checked, onCheckedChange = onChanged, enabled = enabled)
    }
}
