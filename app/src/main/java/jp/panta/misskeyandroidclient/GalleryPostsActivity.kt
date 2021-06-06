package jp.panta.misskeyandroidclient


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.databinding.ActivityGalleryPostsBinding

class GalleryPostsActivity : AppCompatActivity() {

    companion object {
        private const val EDITOR = "gallery_posts_editor"
        private const val GALLERY_POSTS_TAB = "gallery_posts_tab"

        private const val CURRENT_FRAGMENT_TAG = "CURRENT_FRAGMENT"
    }


    val binding: ActivityGalleryPostsBinding by dataBinding()

    private var currentFragmentTag = GALLERY_POSTS_TAB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_gallery_posts)

        val action = intent.action ?: Intent.ACTION_VIEW

        if(action == Intent.ACTION_EDIT) {
            currentFragmentTag = EDITOR
        }
        changeFragment(currentFragmentTag)
    }

    private fun changeFragment(tag: String) {
        //val ft = supportFragmentManager.beginTransaction()
        //ft.replace(R.id.base, )
    }


    override fun onBackPressed() {
        super.onBackPressed()

        if(supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        }else{
            finish()
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(currentFragmentTag, currentFragmentTag)
    }
}