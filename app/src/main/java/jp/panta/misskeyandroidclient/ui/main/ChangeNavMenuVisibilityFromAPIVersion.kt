package jp.panta.misskeyandroidclient.ui.main

import com.google.android.material.navigation.NavigationView
import jp.panta.misskeyandroidclient.R
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.instance.FeatureEnables
import net.pantasystem.milktea.model.instance.FeatureType

internal class ChangeNavMenuVisibilityFromAPIVersion(
    private val navView: NavigationView,
    private val featureEnables: FeatureEnables,
) {
    suspend operator fun invoke(currentAccount: Account) {
        val enableFeatures = featureEnables.enableFeatures(currentAccount.normalizedInstanceDomain)
        navView.menu.also { menu ->
            menu.findItem(R.id.nav_antenna).isVisible = enableFeatures.contains(FeatureType.Antenna)
            menu.findItem(R.id.nav_channel).isVisible = enableFeatures.contains(FeatureType.Channel)
            menu.findItem(R.id.nav_gallery).isVisible = enableFeatures.contains(FeatureType.Gallery)
            menu.findItem(R.id.nav_group).isVisible = enableFeatures.contains(FeatureType.Group)
        }
    }
}
