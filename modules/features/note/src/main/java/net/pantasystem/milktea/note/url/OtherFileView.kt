package net.pantasystem.milktea.note.url

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import net.pantasystem.milktea.model.file.FilePreviewSource
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.R.layout
import net.pantasystem.milktea.note.url.UrlPreviewHelper.setUrlPreviewThumbnail

class OtherFileView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(
    context, attrs, defStyleAttr
) {

    private val fileThumbnailView: ImageView
    private val fileNameView: TextView
    private val fileTypeView: TextView
    init {
        inflate(context, layout.view_other_file, this)
        fileThumbnailView = findViewById(R.id.fileThumbnailView)
        fileNameView = findViewById(R.id.fileNameView)
        fileTypeView = findViewById(R.id.fileTypeView)
    }

    fun setOtherFile(otherFile: FilePreviewSource) {
        fileThumbnailView.setUrlPreviewThumbnail(otherFile.thumbnailUrl)
        fileNameView.text = otherFile.name
        fileTypeView.text = otherFile.type
    }
}