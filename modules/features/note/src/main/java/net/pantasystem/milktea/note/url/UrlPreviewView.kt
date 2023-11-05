package net.pantasystem.milktea.note.url

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import net.pantasystem.milktea.model.url.UrlPreview
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.url.UrlPreviewHelper.setSiteIcon
import net.pantasystem.milktea.note.url.UrlPreviewHelper.setUrlPreviewThumbnail

class UrlPreviewView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
): ConstraintLayout(
    context, attrs, defStyleAttr
) {

    private val siteThumbnailView: ImageView
    private val siteTitleView: TextView
    private val siteDescription: TextView
    private val siteIconView: ImageView
    private val siteNameView: TextView

    init {
        inflate(context, R.layout.view_url_preview, this)
        siteThumbnailView = findViewById(R.id.siteThumbnailView)
        siteTitleView = findViewById(R.id.siteTitleView)
        siteDescription = findViewById(R.id.siteDescription)
        siteIconView = findViewById(R.id.siteIconView)
        siteNameView = findViewById(R.id.siteNameView)
    }

    fun setUrlPreview(urlPreview: UrlPreview) {
        siteThumbnailView.setUrlPreviewThumbnail(urlPreview.thumbnail)
        siteTitleView.text = urlPreview.title
        siteDescription.text = urlPreview.description
        siteIconView.setSiteIcon(urlPreview.icon)
        siteNameView.text = urlPreview.siteName
    }
}