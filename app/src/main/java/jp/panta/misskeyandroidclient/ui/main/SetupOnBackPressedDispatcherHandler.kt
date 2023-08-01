package jp.panta.misskeyandroidclient.ui.main

import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ActivityMainBinding

class SetupOnBackPressedDispatcherHandler(
    private val activity: AppCompatActivity,
    private val binding: ActivityMainBinding,
) {

    @Suppress("RestrictedApi")
    fun setup() {
        activity.onBackPressedDispatcher.addCallback {
            val drawerLayout: DrawerLayout = binding.drawerLayout
            val navController = binding.appBarMain.contentMain.contentMain.findNavController()
            when {
                drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                // NOTE: そのままpopBackStackしてしまうとcurrentDestinationがNullになってしまいクラッシュしてしまう。
                // NOTE: backQueue == 2の時は初めのDestinationを表示している状態になっている
                // NOTE: backQueueには初期状態の時点で２つ以上入っている
                //  destinations stack
                //    |  fragment  |
                //    | navigation |
                //    |------------|
                // 参考: https://qiita.com/kaleidot725/items/a6010dc4e67c944f44f1

                navController.currentBackStack.value.filterNot { it.destination.id == R.id.main_nav }.size > 1 -> {
                    navController.popBackStack()
                }
                else -> {
                    remove()
                    activity.onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }
}