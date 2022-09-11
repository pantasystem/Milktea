package net.pantasystem.milktea.gallery


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.gallery.databinding.ActivityGalleryPostsBinding
import net.pantasystem.milktea.gallery.viewmodel.Action
import net.pantasystem.milktea.gallery.viewmodel.GalleryPostActionViewModel
import javax.inject.Inject

@AndroidEntryPoint
class GalleryPostsActivity : AppCompatActivity() {



    val binding: ActivityGalleryPostsBinding by dataBinding()
    private val actionViewModel: GalleryPostActionViewModel by viewModels()

    @Inject
    lateinit var setTheme: ApplyTheme

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme.invoke()
        setContentView(R.layout.activity_gallery_posts)

        val action = intent.action ?: Intent.ACTION_VIEW

        val ft = supportFragmentManager.beginTransaction()
        if(action == Intent.ACTION_EDIT) {
            ft.replace(R.id.base, GalleryEditorFragment())
        }else{
            ft.replace(R.id.base, GalleryPostTabFragment())
        }
        ft.commit()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                actionViewModel.viewAction.collect {
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