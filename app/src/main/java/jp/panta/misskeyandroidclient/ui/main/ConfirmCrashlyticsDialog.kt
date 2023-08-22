package jp.panta.misskeyandroidclient.ui.main

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.ui.main.viewmodel.MainViewModel

@AndroidEntryPoint
class ConfirmCrashlyticsDialog : AppCompatDialogFragment() {

    companion object {
        const val FRAGMENT_TAG = "ConfirmCrashlyticsDialog"
    }

    private val mainViewModel by activityViewModels<MainViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.send_a_crash_report)
            .setPositiveButton(R.string.agree) { _, _ ->
                mainViewModel.setCrashlyticsCollectionEnabled(true)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                mainViewModel.setCrashlyticsCollectionEnabled(false)
            }
            .setMessage(R.string.crash_reports_are_useful_for_development)
            .show()
    }
}