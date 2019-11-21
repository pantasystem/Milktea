package jp.panta.misskeyandroidclient.view.notes

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentShareBottomSheetBinding
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel

class ShareBottomSheetDialog : BottomSheetDialogFragment(){
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        val view = View.inflate(dialog.context, R.layout.fragment_share_bottom_sheet, null)
        val dataBinding = DataBindingUtil.bind<FragmentShareBottomSheetBinding>(view)
        dialog.setContentView(view)

        if(dataBinding == null){
            Log.e("ShareBottomSheetDialog", "Bindingを取得するのに失敗しました")
            dismiss()
            return
        }
        val viewModel = ViewModelProvider(activity!!).get(NotesViewModel::class.java)
        val note = viewModel.shareTarget.event

        dataBinding.notesViewModel = viewModel
        dataBinding.executePendingBindings()
        dataBinding.lifecycleOwner = this

        dataBinding.showDetail?.setOnClickListener {
            viewModel.setTargetToNote()
            dismiss()
        }

        dataBinding.removeFavorite.setOnClickListener {
            viewModel.deleteFavorite()
            dismiss()
        }

        dataBinding.addFavorite.setOnClickListener{
            viewModel.addFavorite()
            dismiss()
        }

        val clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?

        dataBinding.copyContent.setOnClickListener{
            if(clipboardManager == null || note == null){
                dismiss()
                return@setOnClickListener
            }
            clipboardManager.primaryClip = ClipData.newPlainText("", note.toShowNote.text)
            dismiss()
        }

        dataBinding.copyUrl.setOnClickListener{
            if(clipboardManager == null || note == null){
                dismiss()
                return@setOnClickListener
            }
            clipboardManager.primaryClip = ClipData.newPlainText("", "${note.note.uri}")
            dismiss()

        }

        dataBinding.removeMyNote.setOnClickListener {
            viewModel.removeNoteFromShareTarget()
            dismiss()
        }

    }
}