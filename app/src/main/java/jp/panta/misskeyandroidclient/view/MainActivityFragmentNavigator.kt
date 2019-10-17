package jp.panta.misskeyandroidclient.view

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentManager
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator

/*class MainActivityFragmentNavigator(private val context: Context, private val manager: FragmentManager, private val containerId: Int) : FragmentNavigator(context,manager, containerId){
    override fun navigate(
        destination: Destination,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: Navigator.Extras?
    ): NavDestination? {
        if(manager.isStateSaved){
            return null
        }

        var className = destination.className
        if(className[0] == '.'){
            className = context.packageName + className
        }

        val tag = destination.id.toString()
        val transaction = manager.primaryNavigationFragment

        val currentFragment = manager.primaryNavigationFragment
        if(currentFragment != null){
            transaction
        }
    }
}*/