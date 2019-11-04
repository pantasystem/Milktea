package jp.panta.misskeyandroidclient.view.account

import android.app.Dialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.panta.misskeyandroidclient.MiApplication

class AccountSwitchingDialog : BottomSheetDialogFragment(){

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        val miApplication = context?.applicationContext as MiApplication
        miApplication.connectionInstanceDao?.findAll()
    }
}