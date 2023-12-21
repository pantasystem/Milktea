package net.pantasystem.milktea.common_android.ui

import android.widget.TextView
import androidx.databinding.BindingAdapter
import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.common_android.resource.getString

@BindingAdapter("stringSource")
fun TextView.setStringSource(source: StringSource?) {
    text = source?.getString(context)
}