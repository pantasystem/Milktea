package jp.panta.misskeyandroidclient.view.media

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.file.File
import kotlinx.android.synthetic.main.fragment_image.*

class ImageFragment : Fragment(R.layout.fragment_image){

    companion object{
        private const val TAG = "ImageFragment"
        private const val EXTRA_IMAGE_URL = "jp.panta.misskeyandroidclient.view.media.EXTRA_IMAGE_URL"
        private const val EXTRA_IMAGE_URI = "jp.panta.misskeyandroidclient.view.media.EXTRA_IMAGE_URI"
        fun newInstance(url: String): ImageFragment{
            val bundle = Bundle().apply{
                putString(EXTRA_IMAGE_URL, url)
            }
            return ImageFragment().apply{
                arguments = bundle
            }
        }

        fun newInstance(uri: Uri): ImageFragment{
            return ImageFragment().apply{
                arguments = Bundle().apply{
                    putString(EXTRA_IMAGE_URI, uri.toString())
                }
            }
        }

        fun newInstance(file: File): ImageFragment{
            return newInstance(file.path)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val url = arguments?.getString(EXTRA_IMAGE_URL)
        val extraUri = arguments?.getString(EXTRA_IMAGE_URI)
        val uri = if(extraUri == null) null else Uri.parse(extraUri)

        if(url == null && uri == null){
            Log.e(TAG, "EXTRA_IMAGE_URL must not null")
            return
        }

        Glide.with(view.context).let {
            if (uri == null) {
                it.load(url)
            } else {
                it.load(uri)
            }
        }.into(imageView)
    }
}