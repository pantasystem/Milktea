package jp.panta.misskeyandroidclient.util

import android.util.Log
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BottomNavigationAdapter(
    private val bottomNavigationView: BottomNavigationView,
    private val fragmentManager: FragmentManager,
    @IdRes val currentMenuRes: Int,
    @IdRes val containerViewId: Int
) {
    companion object{
        private const val TAG = "BottomNavigationAdapter"
    }
    private var currentFragmentTag = makeTag(currentMenuRes)

    abstract fun getItem(menuItem: MenuItem) : Fragment?

    open fun menuRetouched(menuItem: MenuItem, fragment: Fragment){
        Log.d(TAG, "menu retouched menuItemId: ${menuItem.itemId}")
    }

    open fun viewChanged(menuItem: MenuItem, fragment: Fragment){
        Log.d(TAG, "viewChanged: $currentFragmentTag")
    }

    init{
        initFragment()
    }

    private fun initFragment(){
        bottomNavigationView.setOnNavigationItemSelectedListener {
            setFragment(it)
        }
        setCurrentFragment(currentMenuRes)
    }

    fun setCurrentFragment(@IdRes id: Int){
        val menuItem = bottomNavigationView.menu.findItem(id)
        menuItem.isChecked = setFragment(menuItem)

    }

    private fun setFragment(menuItem: MenuItem): Boolean{
        val targetTag = makeTag(menuItem.itemId)

        val currentFragment = fragmentManager.findFragmentByTag(currentFragmentTag)
        val targetFragment = fragmentManager.findFragmentByTag(targetTag)

        if(currentFragmentTag == targetTag && currentFragment != null){
            menuRetouched(menuItem, currentFragment)
            return true
        }

        val ft = fragmentManager.beginTransaction()

        if(currentFragment != null){
            //ft.detach(currentFragment)
            ft.hide(currentFragment)
            currentFragment.setMenuVisibility(false)
        }

        if(targetFragment == null){

            val fragment = getItem(menuItem)
            if(fragment == null){
                ft.commit()
                return false
            }
            fragment.setMenuVisibility(true)
            ft.add(containerViewId, fragment, targetTag)
            viewChanged(menuItem, fragment)

        }else{
            //ft.attach(targetFragment)
            targetFragment.setMenuVisibility(true)
            ft.show(targetFragment)
            viewChanged(menuItem, targetFragment)
        }
        currentFragmentTag = targetTag

        ft.commit()
        return true
    }

    private fun makeTag(@IdRes menuId: Int): String{
        return "BottomNavigationAdapter:${menuId}"
    }
}