package jp.panta.misskeyandroidclient.util

import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BottomNavigationAdapter(
    private val bottomNavigationView: BottomNavigationView,
    private val fragmentManager: FragmentManager,
    @MenuRes val currentMenuRes: Int,
    @IdRes val containerViewId: Int
) {
    private var currentFragmentTag = makeTag(currentMenuRes)

    abstract fun getItem(menuItem: MenuItem) : Fragment?

    abstract fun menuRetouched(menuItem: MenuItem, fragment: Fragment)

    init{
        initFragment()
    }

    private fun initFragment(){
        bottomNavigationView.setOnNavigationItemSelectedListener {
            setFragment(it)
        }
        setCurrentFragment(currentMenuRes)
    }

    fun setCurrentFragment(@MenuRes id: Int){
        val menuItem = bottomNavigationView.menu.findItem(id)
        menuItem.isChecked = setFragment(menuItem)

    }

    private fun setFragment(menuItem: MenuItem): Boolean{
        val targetTag = makeTag(menuItem.itemId)

        val currentFragment = fragmentManager.findFragmentByTag(currentFragmentTag)
        val targetFragment = fragmentManager.findFragmentByTag(targetTag)

        if(currentFragmentTag == targetTag && currentFragment != null){
            menuRetouched(menuItem, currentFragment)
        }

        val ft = fragmentManager.beginTransaction()

        if(currentFragment != null){
            ft.detach(currentFragment)
        }

        if(targetFragment == null){

            val fragment = getItem(menuItem)?: return false
            ft.add(containerViewId, fragment, targetTag)

        }else{
            ft.attach(targetFragment)
        }

        ft.commit()
        return true
    }

    private fun makeTag(@MenuRes menuId: Int): String{
        return "BottomNavigationAdapter:${menuId}"
    }
}