package jp.panta.misskeyandroidclient.view.notes

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import jp.panta.misskeyandroidclient.MediaActivity
import jp.panta.misskeyandroidclient.NoteDetailActivity
import jp.panta.misskeyandroidclient.NoteEditorActivity
import jp.panta.misskeyandroidclient.UserDetailActivity
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.view.notes.reaction.ReactionSelectionDialog
import jp.panta.misskeyandroidclient.view.notes.reaction.choices.ReactionInputDialog
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import jp.panta.misskeyandroidclient.viewmodel.notes.media.FileViewData
import jp.panta.misskeyandroidclient.viewmodel.notes.media.MediaViewData

class ActionNoteHandler(
    val activity: AppCompatActivity,
    val mNotesViewModel: NotesViewModel
) {
    /*init{
        initViewModelListener()
    }*/

    private val replyTargetObserver = Observer<PlaneNoteViewData> {
        //Log.d("MainActivity", "reply clicked :$it")
        val intent = Intent(activity, NoteEditorActivity::class.java)
        intent.putExtra(NoteEditorActivity.EXTRA_REPLY_TO_NOTE_ID, it.toShowNote.id)
        activity.startActivity(intent)
    }

    private val reNoteTargetObserver = Observer<PlaneNoteViewData>{
        Log.d("MainActivity", "renote clicked :$it")
        val dialog = RenoteBottomSheetDialog()
        dialog.show(activity.supportFragmentManager, "timelineFragment")

    }
    private val shareTargetObserver = Observer<PlaneNoteViewData> {
        Log.d("MainActivity", "share clicked :$it")
        ShareBottomSheetDialog().show(activity.supportFragmentManager, "MainActivity")
    }
    private val targetUserObserver = Observer<User>{
        Log.d("MainActivity", "user clicked :$it")
        val intent = Intent(activity, UserDetailActivity::class.java)
        intent.putExtra(UserDetailActivity.EXTRA_USER_ID, it.id)
        activity.startActivity(intent)
    }

    private val statusMessageObserver = Observer<String>{
        Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
    }

    private val quoteRenoteTargetObserver = Observer<PlaneNoteViewData>{
        val intent = Intent(activity, NoteEditorActivity::class.java)
        intent.putExtra(NoteEditorActivity.EXTRA_QUOTE_TO_NOTE_ID, it.toShowNote.id)
        activity.startActivity(intent)
    }

    private val reactionTargetObserver = Observer<PlaneNoteViewData>{
        Log.d("MainActivity", "リアクションの対象ノートを選択:${it.toShowNote}")
        ReactionSelectionDialog().show(activity.supportFragmentManager, "MainActivity")
    }

    private val noteTargetObserver = Observer<PlaneNoteViewData>{
        val intent = Intent(activity, NoteDetailActivity::class.java)
        intent.putExtra(NoteDetailActivity.EXTRA_NOTE_ID, it.toShowNote.id)
        activity.startActivity(intent)
    }

    private val fileTargetObserver = Observer<Pair<FileViewData, MediaViewData>>{
        Log.d("ActionNoteHandler", "${it.first.fileProperty}")
        val list = it.second.files.map{fv ->
            fv.fileProperty
        }
        val intent = MediaActivity.newIntent(activity, ArrayList(list), 0)
        activity.startActivity(intent)
        //val intent =
    }

    private val reactionInputObserver = Observer<Unit>{
        val dialog = ReactionInputDialog()
        dialog.show(activity.supportFragmentManager, "")
    }

    fun initViewModelListener(){
        mNotesViewModel.replyTarget.removeObserver(replyTargetObserver)
        mNotesViewModel.replyTarget.observe(activity, replyTargetObserver)

        mNotesViewModel.reNoteTarget.removeObserver(reNoteTargetObserver)
        mNotesViewModel.reNoteTarget.observe(activity, reNoteTargetObserver)

        mNotesViewModel.shareTarget.removeObserver(shareTargetObserver)
        mNotesViewModel.shareTarget.observe(activity, shareTargetObserver)

        mNotesViewModel.targetUser.removeObserver(targetUserObserver)
        mNotesViewModel.targetUser.observe(activity, targetUserObserver)

        mNotesViewModel.statusMessage.removeObserver(statusMessageObserver)
        mNotesViewModel.statusMessage.observe(activity, statusMessageObserver)

        mNotesViewModel.quoteRenoteTarget.removeObserver(quoteRenoteTargetObserver)
        mNotesViewModel.quoteRenoteTarget.observe(activity, quoteRenoteTargetObserver)

        mNotesViewModel.reactionTarget.removeObserver(reactionTargetObserver)
        mNotesViewModel.reactionTarget.observe(activity, reactionTargetObserver)

        mNotesViewModel.targetNote.removeObserver(noteTargetObserver)
        mNotesViewModel.targetNote.observe(activity, noteTargetObserver)

        mNotesViewModel.targetFile.removeObserver(fileTargetObserver)
        mNotesViewModel.targetFile.observe(activity, fileTargetObserver)

        mNotesViewModel.showInputReactionEvent.removeObserver(reactionInputObserver)
        mNotesViewModel.showInputReactionEvent.observe(activity, reactionInputObserver)
    }
}