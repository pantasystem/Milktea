package jp.panta.misskeyandroidclient.ui.main

import com.google.android.material.navigation.NavigationView
import jp.panta.misskeyandroidclient.R
import net.pantasystem.milktea.api.misskey.MisskeyAPI
import net.pantasystem.milktea.api.misskey.v12.MisskeyAPIV12
import net.pantasystem.milktea.api.misskey.v12_75_0.MisskeyAPIV1275

internal class ChangeNavMenuVisibilityFromAPIVersion(
    private val navView: NavigationView
) {
    operator fun invoke(api: MisskeyAPI) {
        navView.menu.also { menu ->
            menu.findItem(R.id.nav_antenna).isVisible = api is MisskeyAPIV12
            menu.findItem(R.id.nav_channel).isVisible = api is MisskeyAPIV12
            menu.findItem(R.id.nav_gallery).isVisible = api is MisskeyAPIV1275
            menu.findItem(R.id.nav_group).isVisible = false
        }
    }
}
