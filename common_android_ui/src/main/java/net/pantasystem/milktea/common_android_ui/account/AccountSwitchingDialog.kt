package net.pantasystem.milktea.common_android_ui.account

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.ui.account.viewmodel.AccountViewData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.common_android_ui.R
import net.pantasystem.milktea.common_navigation.AuthorizationNavigation
import net.pantasystem.milktea.common_viewmodel.viewmodel.AccountViewModel
import javax.inject.Inject

@AndroidEntryPoint
class AccountSwitchingDialog : BottomSheetDialogFragment() {

    @Inject
    lateinit var authorizationNavigation: AuthorizationNavigation

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(context, R.layout.dialog_switch_account, null)
        dialog.setContentView(view)


        val activity = activity
        if (activity == null) {
            dismiss()
            return dialog
        }

        view.findViewById<Button>(R.id.add_account).setOnClickListener {
            startActivity(authorizationNavigation.newIntent(Unit))
            dismiss()
        }
        val accountsView = view.findViewById<RecyclerView>(R.id.accounts_view)

        accountsView.layoutManager = LinearLayoutManager(view.context)

        val accountViewModel = ViewModelProvider(activity)[AccountViewModel::class.java]

        val adapter = AccountListAdapter(diff, accountViewModel, activity)
        accountViewModel.accounts.observe(this) {
            adapter.submitList(it?: emptyList())
        }
        accountsView.adapter = adapter
        accountViewModel.switchTargetConnectionInstanceEvent.observe(activity) {
            dismiss()
        }
        return dialog
    }


    @FlowPreview
    @ExperimentalCoroutinesApi
    private val diff = object : DiffUtil.ItemCallback<AccountViewData>() {
        override fun areContentsTheSame(
            oldItem: AccountViewData,
            newItem: AccountViewData
        ): Boolean {
            return oldItem.account.accountId == newItem.account.accountId
        }

        override fun areItemsTheSame(oldItem: AccountViewData, newItem: AccountViewData): Boolean {
            return oldItem.user == newItem.user
                    && oldItem.account == newItem.account
        }
    }
}