package jp.panta.misskeyandroidclient.ui.main

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.ui.main.viewmodel.MainViewModel

@AndroidEntryPoint
class ConfirmGoogleAnalyticsDialog : DialogFragment() {
    companion object {
        const val FRAGMENT_TAG = "ConfirmGoogleAnalyticsDialog"
    }

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.consent_to_collect_behavior_history)
            .setMessage(R.string.consent_to_collect_behavior_history_message)
            .setPositiveButton(R.string.agree) { _, _ ->
                mainViewModel.setAnalyticsCollectionEnabled(true)
            }
            .setNegativeButton(R.string.disagree) { _, _ ->
                mainViewModel.setAnalyticsCollectionEnabled(false)
            }.show()
    }
}