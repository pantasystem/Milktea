package net.pantasystem.milktea.favorite

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.common_android_ui.error.UserActionAppGlobalErrorListener
import net.pantasystem.milktea.favorite.databinding.ActivityFavoriteBinding
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.note.view.NoteActionHandler
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import javax.inject.Inject


@AndroidEntryPoint
class FavoriteActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityFavoriteBinding

    val notesViewModel by viewModels<NotesViewModel>()

    @Inject
    lateinit var settingStore: SettingStore

    @Inject
    lateinit var pageableFragmentFactory: PageableFragmentFactory

    @Inject
    internal lateinit var applyTheme: ApplyTheme

    @Inject
    internal lateinit var userActionAppGlobalErrorListener: UserActionAppGlobalErrorListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        userActionAppGlobalErrorListener(lifecycle, supportFragmentManager)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_favorite)
        setSupportActionBar(mBinding.favoriteToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.favorite)


        NoteActionHandler(
            this.supportFragmentManager,
            this,
            this,
            notesViewModel,
        ).initViewModelListener()
        val fragment = pageableFragmentFactory.create(
            Pageable.Favorite
        )

        val manager = supportFragmentManager.beginTransaction()
        manager.replace(R.id.favorite_fragment_base, fragment)
        manager.commit()


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
