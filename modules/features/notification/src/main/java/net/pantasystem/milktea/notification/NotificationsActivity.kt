package net.pantasystem.milktea.notification

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common.ui.ToolbarSetter
import net.pantasystem.milktea.note.view.NoteActionHandler
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import net.pantasystem.milktea.notification.databinding.ActivityNotificationsBinding
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class NotificationsActivity : AppCompatActivity(), ToolbarSetter {

    @Inject
    lateinit var settingStore: SettingStore

    @Inject
    lateinit var applyTheme: ApplyTheme

    val binding: ActivityNotificationsBinding by dataBinding()
    private val notesViewModel by viewModels<NotesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        setContentView(R.layout.activity_notifications)

        NoteActionHandler(
            this.supportFragmentManager,
            this,
            this,
            notesViewModel,
        ).initViewModelListener()

        showNotificationFragment()
    }

    private fun showNotificationFragment() {
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.notificationBase, NotificationMentionFragment())
        ft.commit()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setToolbar(toolbar: Toolbar, visibleTitle: Boolean) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(visibleTitle)
    }
}
