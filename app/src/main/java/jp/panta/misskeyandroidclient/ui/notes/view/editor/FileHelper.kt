package jp.panta.misskeyandroidclient.ui.notes.view.editor

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.pantasystem.milktea.data.model.file.File
import jp.panta.misskeyandroidclient.viewmodel.file.FileListener

object FileHelper{

    @JvmStatic
    @BindingAdapter("previewFiles", "imagePreviewsFileListener")
    fun RecyclerView.setPreviewFiles(files: List<File>?, imagePreviewsFileListener: FileListener?){

        files?: return
        imagePreviewsFileListener?: return
        val adapter = SimpleImagePreviewAdapter(imagePreviewsFileListener)
        adapter.submitList(files)
        this.adapter = adapter
        this.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)

    }
}