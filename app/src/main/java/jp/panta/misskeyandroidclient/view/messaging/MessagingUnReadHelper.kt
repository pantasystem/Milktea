package jp.panta.misskeyandroidclient.view.messaging

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.badge.BadgeDrawable

object MessagingUnReadHelper{

    @JvmStatic
    @BindingAdapter("isRead", "unreadCount")
    fun TextView.setUnReadBadge(isRead: Boolean?, unreadCount: Int?){
        if(isRead == true){
            this.visibility = View.GONE

        }else{
            this.visibility = View.VISIBLE
        }

    }
}