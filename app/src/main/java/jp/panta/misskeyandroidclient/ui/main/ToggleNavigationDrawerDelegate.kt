package jp.panta.misskeyandroidclient.ui.main

import android.app.Activity
import androidx.annotation.MainThread
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import jp.panta.misskeyandroidclient.R

class ToggleNavigationDrawerDelegate(
    private val activity: Activity,
    private val drawerLayout: DrawerLayout
) {
    private var toggle: ActionBarDrawerToggle? = null

    @MainThread
    fun updateToolbar(toolbar: Toolbar) {
        if (toggle != null) {
            drawerLayout.removeDrawerListener(toggle!!)
        }
        toggle = ActionBarDrawerToggle(
            activity,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        toggle!!.syncState()
    }

}