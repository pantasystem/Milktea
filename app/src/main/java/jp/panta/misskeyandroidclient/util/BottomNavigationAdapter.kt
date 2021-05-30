package jp.panta.misskeyandroidclient.util

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import jp.panta.misskeyandroidclient.R

abstract class BottomNavigationAdapter(
    private val bottomNavigationView: BottomNavigationView,
    private val fragmentManager: FragmentManager,
    @IdRes val currentMenuRes: Int,
    @IdRes val containerViewId: Int,
    val savedInstanceState: Bundle?
) {
    companion object{
        private const val TAG = "BottomNavigationAdapter"
        private const val EXTRA_SELECTED_ITEM_ID = "jp.panta.misskeyandroidclient.util.BottomNavigationAdapter.EXTRA_SELECTED_ITEM_ID"
    }
    private var currentFragmentTag: String = makeTag(currentMenuRes)
    private var selectedItemId: Int = savedInstanceState?.getInt(EXTRA_SELECTED_ITEM_ID, currentMenuRes)?: bottomNavigationView.selectedItemId

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
        bottomNavigationView.setOnItemSelectedListener {
            setFragment(it)
        }
        /*if(selectedItemId == R.id.navigation_home){
            Log.d("BottomNavigationAdapter", "#initFragment homeを選択していることになっている")
        }else{
            Log.d("BottomNavigationAdapter", "#initFragment homeを選択していない")
        }*/
        if(currentFragmentTag == makeTag(R.id.navigation_home)){
            Log.d(TAG, "#initFragment currentFragmentTagはHomeを選択していることになっている")
        }

        setCurrentFragment(selectedItemId)

    }

    fun setCurrentFragment(@IdRes id: Int){
        val menuItem = bottomNavigationView.menu.findItem(id)
        if(menuItem.itemId == R.id.navigation_home){
            Log.d("BottomNavigationAdapter", "#setCurrentFragment, homeを選択していることになっている")
        }
        menuItem.isChecked = setFragment(menuItem).also{
            if(it){
                Log.d(TAG, "Fragment設置成功")
            }else{
                Log.d(TAG, "Fragment設置　失敗")
            }
        }

    }

    private fun setFragment(menuItem: MenuItem): Boolean{
        selectedItemId = menuItem.itemId
        val targetTag = makeTag(menuItem.itemId)

        val currentFragment = fragmentManager.findFragmentByTag(currentFragmentTag)


        val targetFragment = fragmentManager.findFragmentByTag(targetTag)


        if(currentFragmentTag == targetTag && currentFragment != null){
            menuRetouched(menuItem, currentFragment)
            viewChanged(menuItem, currentFragment)
            Log.d(TAG, "#setFragment 再タッチされた")
            return true
        }

        val ft = fragmentManager.beginTransaction()

        if(currentFragment != null){
            //ft.detach(currentFragment)
            ft.hide(currentFragment)
            currentFragment.setMenuVisibility(false)
        }else{
            Log.d(TAG, "#setFragment currentFragmentは存在しなかった")
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

        if(currentFragmentTag == makeTag(R.id.navigation_home)){
            Log.d(TAG, "#setFramgnet currentFragmentTagはHomeを選択していることになっている")
        }else{
            Log.d(TAG, "#setFramgnet currentFragmentTagはHomeは選択されていない")
        }

        ft.commit()
        return true
    }

    private fun makeTag(@IdRes menuId: Int): String{
        return "BottomNavigationAdapter:${menuId}"
    }

    fun saveState(bundle: Bundle){
        bundle.putInt(EXTRA_SELECTED_ITEM_ID, selectedItemId)
    }
}