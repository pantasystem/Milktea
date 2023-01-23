package jp.panta.misskeyandroidclient.ui.main

import android.view.Gravity
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ActivityMainBinding
import jp.panta.misskeyandroidclient.util.DoubleBackPressedFinishDelegate

class SetupOnBackPressedDispatcherHandler(
    private val activity: AppCompatActivity,
    private val binding: ActivityMainBinding,
    private val backPressedDelegate: DoubleBackPressedFinishDelegate = DoubleBackPressedFinishDelegate(),
) {

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
                navController.backQueue.filterNot { it.destination.id == R.id.main_nav }.size > 1 -> {
                    navController.popBackStack()
                }
                else -> {
                    if (backPressedDelegate.back()) {
                        remove()
                        activity.onBackPressedDispatcher.onBackPressed()
                    } else {
                        Toast.makeText(
                            activity,
                            activity.getString(R.string.please_again_to_finish),
                            Toast.LENGTH_SHORT
                        ).apply {
                            setGravity(Gravity.CENTER, 0, 0)
                            show()
                        }
                    }
                }
            }
        }
    }
}