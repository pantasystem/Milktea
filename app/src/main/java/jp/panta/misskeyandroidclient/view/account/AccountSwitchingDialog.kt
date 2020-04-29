package jp.panta.misskeyandroidclient.view.account

import android.app.Dialog
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.account.AccountViewData
import jp.panta.misskeyandroidclient.viewmodel.account.AccountViewModel
import kotlinx.android.synthetic.main.dialog_switch_account.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountSwitchingDialog : BottomSheetDialogFragment(){

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        val view = View.inflate(context, R.layout.dialog_switch_account,null)
        dialog.setContentView(view)


        val miApplication = context?.applicationContext as MiApplication
        //miApplication.connectionInstanceDao?.findAll()
        val accounts = miApplication.accounts.value
        if(accounts == null){
            Log.w("AccountSwitchDialog", "アカウント達の取得に失敗しました")
            Toast.makeText(this.context, "アカウントの取得に失敗しました", Toast.LENGTH_LONG).show()
            return
        }
        val activity = activity
        if(activity == null){
            dismiss()
            return
        }

        view.add_account.setOnClickListener {
            //FIXME 新しい認証アクティビティを実装する
            dismiss()
        }

        view.accounts_view.layoutManager = LinearLayoutManager(context)


        val viewDataList = accounts.map{ ar ->
            loadUser(miApplication, AccountViewData(MutableLiveData(), ar))
        }


        val accountViewModel = ViewModelProvider(activity)[AccountViewModel::class.java]

        val adapter = AccountListAdapter(diff, accountViewModel, activity)
        adapter.submitList(viewDataList)
        view.accounts_view.adapter = adapter
        accountViewModel.switchTargetConnectionInstance.observe(activity, Observer {
            dismiss()
        })


    }

    private fun loadUser(miCore: MiCore, accountViewData: AccountViewData): AccountViewData{

        miCore.getMisskeyAPI(accountViewData.accountRelation)?.i(
            I(
                accountViewData.accountRelation.getCurrentConnectionInformation()?.getI(miCore.getEncryption())!!
            )
        )?.enqueue(object : Callback<User>{
            override fun onResponse(call: Call<User>, response: Response<User>) {
                val user = response.body()
                accountViewData.user.postValue(user)
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.d(this.javaClass.name, "user load error", t)
            }
        })
        return accountViewData
    }

    private val diff = object : DiffUtil.ItemCallback<AccountViewData>(){
        override fun areContentsTheSame(
            oldItem: AccountViewData,
            newItem: AccountViewData
        ): Boolean {
            return oldItem.accountRelation.account.id == newItem.accountRelation.account.id
        }

        override fun areItemsTheSame(oldItem: AccountViewData, newItem: AccountViewData): Boolean {
            return oldItem.user == newItem.user
                    && oldItem.accountRelation.getCurrentConnectionInformation() == newItem.accountRelation.getCurrentConnectionInformation()
        }
    }
}