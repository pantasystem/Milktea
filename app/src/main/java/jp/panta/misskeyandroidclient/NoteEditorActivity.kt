package jp.panta.misskeyandroidclient

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.databinding.ActivityNoteEditorBinding
import jp.panta.misskeyandroidclient.ui.notes.view.editor.NoteEditorFragment
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.editor.NoteEditorViewModel
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.notes.Note


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

        fun newBundle(
            context: Context,
            replyTo: Note.Id? = null,
            quoteTo: Note.Id? = null,
            draftNoteId: Long? = null,
            mentions: List<String>? = null,
            channelId: Channel.Id? = null,
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

            }
        }
    }

    val mViewModel: NoteEditorViewModel by viewModels()

    private val binding: ActivityNoteEditorBinding by dataBinding()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_note_editor)

        binding.lifecycleOwner = this

        var text: String? = null
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("text/") == true) {
            text = intent.getStringExtra(Intent.EXTRA_TEXT)
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

        if (savedInstanceState == null) {
            val mentions = intent.getStringArrayExtra(EXTRA_MENTIONS)?.toList()
            val fragment = NoteEditorFragment.newInstance(
                replyTo = replyToNoteId,
                quoteTo = quoteToNoteId,
                draftNoteId = draftNoteId,
                mentions = mentions,
                channelId = channelId,
                text = text
            )
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.fragmentBase, fragment)
            ft.commit()
        }

    }


}
