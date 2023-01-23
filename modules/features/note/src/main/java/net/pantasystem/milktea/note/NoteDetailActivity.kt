package net.pantasystem.milktea.note

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.TaskStackBuilder
import androidx.lifecycle.ViewModelProvider
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.ui.ApplyMenuTint
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_android.ui.Activities
import net.pantasystem.milktea.common_android.ui.getParentActivity
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.common_android_ui.account.viewmodel.AccountViewModel
import net.pantasystem.milktea.common_navigation.MainNavigation
import net.pantasystem.milktea.common_viewmodel.confirm.ConfirmViewModel
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.note.databinding.ActivityNoteDetailBinding
import net.pantasystem.milktea.note.detail.NoteDetailPagerFragment
import net.pantasystem.milktea.note.detail.viewmodel.NoteDetailPagerViewModel
import net.pantasystem.milktea.note.view.ActionNoteHandler
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import javax.inject.Inject

@AndroidEntryPoint
class NoteDetailActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_NOTE_ID = "jp.panta.misskeyandroidclient.EXTRA_NOTE_ID"
        private const val EXTRA_ACCOUNT_ID = "jp.panta.misskeyandroidclient.EXTRA_ACCOUNT_ID"

        private const val TAG = "NoteDetailActivity"

        const val EXTRA_IS_MAIN_ACTIVE = "jp.panta.misskeyandroidclient.EXTRA_IS_MAIN_ACTIVE"

        fun newIntent(context: Context, noteId: Note.Id, fromPageable: Pageable? = null): Intent {
            return Intent(context, NoteDetailActivity::class.java).apply {
                putExtra(EXTRA_NOTE_ID, noteId.noteId)
                putExtra(EXTRA_ACCOUNT_ID, noteId.accountId)
                putExtra(NoteDetailPagerViewModel.EXTRA_FROM_PAGEABLE, fromPageable)
            }
        }
    }

    private var mNoteId: String? = null
    private var mAccountId: Long? = null

    private var mIsMainActive: Boolean = true

    private var mParentActivity: Activities? = null

    private val binding: ActivityNoteDetailBinding by dataBinding()
    val notesViewModel: NotesViewModel by viewModels()

    @Inject
    internal lateinit var settingStore: SettingStore

    @Inject
    internal lateinit var pageableFragmentFactory: PageableFragmentFactory


    private val accountViewModel: AccountViewModel by viewModels()

    @Inject
    internal lateinit var setTheme: ApplyTheme

    @Inject
    lateinit var mainNavigation: MainNavigation

    @Inject
    lateinit var setMenuTint: ApplyMenuTint

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_note_detail)

        setSupportActionBar(binding.noteDetailToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mParentActivity = intent.getParentActivity()

        val noteId = intent.getStringExtra(EXTRA_NOTE_ID)
            ?: intent.data?.path?.replace("/notes/", "")
        Log.d(TAG, "受け取ったnoteId: $noteId")
        mNoteId = noteId
        mAccountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1).let {
            if (it == -1L) null else it
        }

        mIsMainActive = intent.getBooleanExtra(EXTRA_IS_MAIN_ACTIVE, true)

        ActionNoteHandler(
            this,
            notesViewModel,
            ViewModelProvider(this)[ConfirmViewModel::class.java],
            settingStore
        ).initViewModelListener()
        val ft = supportFragmentManager.beginTransaction()

        val fragment = NoteDetailPagerFragment.newInstance(noteId!!,
            intent.getSerializableExtra(NoteDetailPagerViewModel.EXTRA_FROM_PAGEABLE) as? Pageable, mAccountId)
        ft.replace(
            R.id.fragment_base,
            fragment
        )
        ft.commit()


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.note_detail_menu, menu)
        setMenuTint(this, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finishAndGoToMainActivity()
            }
            R.id.nav_add_to_tab -> {
                addToTab()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun finishAndGoToMainActivity() {
        when (mParentActivity) {
            Activities.ACTIVITY_OUT_APP -> {
                val upIntent = mainNavigation.newIntent(Unit)
                upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                if (shouldUpRecreateTask(upIntent)) {
                    TaskStackBuilder.create(this)
                        .addNextIntentWithParentStack(upIntent)
                        .startActivities()
                    finish()
                } else {
                    navigateUpTo(upIntent)
                }
            }
            Activities.ACTIVITY_IN_APP -> {
                finish()
            }

            else -> {}
        }
        if (!mIsMainActive) {

            startActivity(mainNavigation.newIntent(Unit))
        }
        finish()
    }


    private fun addToTab() {
        val title = getString(R.string.detail)

        accountViewModel.addPage(
            Page(
                -1,
                title,
                pageable = Pageable.Show(mNoteId!!),
                weight = 0
            )
        )
    }
}
