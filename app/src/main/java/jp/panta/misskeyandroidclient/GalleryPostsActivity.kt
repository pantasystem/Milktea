package jp.panta.misskeyandroidclient


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.databinding.ActivityGalleryPostsBinding
import jp.panta.misskeyandroidclient.ui.gallery.GalleryEditorFragment
import jp.panta.misskeyandroidclient.ui.gallery.GalleryPostTabFragment
import jp.panta.misskeyandroidclient.ui.gallery.viewmodel.Action
import jp.panta.misskeyandroidclient.ui.gallery.viewmodel.GalleryPostActionViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@AndroidEntryPoint
class GalleryPostsActivity : AppCompatActivity() {



    val binding: ActivityGalleryPostsBinding by dataBinding()
    private val actionViewModel: GalleryPostActionViewModel by viewModels()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
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