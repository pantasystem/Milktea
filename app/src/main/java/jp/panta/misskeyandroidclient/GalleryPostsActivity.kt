package jp.panta.misskeyandroidclient


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.databinding.ActivityGalleryPostsBinding
import jp.panta.misskeyandroidclient.model.TaskState
import jp.panta.misskeyandroidclient.view.gallery.GalleryEditorFragment
import jp.panta.misskeyandroidclient.view.gallery.GalleryPostTabFragment
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.gallery.Action
import jp.panta.misskeyandroidclient.viewmodel.gallery.GalleryPostActionViewModel
import kotlinx.coroutines.flow.onEach

class GalleryPostsActivity : AppCompatActivity() {



    val binding: ActivityGalleryPostsBinding by dataBinding()
    val actionViewModel: GalleryPostActionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_gallery_posts)

        val action = intent.action ?: Intent.ACTION_VIEW

        val ft = supportFragmentManager.beginTransaction()
        if(action == Intent.ACTION_EDIT) {
            ft.replace(R.id.base, GalleryEditorFragment())
        }else{
            ft.replace(R.id.base, GalleryPostTabFragment())
        }
        ft.commit()

        actionViewModel.viewAction.observe(this) {
            when(it) {
                is Action.OpenCreationEditor -> {
                    Log.d("GalleryPostsActivity", "アクションがあった")
                    val t = supportFragmentManager.beginTransaction()
                    t.replace(R.id.base, GalleryEditorFragment())
                    t.addToBackStack(null)
                    t.commit()
                }
            }

        }

    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home ->{
                pop()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun pop() {
        if(supportFragmentManager.backStackEntryCount > 0) {
            Log.d("count", "backStackEntryCount:${supportFragmentManager.backStackEntryCount}")
            supportFragmentManager.popBackStack()
        }else{
            finish()
        }
    }


}