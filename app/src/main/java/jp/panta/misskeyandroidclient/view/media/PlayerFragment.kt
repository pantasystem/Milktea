package jp.panta.misskeyandroidclient.view.media

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import jp.panta.misskeyandroidclient.MediaActivity
import jp.panta.misskeyandroidclient.R

class PlayerFragment : Fragment(R.layout.fragment_player){

    companion object{
        const val TAG = "PlayerFragment"
        private const val EXTRA_MEDIA_SOURCE_URI = "jp.panta.misskeyandroidclient.view.media.PlayerFragment.EXTRA_MEDIA_SOURCE_URI"
        private const val EXTRA_INDEX = "jp.panta.misskeyandroidclient.view.media.PlayerFragment.extra.INDEX"

        fun newInstance(index: Int, uri: String): PlayerFragment{
            return PlayerFragment().apply{
                arguments = Bundle().apply{
                    putString(EXTRA_MEDIA_SOURCE_URI, uri)
                    putInt(EXTRA_INDEX, index)
                }
            }
        }

        fun newInstance(index: Int, uri: Uri): PlayerFragment{
            return PlayerFragment().apply{
                arguments = Bundle().apply{
                    putString(EXTRA_MEDIA_SOURCE_URI, uri.toString())
                    putInt(EXTRA_INDEX, index)
                }
            }
        }
    }

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
        val simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(requireContext())
        mExoPlayer = simpleExoPlayer

        val mediaSource = ProgressiveMediaSource.Factory(DefaultDataSourceFactory(requireContext(), Util.getUserAgent(requireContext(), getString(R.string.app_name))))
            .createMediaSource(uri)

        simpleExoPlayer.prepare(mediaSource)

        view.findViewById<PlayerView>(R.id.player_view).player = simpleExoPlayer
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