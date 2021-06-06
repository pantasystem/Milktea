package jp.panta.misskeyandroidclient


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.databinding.ActivityGalleryPostsBinding

class GalleryPostsActivity : AppCompatActivity() {



    val binding: ActivityGalleryPostsBinding by dataBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_gallery_posts)

        val action = intent.action ?: Intent.ACTION_VIEW

    }
}