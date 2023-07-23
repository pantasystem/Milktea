package net.pantasystem.milktea.common_android_ui

import androidx.databinding.BindingAdapter
import dagger.hilt.android.EntryPointAccessors
import net.pantasystem.milktea.common.ui.AvatarIconView
import net.pantasystem.milktea.model.setting.DefaultConfig

object AvatarIconViewBindingAdapter  {
    @JvmStatic
    @BindingAdapter("applyShapeFromConfig")
    fun AvatarIconView.applyShapeFromConfig(apply: Boolean) {
        if (apply) {
            val type = EntryPointAccessors.fromApplication<BindingProvider>(this.context.applicationContext).configRepository().get().getOrElse {
                DefaultConfig.config
            }.avatarIconShapeType.value
            this.setIconShape(type)
        }
    }
}