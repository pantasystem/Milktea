package net.pantasystem.milktea.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.rememberAsyncImagePainter
import net.pantasystem.milktea.common_compose.CustomEmojiText
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.user.R

@Composable
fun AccountInfoLayout(
    modifier: Modifier = Modifier,
    isUserNameMain: Boolean,
    userDetail: User.Detail,
    account: Account,
) {
    ConstraintLayout(
        modifier.fillMaxWidth()
    ) {
        val (
            headerRef,
            avatarIconRef,
            mainNameRef,
            subNameRef,
            descriptionRef,
            aggregationRef,
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
            accountHost = account.getHost(),
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
            accountHost = account.getHost(),
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
            accountHost = account.getHost(),
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
//                        onAction(UserDetailCardAction.NotesCountClicked(userDetail.id))
                    },
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "${userDetail.info.followingCount ?: 0} ${stringResource(id = R.string.following)}",
                color = MaterialTheme.colors.primary,
                modifier = Modifier
                    .padding(2.dp)
                    .clickable {
//                        onAction(UserDetailCardAction.FollowingsCountClicked(userDetail.id))
                    },
            )

            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "${userDetail.info.followersCount ?: 0} ${stringResource(id = R.string.follower)}",
                color = MaterialTheme.colors.primary,
                modifier = Modifier
                    .padding(2.dp)
                    .clickable {
//                        onAction(UserDetailCardAction.FollowersCountClicked(userDetail.id))
                    }
            )

        }


    }
}