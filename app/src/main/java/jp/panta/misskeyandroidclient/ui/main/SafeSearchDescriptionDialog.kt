package jp.panta.misskeyandroidclient.ui.main

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.pantasystem.milktea.common_resource.R
import net.pantasystem.milktea.setting.activities.SettingMovementActivity

class SafeSearchDescriptionDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.safe_search_description_dialog_title)
            .setMessage(R.string.safe_search_description_dialog_message)
            .setPositiveButton(R.string.safe_search_description_dialog_positive_button) { _, _ ->
                val intent = Intent(requireContext(), SettingMovementActivity::class.java)
                intent.putExtra(SettingMovementActivity.EXTRA_HIGHLIGHT_SAFE_SEARCH, true)
                startActivity(intent)
                dismiss()
            }
            .setNegativeButton(R.string.safe_search_description_dialog_negative_button) { _, _ ->
                // 何もしない
                dismiss()
            }
            .setCancelable(false)

            .create()
    }
}