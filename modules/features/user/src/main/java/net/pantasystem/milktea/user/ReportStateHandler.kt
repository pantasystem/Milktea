package net.pantasystem.milktea.user

import android.view.View
import com.google.android.material.snackbar.Snackbar
import net.pantasystem.milktea.model.user.report.ReportState

class ReportStateHandler {

    operator fun invoke(view: View, state: ReportState) {
        if (state is ReportState.Sending.Success) {
            Snackbar.make(
                view,
                R.string.successful_report,
                Snackbar.LENGTH_SHORT
            ).show()
        } else if (state is ReportState.Sending.Failed) {
            Snackbar.make(
                view,
                R.string.report_failed,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
}