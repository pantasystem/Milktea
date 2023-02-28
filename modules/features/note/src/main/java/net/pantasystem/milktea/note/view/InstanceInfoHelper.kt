package net.pantasystem.milktea.note.view

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import net.pantasystem.milktea.common_android.ui.text.DrawableEmojiSpan
import net.pantasystem.milktea.common_android.ui.text.EmojiAdapter
import net.pantasystem.milktea.model.user.User
import kotlin.math.min

object InstanceInfoHelper {

    @JvmStatic
    @BindingAdapter("instanceInfo", "isEnable")
    fun TextView.setInstanceInfo(info: User.InstanceInfo?, isEnable: Boolean? = null) {



        val enable = info?.name != null
                && info.faviconUrl != null
                && isEnable == true
        this.isVisible = enable
        if (enable) {
            val emojiAdapter = EmojiAdapter(this)

            val iconDrawable = DrawableEmojiSpan(emojiAdapter, info?.faviconUrl)
            Glide.with(this)
                .load(info!!.faviconUrl)
                .override(min(this.textSize.toInt(), 640))
                .into(iconDrawable.target)
            text =  SpannableStringBuilder(":${info.faviconUrl}:${info.name}").apply {
                setSpan(iconDrawable, 0, ":${info.faviconUrl}:".length, 0)
            }
            when(val color = info.themeColorNumber.getOrNull()) {
                null -> {

                }
                else -> {
                    val parsedColor = ColorUtils.setAlphaComponent(color, (255 * 0.42).toInt())
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