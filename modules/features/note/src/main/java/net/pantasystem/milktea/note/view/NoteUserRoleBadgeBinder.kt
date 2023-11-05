package net.pantasystem.milktea.note.view

import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import net.pantasystem.milktea.common.glide.GlideApp
import net.pantasystem.milktea.model.user.User

object NoteUserRoleBadgeBinder {

    @JvmStatic
    @BindingAdapter("parentView", "userRoleBadges", "iconSizePx")
    fun Flow.setUserRoleBadge(parentView: ConstraintLayout, badges: List<User.BadgeRole>, iconSizePx: Int) {
        val refIds = this.referencedIds?.toList()?: emptyList()

        // 使用しないViewを削除する処理
        while((this.referencedIds?.size ?: 0) > badges.size) {
            val id = this.referencedIds?.lastOrNull() ?: break
            this.removeView(parentView.findViewById(id))
            parentView.removeView(parentView.findViewById(id))
        }

        badges.forEachIndexed { index, badgeRole ->
            val existsView = refIds.getOrNull(index)
                ?.let { parentView.findViewById<ImageView>(it) }

            val imageView = existsView ?: ImageView(parentView.context).apply {
                // height width 20dp
                this.layoutParams = ConstraintLayout.LayoutParams(
                    iconSizePx,
                    iconSizePx,
                )

                // padding
                this.setPadding(4, 0, 4, 0)
            }

            GlideApp.with(parentView.context)
                .load(badgeRole.iconUri)
                .into(imageView)

            if (existsView == null) {
                imageView.id = View.generateViewId()
                parentView.addView(imageView)
                this.addView(imageView)
            }
        }
    }


}