package jp.panta.misskeyandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.view.media.ImageFragment

class MediaActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_IMAGE_URL = "jp.panta.misskeyandroidclient.MediaActivity.EXTRA_IMAGE_URL"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media)

        val url: String? = intent.getStringExtra(EXTRA_IMAGE_URL)

        if(url != null){
            val fragment = ImageFragment.newInstance(url)

            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.mediaBase, fragment)
            ft.commit()
        }


    }
}
