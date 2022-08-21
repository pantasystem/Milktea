package jp.panta.misskeyandroidclient

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.databinding.ActivityFavoriteBinding
import net.pantasystem.milktea.common_android.ui.PageableFragmentFactory
import jp.panta.misskeyandroidclient.ui.notes.view.ActionNoteHandler
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.confirm.ConfirmViewModel
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.model.account.page.Pageable
import javax.inject.Inject


@AndroidEntryPoint
class FavoriteActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityFavoriteBinding

    val notesViewModel by viewModels<NotesViewModel>()

    @Inject
    lateinit var settingStore: SettingStore

    @Inject
    lateinit var pageableFragmentFactory: PageableFragmentFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_favorite)
        setSupportActionBar(mBinding.favoriteToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.favorite)


        ActionNoteHandler(
            this,
            notesViewModel,
            ViewModelProvider(this)[ConfirmViewModel::class.java],
            settingStore
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
