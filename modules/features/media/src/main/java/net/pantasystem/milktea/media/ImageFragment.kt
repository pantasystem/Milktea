package net.pantasystem.milktea.media

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.wada811.databinding.dataBinding
import net.pantasystem.milktea.media.databinding.FragmentImageBinding

class ImageFragment : Fragment(R.layout.fragment_image){

    companion object{
        private const val TAG = "ImageFragment"
        private const val EXTRA_IMAGE_URL = "jp.panta.misskeyandroidclient.ui.media.EXTRA_IMAGE_URL"
        private const val EXTRA_IMAGE_URI = "jp.panta.misskeyandroidclient.ui.media.EXTRA_IMAGE_URI"
        private const val EXTRA_INDEX = "jp.panta.misskeyandroidclient.ui.media.EXTRA_INDEX"
        fun newInstance(index: Int, url: String): ImageFragment {
            val bundle = Bundle().apply{
                putString(EXTRA_IMAGE_URL, url)
                putInt(EXTRA_INDEX, index)
            }
            return ImageFragment().apply{
                arguments = bundle
            }
        }

        fun newInstance(index: Int, uri: Uri): ImageFragment {
            return ImageFragment().apply{
                arguments = Bundle().apply{
                    putString(EXTRA_IMAGE_URI, uri.toString())
                    putInt(EXTRA_INDEX, index)
                }
            }
        }

        fun newInstance(index: Int, file: File): ImageFragment {
            return newInstance(index, file.path!!)
        }
    }

    var index = 0
    val binding: FragmentImageBinding by dataBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val url = arguments?.getString(EXTRA_IMAGE_URL)
        val extraUri = arguments?.getString(EXTRA_IMAGE_URI)
        val uri = if(extraUri == null) null else Uri.parse(extraUri)
        index = arguments?.getInt(EXTRA_INDEX)?: 0

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
        }.into(binding.imageView)
    }

    override fun onResume() {
        super.onResume()

        val activity = requireActivity()
        if(activity is MediaActivity){
            activity.setCurrentFileIndex(index)
        }

    }
}