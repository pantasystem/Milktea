package jp.panta.misskeyandroidclient.ui

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.core.view.forEach
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationView

@SuppressLint("RestrictedApi")
fun BottomNavigationView.setLongPressListenerOnNavigationItem(
    itemId: Int,
    onLongClicked: () -> Boolean
) {
    this.getChildAt(0)?.also { view ->
        if (view is ViewGroup) {
            view.forEach { child ->
                if (child is BottomNavigationItemView) {
                    if (child.itemData?.itemId == itemId) {
                        child.setOnLongClickListener {
                            onLongClicked()
                        }
                    }
                }
            }
        }
    }
}