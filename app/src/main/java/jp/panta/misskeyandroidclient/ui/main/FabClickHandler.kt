package jp.panta.misskeyandroidclient.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common_android_ui.account.AccountSwitchingDialog
import net.pantasystem.milktea.common_viewmodel.CurrentPageType
import net.pantasystem.milktea.common_viewmodel.CurrentPageableTimelineViewModel
import net.pantasystem.milktea.common_viewmodel.SuitableType
import net.pantasystem.milktea.common_viewmodel.suitableType
import net.pantasystem.milktea.gallery.GalleryPostsActivity
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.note.NoteEditorActivity

internal class FabClickHandler(
    private val currentPageableTimelineViewModel: CurrentPageableTimelineViewModel,
    private val activity: AppCompatActivity,
    private val accountStore: AccountStore,
) {

    fun onClicked() {
        activity.apply {
            when (val type = currentPageableTimelineViewModel.currentType.value) {
                CurrentPageType.Account -> {
                    AccountSwitchingDialog().show(
                        activity.supportFragmentManager,
                        "AccountSwitchingDialog"
                    )
                }
                is CurrentPageType.Page -> {
                    when (val suitableType = type.pageable.suitableType()) {
                        is SuitableType.Other -> {
                            val text = when (val pageable = type.pageable) {
                                is Pageable.SearchByTag -> "#${pageable.tag}"
                                is Pageable.Mastodon.HashTagTimeline -> "#${pageable.hashtag}"
                                else -> ""
                            }
                            startActivity(
                                NoteEditorActivity.newBundle(
                                    this,
                                    accountId = type.accountId,
                                    text = text
                                )
                            )
                        }
                        is SuitableType.Gallery -> {
                            val intent = Intent(this, GalleryPostsActivity::class.java)
                            intent.action = Intent.ACTION_EDIT
                            startActivity(intent)
                        }
                        is SuitableType.Channel -> {
                            val accountId = type.accountId ?: accountStore.currentAccountId!!
                            startActivity(
                                NoteEditorActivity.newBundle(
                                    this,
                                    channelId = Channel.Id(accountId, suitableType.channelId),
                                )
                            )
                        }
                    }
                }
            }

        }

    }
}