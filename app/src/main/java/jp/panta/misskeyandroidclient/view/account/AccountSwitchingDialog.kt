package jp.panta.misskeyandroidclient.view.account

import android.app.Dialog
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.panta.misskeyandroidclient.AuthActivity
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.viewmodel.account.AccountViewData
import jp.panta.misskeyandroidclient.viewmodel.account.AccountViewModel
import kotlinx.android.synthetic.main.dialog_switch_account.view.*

class AccountSwitchingDialog : BottomSheetDialogFragment(){

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        val view = View.inflate(context, R.layout.dialog_switch_account,null)
        dialog.setContentView(view)

        val miApplication = context?.applicationContext as MiApplication
        //miApplication.connectionInstanceDao?.findAll()
        val connectionInstances = miApplication.connectionInstancesLiveData.value
        if(connectionInstances == null){
            Log.w("AccountSwitchDialog", "アカウント達の取得に失敗しました")
            Toast.makeText(this.context, "アカウントの取得に失敗しました", Toast.LENGTH_LONG).show()
            dismiss()
            return
        }
        val activity = activity
        if(activity == null){
            dismiss()
            return
        }

        view.add_account.setOnClickListener {
            startActivity(Intent(activity, AuthActivity::class.java))
            dismiss()
        }

        view.accounts_view.layoutManager = LinearLayoutManager(context)

       val accounts = miApplication.accountsLiveData.value

        val a = connectionInstances.map{ci ->
            val account = accounts?.firstOrNull{user ->
                user.id == ci.userId
            }
            if(account != null){
                AccountViewData(account, ci)
            }else{
                null
            }
        }.filterNotNull()

        val accountViewModel = ViewModelProvider(activity)[AccountViewModel::class.java]

        val adapter = AccountListAdapter(diff, accountViewModel)
        adapter.submitList(a)
        view.accounts_view.adapter = adapter
        accountViewModel.switchTargetConnectionInstance.observe(activity, Observer {
            dismiss()
        })


    }

    private val diff = object : DiffUtil.ItemCallback<AccountViewData>(){
        override fun areContentsTheSame(
            oldItem: AccountViewData,
            newItem: AccountViewData
        ): Boolean {
            return oldItem.connectionInstance.userId == newItem.connectionInstance.userId
        }

        override fun areItemsTheSame(oldItem: AccountViewData, newItem: AccountViewData): Boolean {
            return oldItem.user == newItem.user && oldItem.connectionInstance == newItem.connectionInstance
        }
    }
}