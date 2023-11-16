package net.pantasystem.milktea.user

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.pantasystem.milktea.model.user.FollowState

@Composable
fun FollowButton(
    userState: FollowState?,
    isMine: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val buttonText = getFollowStateString(userState)
    if (!isMine) {
        when (userState) {
            FollowState.UNFOLLOWING, FollowState.UNFOLLOWING_LOCKED -> {
                OutlinedButton(
                    shape = RoundedCornerShape(32.dp),
                    onClick = onClick,
                ) {
                    Text(buttonText)
                }
            }
            FollowState.FOLLOWING, FollowState.PENDING_FOLLOW_REQUEST -> {
                Button(
                    shape = RoundedCornerShape(32.dp),
                    onClick = onClick,
                ) {
                    Text(buttonText)
                }
            }
            null -> {  }
        }
    }
}

@Composable
private fun getFollowStateString(state: FollowState?): String = when (state) {
    FollowState.FOLLOWING -> stringResource(R.string.unfollow)
    FollowState.UNFOLLOWING -> stringResource(R.string.follow)
    FollowState.UNFOLLOWING_LOCKED -> stringResource(R.string.request_follow_from_u)
    FollowState.PENDING_FOLLOW_REQUEST -> stringResource(R.string.follow_approval_pending)
    else -> ""
}
