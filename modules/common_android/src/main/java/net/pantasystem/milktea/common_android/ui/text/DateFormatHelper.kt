package net.pantasystem.milktea.common_android.ui.text

import android.text.SpannableStringBuilder
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.pantasystem.milktea.common.glide.GlideApp
import net.pantasystem.milktea.common.ui.SimpleElapsedTime
import net.pantasystem.milktea.common_android.R
import net.pantasystem.milktea.model.notes.Visibility
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

object DateFormatHelper {

    @BindingAdapter("dateOnly")
    @JvmStatic
    fun TextView.setDateOnly(dateOnly: Date?) {
        val date = dateOnly ?: Date()
        val sdf = SimpleDateFormat("yyyy/M/d", Locale.getDefault())
        this.text = sdf.format(date)
    }

    @BindingAdapter("timeOnly")
    @JvmStatic
    fun TextView.setTimeOnly(timeOnly: Date?) {
        val date = timeOnly ?: Date()
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        this.text = sdf.format(date)
    }



    @BindingAdapter("elapsedTime")
    @JvmStatic
    fun TextView.setElapsedTime(elapsedTime: Instant?) {

        this.text = GetElapsedTimeStringSource(
            SimpleElapsedTime(
                elapsedTime ?: Clock.System.now()
            )
        ).getString(context)
    }

    @BindingAdapter("elapsedTime", "visibility")
    @JvmStatic
    fun TextView.setElapsedTimeAndVisibility(elapsedTime: Instant?, visibility: Visibility?) {
        val visibilityIcon = when(visibility ?: Visibility.Public(false)) {
            is Visibility.Followers -> R.drawable.ic_lock_black_24dp
            is Visibility.Home -> R.drawable.ic_home_black_24dp
            is Visibility.Public -> null
            is Visibility.Specified -> R.drawable.ic_email_black_24dp
            is Visibility.Limited -> R.drawable.ic_groups
            Visibility.Mutual -> R.drawable.ic_sync_alt_24px
            Visibility.Personal -> R.drawable.ic_person_black_24dp
        }
        val text = GetElapsedTimeStringSource(
            SimpleElapsedTime(
                elapsedTime ?: Clock.System.now()
            )
        ).getString(context)

        this.text = if (visibilityIcon == null) {
            text
        } else {
            val target = "$text visibility"
            SpannableStringBuilder(target).apply {
                val drawable = ContextCompat.getDrawable(context, visibilityIcon)
                drawable?.setTint(currentTextColor)
                val span = DrawableEmojiSpan(EmojiAdapter(this@setElapsedTimeAndVisibility))
                setSpan(span, text.length + 1, target.length,0)
                GlideApp.with(this@setElapsedTimeAndVisibility)
                    .load(drawable)
                    .override(min(textSize.toInt(), 640))
                    .into(DrawableEmojiTarget(span))
            }
        }
    }

    @BindingAdapter("createdAt")
    @JvmStatic
    fun TextView.setCreatedAt(createdAt: Instant?) {
        val date = createdAt ?: Clock.System.now()
        val javaDate = Date(date.toEpochMilliseconds())
        this.text = SimpleDateFormat.getDateTimeInstance().format(javaDate)
    }
}