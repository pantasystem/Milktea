package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.list.ListListViewModel
import java.util.*

class ListListActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_USER_LIST_NAME = "jp.panta.misskeyandroidclient.EXTRA_USER_LIST_NAME"
        const val EXTRA_USER_LIST_ID = "jp.panta.misskeyandroidclient.EXTRA_USER_LIST_ID"
        const val EXTRA_CREATED_AT = "jp.panta.misskeyandroidclient.EXTRA_CREATED_AT"
        const val EXTRA_USER_ID_ARRAY = "jp.panta.misskeyandroidclient.EXTRA_USER_ID_ARRAY"
        const val ACTION_USER_LIST_CREATED = "jp.panta.misskeyandroidclient.ACTION_USER_LIST_CREATED"
        const val ACTION_USER_LIST_UPDATED = "jp.panta.misskeyandroidclient.ACTION_USER_LIST_UPDATED"

        private const val USER_LIST_ACTIVITY_RESULT_CODE = 12
    }

    private var mListListViewModel: ListListViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_list_list)

        val miCore = application as MiCore
        miCore.currentAccount.observe(this, Observer{
            mListListViewModel = ViewModelProvider(this, ListListViewModel.Factory(it, miCore))[ListListViewModel::class.java]
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == USER_LIST_ACTIVITY_RESULT_CODE){
            if(resultCode != RESULT_OK || data == null){
                return
            }

            val listId = intent.getStringExtra(EXTRA_USER_LIST_ID)
            val name = intent.getStringExtra(EXTRA_USER_LIST_NAME)
            when(intent.action){
                ACTION_USER_LIST_CREATED ->{
                    val createdAt = intent.getSerializableExtra(EXTRA_CREATED_AT) as Date
                    val userIdList = intent.getStringArrayExtra(EXTRA_USER_ID_ARRAY).toList()
                    // created処理
                    mListListViewModel?.onUserListCreated(listId, createdAt, name, userIdList)
                }
                ACTION_USER_LIST_UPDATED ->{
                    // updated処理
                    mListListViewModel?.onUserListUpdated(listId, name)
                }
            }
        }

    }
}
