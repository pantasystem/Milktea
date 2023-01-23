package jp.panta.misskeyandroidclient.ui.main

import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import jp.panta.misskeyandroidclient.MainActivity
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.setMenuTint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.messaging.MessagingListActivity
import net.pantasystem.milktea.notification.NotificationsActivity
import net.pantasystem.milktea.search.SearchActivity
import net.pantasystem.milktea.setting.activities.PageSettingActivity
import net.pantasystem.milktea.setting.activities.SettingsActivity

internal class MainActivityMenuProvider(
    val activity: MainActivity,
    val settingStore: SettingStore
) : MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        activity.setMenuTint(menu)
        menuInflater.inflate(R.menu.main, menu)

        listOf(
            menu.findItem(R.id.action_messaging),
            menu.findItem(R.id.action_notification),
            menu.findItem(R.id.action_search)
        ).forEach {
            it.isVisible = settingStore.isClassicUI
        }

    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        val idAndActivityMap = mapOf(
            R.id.action_settings to SettingsActivity::class.java,
            R.id.action_tab_setting to PageSettingActivity::class.java,
            R.id.action_notification to NotificationsActivity::class.java,
            R.id.action_messaging to MessagingListActivity::class.java,
            R.id.action_search to SearchActivity::class.java
        )

        val targetActivity = idAndActivityMap[menuItem.itemId]
            ?: return false
        activity.startActivity(Intent(activity, targetActivity))
        return true
    }
}