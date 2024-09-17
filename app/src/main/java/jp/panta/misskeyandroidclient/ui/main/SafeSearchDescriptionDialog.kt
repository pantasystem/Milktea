package jp.panta.misskeyandroidclient.ui.main

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import jp.panta.misskeyandroidclient.ui.main.viewmodel.MainViewModel
import net.pantasystem.milktea.common_resource.R
import net.pantasystem.milktea.setting.activities.SettingMovementActivity

class SafeSearchDescriptionDialog : DialogFragment() {
    companion object {
        const val TAG = "SafeSearchDescriptionDialog"
    }

    private val preferences by lazy {
        requireContext().getSharedPreferences("safe_search_description_dialog", Context.MODE_PRIVATE)
    }

    private val mainViewModel by activityViewModels<MainViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.safe_search_description_dialog_title)
            .setMessage(R.string.safe_search_description_dialog_message)
            .setPositiveButton(R.string.safe_search_description_dialog_positive_button) { _, _ ->
                mainViewModel.onGoToSettingSafeSearchButtonClicked()
                val intent = Intent(requireContext(), SettingMovementActivity::class.java)
                intent.putExtra(SettingMovementActivity.EXTRA_HIGHLIGHT_SAFE_SEARCH, true)
                startActivity(intent)
                dismiss()
            }
            .setNegativeButton(R.string.safe_search_description_dialog_negative_button) { _, _ ->
                if (preferences.getInt("counter", 0) >= 1) {
                    mainViewModel.onDoNotShowSafeSearchDescription()
                }
                preferences.edit().putInt("counter", preferences.getInt("counter", 0) + 1).apply()
                // 何もしない
                dismiss()
            }
            .setCancelable(false)

            .create()
    }
}