package net.pantasystem.milktea.media

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.api.misskey.OkHttpClientProvider
import javax.inject.Inject

@AndroidEntryPoint
class PlayerFragment : Fragment(R.layout.fragment_player){

    companion object{
        const val TAG = "PlayerFragment"
        private const val EXTRA_MEDIA_SOURCE_URI = "net.pantasystem.milktea.media.PlayerFragment.EXTRA_MEDIA_SOURCE_URI"
        private const val EXTRA_INDEX = "net.pantasystem.milktea.media.PlayerFragment.extra.INDEX"

        fun newInstance(index: Int, uri: String): PlayerFragment {
            return PlayerFragment().apply{
                arguments = Bundle().apply{
                    putString(EXTRA_MEDIA_SOURCE_URI, uri)
                    putInt(EXTRA_INDEX, index)
                }
            }
        }

        fun newInstance(index: Int, uri: Uri): PlayerFragment {
            return PlayerFragment().apply{
                arguments = Bundle().apply{
                    putString(EXTRA_MEDIA_SOURCE_URI, uri.toString())
                    putInt(EXTRA_INDEX, index)
                }
            }
        }
    }

    @Inject
    internal lateinit var okHttpProvider: OkHttpClientProvider

    private var mExoPlayer: ExoPlayer? = null
    private var index: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val extraUri = arguments?.getString(EXTRA_MEDIA_SOURCE_URI)
        Log.d(TAG, "extraUri: $extraUri")
        val uri = if(extraUri == null) null else Uri.parse(extraUri)

        index = arguments?.getInt(EXTRA_INDEX) ?: 0

        if(uri == null){
            Log.e(TAG, "uri must not null")
            return
        }

        val okHttpDataSourceFactory = OkHttpDataSource.Factory(okHttpProvider.get())
        val mediaDataSource = ProgressiveMediaSource.Factory(okHttpDataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri))

        val simpleExoPlayer = ExoPlayer.Builder(requireContext())
            .build()
        view.findViewById<StyledPlayerView>(R.id.player_view).player = simpleExoPlayer

        simpleExoPlayer.setMediaSource(mediaDataSource)
        simpleExoPlayer.prepare()
        simpleExoPlayer.play()

        view.findViewById<SwipeFinishLayout>(R.id.swipeFinishLayout).setOnFinishEventListener {
            requireActivity().finish()
        }

        mExoPlayer = simpleExoPlayer

    }

    override fun onResume() {
        super.onResume()

        val activity = requireActivity()
        if(activity is MediaActivity){
            activity.setCurrentFileIndex(index)
        }
    }

    override fun onStop(){
        super.onStop()

        mExoPlayer?.playWhenReady = false
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mExoPlayer?.release()
    }
}