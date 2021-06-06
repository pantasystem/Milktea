package jp.panta.misskeyandroidclient


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.databinding.ActivityGalleryPostsBinding
import jp.panta.misskeyandroidclient.view.gallery.GalleryPostTabFragment

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
        //changeFragment(currentFragmentTag)
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.base, GalleryPostTabFragment())
        ft.commit()
    }



    override fun onBackPressed() {
        super.onBackPressed()

        pop()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home ->{
                if(currentFragmentTag == EDITOR) {
                    supportFragmentManager.popBackStack(GALLERY_POSTS_TAB, 0)
                }else{
                    finish()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun pop() {
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