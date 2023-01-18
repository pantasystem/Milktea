package net.pantasystem.milktea.user.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.rememberAsyncImagePainter
import net.pantasystem.milktea.common_compose.CustomEmojiText
import net.pantasystem.milktea.model.user.FollowState
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.make
import net.pantasystem.milktea.user.R

sealed interface UserDetailCardAction {
    val userId: User.Id

    data class ToggleFollow(override val userId: User.Id) : UserDetailCardAction
    data class NotesCountClicked(override val userId: User.Id) : UserDetailCardAction
    data class FollowersCountClicked(override val userId: User.Id) : UserDetailCardAction
    data class FollowingsCountClicked(override val userId: User.Id) : UserDetailCardAction
    data class OnCardClicked(override val userId: User.Id) : UserDetailCardAction
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserDetailCard(
    userDetail: User.Detail,
    isUserNameMain: Boolean,
    accountHost: String?,
    onAction: (UserDetailCardAction) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = {
            onAction(UserDetailCardAction.OnCardClicked(userDetail.id))
        }
    ) {
        ConstraintLayout(
            Modifier.fillMaxWidth()
        ) {
            val (
                headerRef,
                avatarIconRef,
                mainNameRef,
                subNameRef,
                descriptionRef,
                aggregationRef,
                actionButton,

                followingULabel,
            ) = createRefs()

            Image(
                painter = rememberAsyncImagePainter(userDetail.info.bannerUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .constrainAs(headerRef) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)

                    }
            )

            if (userDetail.related.isFollower) {
                Text(
                    text = stringResource(id = R.string.follower),
                    color = Color.White,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .constrainAs(followingULabel) {
                            start.linkTo(parent.start, margin = 8.dp)
                            top.linkTo(parent.top, margin = 8.dp)
                        }
                )
            }

            UserStateActionButton(
                userState = userDetail.followState,
                modifier = Modifier.constrainAs(actionButton) {
                    end.linkTo(parent.end, margin = 8.dp)
                    top.linkTo(parent.top, margin = 8.dp)
                },
                onClick = {
                    onAction(UserDetailCardAction.ToggleFollow(userDetail.id))
                }
            )


            Image(
                painter = rememberAsyncImagePainter(userDetail.avatarUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colors.surface, CircleShape)
                    .constrainAs(avatarIconRef) {
                        start.linkTo(parent.start, margin = 8.dp)
                        bottom.linkTo(subNameRef.bottom)
                    }
            )

            CustomEmojiText(
                text = if (isUserNameMain) userDetail.displayUserName else userDetail.displayName,
                fontWeight = FontWeight.Bold,
                emojis = userDetail.emojis,
                parsedResult = if (isUserNameMain) null else userDetail.parsedResult,
                accountHost = accountHost,
                sourceHost = userDetail.host,
                modifier = Modifier
                    .constrainAs(mainNameRef) {
                        start.linkTo(avatarIconRef.end, margin = 4.dp)
                        top.linkTo(headerRef.bottom)
                    }
            )
            CustomEmojiText(
                text = if (!isUserNameMain) userDetail.displayUserName else userDetail.displayName,
                fontWeight = FontWeight.Bold,
                emojis = userDetail.emojis,
                parsedResult = if(isUserNameMain) userDetail.parsedResult else null,
                accountHost = accountHost,
                sourceHost = userDetail.host,
                modifier = Modifier
                    .constrainAs(subNameRef) {
                        start.linkTo(avatarIconRef.end, margin = 4.dp)
                        top.linkTo(mainNameRef.bottom)
                    }
            )

            CustomEmojiText(
                text = userDetail.info.description ?: "",
                maxLines = 5,
                textAlign = TextAlign.Start,
                emojis = userDetail.emojis,
                accountHost = accountHost,
                sourceHost = userDetail.host,
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .constrainAs(descriptionRef) {
                        start.linkTo(avatarIconRef.start)
                        top.linkTo(subNameRef.bottom, margin = 2.dp)
                        end.linkTo(parent.end)
                    }
            )


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                    .constrainAs(aggregationRef) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(descriptionRef.bottom, margin = 4.dp)
                    }
            ) {
                Text(
                    "${userDetail.info.notesCount ?: 0} ${stringResource(id = R.string.post)}",
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier
                        .padding(2.dp)
                        .clickable {
                            onAction(UserDetailCardAction.NotesCountClicked(userDetail.id))
                        },
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${userDetail.info.followingCount ?: 0} ${stringResource(id = R.string.following)}",
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier
                        .padding(2.dp)
                        .clickable {
                            onAction(UserDetailCardAction.FollowingsCountClicked(userDetail.id))
                        },
                )

                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${userDetail.info.followersCount ?: 0} ${stringResource(id = R.string.follower)}",
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier
                        .padding(2.dp)
                        .clickable {
                            onAction(UserDetailCardAction.FollowersCountClicked(userDetail.id))
                        }
                )

            }


        }
    }
}


@Composable
fun UserStateActionButton(
    userState: FollowState,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    when (userState) {
        FollowState.PENDING_FOLLOW_REQUEST -> {
            Button(
                onClick = onClick,
                modifier = modifier,
                shape = RoundedCornerShape(32.dp)
            ) {
                Text(stringResource(id = R.string.follow_approval_pending))
            }
        }
        FollowState.FOLLOWING -> {
            Button(
                onClick = onClick,
                modifier = modifier,
                shape = RoundedCornerShape(32.dp)
            ) {
                Text(stringResource(id = R.string.unfollow))
            }
        }
        FollowState.UNFOLLOWING -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier,
                shape = RoundedCornerShape(32.dp)
            ) {
                Text(stringResource(id = R.string.follow))
            }
        }
        FollowState.UNFOLLOWING_LOCKED -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier,
                shape = RoundedCornerShape(32.dp)
            ) {
                Text(stringResource(id = R.string.request_follow_from_u))
            }
        }
    }
}

@Preview
@Composable
fun Preview_UserDetail() {
    UserDetailCard(
        User.Detail.make(
            User.Id(0L, "id"),
            name = "harunon",
            userName = "harunon",
            host = "misskey.io",
            description = "ioawejfioawjfiojwaoefjioawejofawiofjawefawefoawijfoiawjfaefawoifjoawifjioawjfoijawoifjoawjefoiajwioefjioawjefiojaweiofjoiawjfnaiuefjaiowfjioawejfioajfiojaowijfoiawjnfojawoiejfoawejfiowaeijifoawjefijaowfjoiwjfioawjfojaowe",
            avatarUrl = "https://pbs.twimg.com/profile_images/1377726964404908032/nHtGMU-X_400x400.jpg",
            bannerUrl = "https://pbs.twimg.com/profile_banners/795973980721004546/1559754364/1500x500",
            followersCount = 5000,
            followingCount = 100,
            notesCount = 100000,

            isFollower = true,
        ),
        isUserNameMain = true,
        accountHost = "misskey.io"
    ) {}
}