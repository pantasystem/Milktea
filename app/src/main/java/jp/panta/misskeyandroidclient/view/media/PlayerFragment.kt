package jp.panta.misskeyandroidclient.view.media

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import jp.panta.misskeyandroidclient.R
import kotlinx.android.synthetic.main.fragment_player.*

class PlayerFragment : Fragment(R.layout.fragment_player){

    companion object{
        const val TAG = "PlayerFragment"
        private const val EXTRA_MEDIA_SOURCE_URI = "jp.panta.misskeyandroidclient.view.media.PlayerFragment.EXTRA_MEDIA_SOURCE_URI"

        fun newInstance(uri: String): PlayerFragment{
            return PlayerFragment().apply{
                arguments = Bundle().apply{
                    putString(EXTRA_MEDIA_SOURCE_URI, uri)
                }
            }
        }

        fun newInstance(uri: Uri): PlayerFragment{
            return PlayerFragment().apply{
                arguments = Bundle().apply{
                    putString(EXTRA_MEDIA_SOURCE_URI, uri.toString())
                }
            }
        }
    }

    private var mExoPlayer: ExoPlayer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val extraUri = arguments?.getString(EXTRA_MEDIA_SOURCE_URI)
        Log.d(TAG, "extraUri: $extraUri")
        val uri = if(extraUri == null) null else Uri.parse(extraUri)

        if(uri == null){
            Log.e(TAG, "uri must not null")
            return
        }
        val simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this.context)
        mExoPlayer = simpleExoPlayer

        val mediaSource = ProgressiveMediaSource.Factory(DefaultDataSourceFactory(this.context, Util.getUserAgent(this.context, context?.getString(R.string.app_name))))
            .createMediaSource(uri)

        simpleExoPlayer.prepare(mediaSource)
        player_view.player = simpleExoPlayer
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