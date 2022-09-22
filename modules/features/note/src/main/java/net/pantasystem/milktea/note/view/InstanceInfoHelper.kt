package net.pantasystem.milktea.note.view

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import dagger.hilt.android.EntryPointAccessors
import net.pantasystem.milktea.common_android.ui.text.DrawableEmojiSpan
import net.pantasystem.milktea.common_android.ui.text.EmojiAdapter
import net.pantasystem.milktea.common_android_ui.BindingProvider
import net.pantasystem.milktea.model.user.User

object InstanceInfoHelper {

    @JvmStatic
    @BindingAdapter("instanceInfo")
    fun TextView.setInstanceInfo(info: User.InstanceInfo?) {
        val provider = EntryPointAccessors.fromApplication(this.context.applicationContext, BindingProvider::class.java)

        val enable = info?.name != null
                && info.iconUrl != null
                && provider.settingStore().configState.value.isEnableInstanceTicker
        this.isVisible = enable
        if (enable) {
            val emojiAdapter = EmojiAdapter(this)

            val iconDrawable = DrawableEmojiSpan(emojiAdapter)
            Glide.with(this)
                .load(info!!.iconUrl)
                .override(this.textSize.toInt())
                .into(iconDrawable.target)
            text =  SpannableStringBuilder(":${info.iconUrl}:${info.name}").apply {
                setSpan(iconDrawable, 0, ":${info.iconUrl}:".length, 0)
            }
            when(val color = info.themeColor) {
                null -> {

                }
                else -> {
                    val parsedColor = ColorUtils.setAlphaComponent(Color.parseColor(color), (255 * 0.42).toInt())
                    setBackgroundColor(parsedColor)
                    val isDark = ColorUtils.calculateLuminance(parsedColor) < 0.5
                    if (isDark) {
                        setTextColor(Color.WHITE)
                    } else {
                        setTextColor(Color.BLACK)
                    }
                }
            }
        }
    }
}