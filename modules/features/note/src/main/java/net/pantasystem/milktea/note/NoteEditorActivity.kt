package net.pantasystem.milktea.note

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.note.databinding.ActivityNoteEditorBinding
import net.pantasystem.milktea.note.editor.NoteEditorFragment
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorSavedStateKey
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorViewModel
import javax.inject.Inject


@AndroidEntryPoint
class NoteEditorActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_REPLY_TO_NOTE_ID =
            "jp.panta.misskeyandroidclient.EXTRA_REPLY_TO_NOTE_ID"
        private const val EXTRA_QUOTE_TO_NOTE_ID =
            "jp.panta.misskeyandroidclient.EXTRA_QUOTE_TO_NOTE_ID"
        private const val EXTRA_DRAFT_NOTE_ID = "jp.panta.misskeyandroidclient.EXTRA_DRAFT_NOTE"
        private const val EXTRA_ACCOUNT_ID = "jp.panta.misskeyandroidclient.EXTRA_ACCOUNT_ID"

        private const val EXTRA_MENTIONS = "EXTRA_MENTIONS"
        private const val EXTRA_CHANNEL_ID = "EXTRA_CHANNEL_ID"
        private const val EXTRA_SPECIFIED_ACCOUNT_ID = "EXTRA_SPECIFIED_ACCOUNT_ID"

        fun newBundle(
            context: Context,
            replyTo: Note.Id? = null,
            quoteTo: Note.Id? = null,
            draftNoteId: Long? = null,
            mentions: List<String>? = null,
            channelId: Channel.Id? = null,
            accountId: Long? = null,
            text: String? = null,
        ): Intent {
            return Intent(context, NoteEditorActivity::class.java).apply {
                replyTo?.let {
                    putExtra(EXTRA_REPLY_TO_NOTE_ID, replyTo.noteId)
                    putExtra(EXTRA_ACCOUNT_ID, replyTo.accountId)
                }

                quoteTo?.let {
                    putExtra(EXTRA_QUOTE_TO_NOTE_ID, quoteTo.noteId)
                    putExtra(EXTRA_ACCOUNT_ID, quoteTo.accountId)
                }


                draftNoteId?.let {
                    putExtra(EXTRA_DRAFT_NOTE_ID, it)
                }

                mentions?.let {
                    putExtra(EXTRA_MENTIONS, it.toTypedArray())
                }

                channelId?.let {
                    putExtra(EXTRA_CHANNEL_ID, it.channelId)
                    putExtra(EXTRA_ACCOUNT_ID, it.accountId)
                }

                accountId?.let {
                    putExtra(EXTRA_SPECIFIED_ACCOUNT_ID, it)
                }

                text?.let {
                    putExtra(NoteEditorSavedStateKey.Text.name, it)
                }
            }
        }
    }

    val mViewModel: NoteEditorViewModel by viewModels()

    private val binding: ActivityNoteEditorBinding by dataBinding()

    @Inject
    internal lateinit var applyTheme: ApplyTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        setContentView(R.layout.activity_note_editor)

        binding.lifecycleOwner = this

        var text: String? = null

        when {
            intent?.action == Intent.ACTION_SEND -> {
                if (intent.type?.startsWith("text/") == true) {
                    text = intent.getStringExtra(Intent.EXTRA_TEXT)
                }
                if (intent.type?.startsWith("image/") == true) {
                    handleSendImage(intent)
                }
            }
            intent.action == Intent.ACTION_SEND_MULTIPLE && intent.type?.startsWith("image/") == true -> {
                handleSendImages(intent)
            }
            else -> Unit
        }

        val accountId: Long? =
            if (intent.getLongExtra(EXTRA_ACCOUNT_ID, -1) == -1L) null else intent.getLongExtra(
                EXTRA_ACCOUNT_ID,
                -1
            )
        val replyToNoteId = intent.getStringExtra(EXTRA_REPLY_TO_NOTE_ID)?.let {
            requireNotNull(accountId)
            Note.Id(accountId, it)
        }
        val quoteToNoteId = intent.getStringExtra(EXTRA_QUOTE_TO_NOTE_ID)?.let {
            requireNotNull(accountId)
            Note.Id(accountId, it)
        }

        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID)?.let {
            requireNotNull(accountId)
            Channel.Id(accountId, it)
        }

        val draftNoteId = intent.getLongExtra(EXTRA_DRAFT_NOTE_ID, -1).let {
            if (it == -1L) null else it
        }

        val specifiedAccountId = intent.getLongExtra(EXTRA_SPECIFIED_ACCOUNT_ID, -1).takeIf {
            it > 0
        }
        if (savedInstanceState == null) {
            val mentions = intent.getStringArrayExtra(EXTRA_MENTIONS)?.toList()
            val fragment = NoteEditorFragment.newInstance(
                replyTo = replyToNoteId,
                quoteTo = quoteToNoteId,
                draftNoteId = draftNoteId,
                mentions = mentions,
                channelId = channelId,
                text = text,
                specifiedAccountId = specifiedAccountId,
            )
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentBase, fragment)
            ft.commit()
        }

    }

    @Suppress("DEPRECATION")
    private fun handleSendImage(intent: Intent) {
        (intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri)?.let { uri ->
            addFileFromUri(uri)
        }
    }

    @Suppress("DEPRECATION")
    private fun handleSendImages(intent: Intent) {
        intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.mapNotNull {
                it as? Uri
            }?.distinct()?.map(::addFileFromUri)
    }

    private fun addFileFromUri(uri: Uri) {
        val size = mViewModel.fileTotal()
        if (size > mViewModel.maxFileCount.value) {
            Log.d("NoteEditorActivity", "失敗しました")
        } else {
            mViewModel.addFile(uri)
        }
    }

}
