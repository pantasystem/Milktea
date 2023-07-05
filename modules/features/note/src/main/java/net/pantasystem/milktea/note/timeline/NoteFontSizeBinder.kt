package net.pantasystem.milktea.note.timeline

import android.widget.TextView
import net.pantasystem.milktea.note.databinding.ItemSimpleNoteBinding

class NoteFontSizeBinder(
    val userInfoViews: HeaderViews,
    val contentViews: ContentViews,
    val quoteToUserInfoViews: HeaderViews,
    val quoteToContentViews: ContentViews,
    val replyToHeaderViews: HeaderViews? = null,
    val replyToContentViews: ContentViews? = null,
) {

    class HeaderViews(
        val nameView: TextView,
        val userNameView: TextView,
        val elapsedTimeView: TextView?,
    )

    class ContentViews(
        val cwView: TextView,
        val textView: TextView,
    )

    companion object {
        fun from(binding: ItemSimpleNoteBinding): NoteFontSizeBinder {
            return NoteFontSizeBinder(
                userInfoViews = HeaderViews(
                    nameView = binding.mainName,
                    userNameView = binding.subName,
                    elapsedTimeView = binding.elapsedTime
                ),
                contentViews = ContentViews(
                    cwView = binding.cw,
                    textView = binding.text
                ),
                quoteToUserInfoViews = HeaderViews(
                    nameView = binding.subNoteMainName,
                    userNameView = binding.subNoteSubName,
                    elapsedTimeView = null,
                ),
                quoteToContentViews = ContentViews(
                    cwView = binding.subCw,
                    textView = binding.subNoteText
                )
            )
        }
    }

    fun bind(
        headerFontSize: Float,
        contentFontSize: Float,
    ) {
        bind(
            header = userInfoViews,
            content = contentViews,
            headerFontSize = headerFontSize,
            contentFontSize = contentFontSize
        )
        bind(
            header = quoteToUserInfoViews,
            content = quoteToContentViews,
            headerFontSize = headerFontSize,
            contentFontSize = contentFontSize,
        )


        if (replyToContentViews != null && replyToHeaderViews != null) {
            bind(
                header = replyToHeaderViews,
                content = replyToContentViews,
                headerFontSize = headerFontSize,
                contentFontSize = contentFontSize,
            )
        }
    }

    private fun bind(
        header: HeaderViews,
        content: ContentViews,
        headerFontSize: Float,
        contentFontSize: Float,
    ) {
        header.elapsedTimeView?.textSize = headerFontSize
        header.userNameView.textSize = headerFontSize
        header.nameView.textSize = headerFontSize
        content.cwView.textSize = contentFontSize
        content.textView.textSize = contentFontSize
    }
}