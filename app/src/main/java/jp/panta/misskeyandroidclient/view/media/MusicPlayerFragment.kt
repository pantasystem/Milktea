package jp.panta.misskeyandroidclient.view.media

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.drive.FileProperty

class MusicPlayerFragment : Fragment(R.layout.fragment_music_player){

    companion object{
        fun newInstance(media: FileProperty): MusicPlayerFragment{
            return MusicPlayerFragment().apply{
                arguments = Bundle().apply{

                }
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val url = ""
        val mp = MediaPlayer()
        mp.setDataSource(url)


    }
}