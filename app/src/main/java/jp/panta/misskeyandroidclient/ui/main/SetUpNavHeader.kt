package jp.panta.misskeyandroidclient.ui.main

import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.navigation.NavigationView
import jp.panta.misskeyandroidclient.databinding.NavHeaderMainBinding
import net.pantasystem.milktea.common_android_ui.account.viewmodel.AccountViewModel

internal class SetUpNavHeader(
    private val navView: NavigationView,
    private val lifecycleOwner: LifecycleOwner,
    private val accountViewModel: AccountViewModel
) {

    operator fun invoke() {
        DataBindingUtil.bind<NavHeaderMainBinding>(this.navView.getHeaderView(0))
        val headerBinding =
            DataBindingUtil.getBinding<NavHeaderMainBinding>(this.navView.getHeaderView(0))
        headerBinding?.lifecycleOwner = lifecycleOwner
        headerBinding?.accountViewModel = accountViewModel
    }
}
