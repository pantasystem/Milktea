package jp.panta.misskeyandroidclient.view.media

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import jp.panta.misskeyandroidclient.R
import kotlinx.android.synthetic.main.fragment_image.*

class ImageFragment : Fragment(R.layout.fragment_image){

    companion object{
        private const val TAG = "ImageFragment"
        private const val EXTRA_IMAGE_URL = "jp.panta.misskeyandroidclient.view.media.EXTRA_IMAGE_URL"
        fun newInstance(url: String): ImageFragment{
            val bundle = Bundle().apply{
                putString(EXTRA_IMAGE_URL, url)
            }
            return ImageFragment().apply{
                arguments = bundle
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val url = arguments?.getString(EXTRA_IMAGE_URL)

        if(url == null){
            Log.e(TAG, "EXTRA_IMAGE_URL must not null")
            return
        }

        Glide.with(view.context)
            .load(url)
            .into(imageView)
    }
}