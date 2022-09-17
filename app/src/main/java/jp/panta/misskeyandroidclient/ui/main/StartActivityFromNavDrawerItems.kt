package jp.panta.misskeyandroidclient.ui.main

import android.content.Intent
import android.view.MenuItem
import jp.panta.misskeyandroidclient.MainActivity
import jp.panta.misskeyandroidclient.R
import net.pantasystem.milktea.antenna.AntennaListActivity
import net.pantasystem.milktea.channel.ChannelActivity
import net.pantasystem.milktea.drive.DriveActivity
import net.pantasystem.milktea.favorite.FavoriteActivity
import net.pantasystem.milktea.gallery.GalleryPostsActivity
import net.pantasystem.milktea.group.GroupActivity
import net.pantasystem.milktea.note.DraftNotesActivity
import net.pantasystem.milktea.setting.activities.SettingsActivity
import net.pantasystem.milktea.userlist.ListListActivity

internal class StartActivityFromNavDrawerItems(
    val mainActivity: MainActivity
) {
    fun showNavDrawersActivityBy(item: MenuItem) {
        val activity = when (item.itemId) {
            R.id.nav_setting -> SettingsActivity::class.java
            R.id.nav_drive -> DriveActivity::class.java
            R.id.nav_favorite -> FavoriteActivity::class.java
            R.id.nav_list -> ListListActivity::class.java
            R.id.nav_antenna -> AntennaListActivity::class.java
            R.id.nav_draft -> DraftNotesActivity::class.java
            R.id.nav_gallery -> GalleryPostsActivity::class.java
            R.id.nav_channel -> ChannelActivity::class.java
            R.id.nav_group -> GroupActivity::class.java
            else -> throw IllegalStateException("未定義なNavigation Itemです")
        }
        mainActivity.startActivity(Intent(mainActivity, activity))
    }
}