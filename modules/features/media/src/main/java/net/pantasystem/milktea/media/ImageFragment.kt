package net.pantasystem.milktea.media

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.wada811.databinding.dataBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.common.glide.GlideApp
import net.pantasystem.milktea.media.databinding.FragmentImageBinding

class ImageFragment : Fragment(R.layout.fragment_image) {

    companion object {
        private const val EXTRA_INDEX = "jp.panta.misskeyandroidclient.ui.media.EXTRA_INDEX"
        fun newInstance(index: Int, url: String, thumbnailUrl: String? = null): ImageFragment {
            val bundle = Bundle().apply {
                putString(ImageViewModel.EXTRA_IMAGE_URL, url)
                putInt(EXTRA_INDEX, index)
                putString(ImageViewModel.EXTRA_IMAGE_THUMBNAIL_URL, thumbnailUrl)
            }
            return ImageFragment().apply {
                arguments = bundle
            }
        }

        fun newInstance(index: Int, uri: Uri): ImageFragment {
            return ImageFragment().apply {
                arguments = Bundle().apply {
                    putString(ImageViewModel.EXTRA_IMAGE_URI, uri.toString())
                    putInt(EXTRA_INDEX, index)
                }
            }
        }

        fun newInstance(index: Int, file: File): ImageFragment {
            return newInstance(index, file.path!!, file.thumbnailUrl)
        }
    }

    var index = 0
    val binding: FragmentImageBinding by dataBinding()

    private val viewModel by viewModels<ImageViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        index = arguments?.getInt(EXTRA_INDEX) ?: 0

        binding.swipeFinishLayout.setOnFinishEventListener {
            requireActivity().finish()
        }


        GlideApp.with(view)
            .load(viewModel.getUri())
            .addListener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    viewModel.onLoadFailed()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    viewModel.onResourceReady()
                    return false
                }
            })
            .let {
                val thumbnailUrl = viewModel.getThumbnailUrl()
                val uri = viewModel.getUri()
                if (thumbnailUrl != null && uri != thumbnailUrl)
                    it.thumbnail(
                        GlideApp.with(view).load(thumbnailUrl).fitCenter()
                    )
                else
                    it
            }
            .into(binding.imageView)

        viewModel.isImageLoading.onEach {
            binding.progressBar.isVisible = it
        }.flowWithLifecycle(lifecycle).launchIn(lifecycleScope)
    }

    override fun onResume() {
        super.onResume()

        val activity = requireActivity()
        if (activity is MediaActivity) {
            activity.setCurrentFileIndex(index)
        }

    }
}