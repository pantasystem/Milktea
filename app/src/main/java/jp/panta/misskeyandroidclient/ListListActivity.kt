package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class ListListActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_USER_LIST_NAME = "jp.panta.misskeyandroidclient.EXTRA_USER_LIST_NAME"
        const val EXTRA_USER_LIST_ID = "jp.panta.misskeyandroidclient.EXTRA_USER_LIST_ID"
        const val EXTRA_CREATED_AT = "jp.panta.misskeyandroidclient.EXTRA_CREATED_AT"
        const val EXTRA_USER_ID_LIST = "jp.panta.misskeyandroidclient.EXTRA_USER_ID_LIST"
        const val ACTION_USER_LIST_CREATED = "jp.panta.misskeyandroidclient.ACTION_USER_LIST_CREATED"
        const val ACTION_USER_LIST_UPDATED = "jp.panta.misskeyandroidclient.ACTION_USER_LIST_UPDATED"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_list)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

    }
}
