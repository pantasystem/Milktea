package jp.panta.misskeyandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.databinding.ActivitySortedUsesBinding
import jp.panta.misskeyandroidclient.ui.users.SortedUsersFragment
import jp.panta.misskeyandroidclient.ui.users.viewmodel.SortedUsersViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class SortedUsersActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_SORT = "jp.panta.misskeyandroidclient.EXTRA_SORT"
        const val EXTRA_ORIGIN = "jp.panta.misskeyandroidclient.EXTRA_ORIGIN"
        const val EXTRA_STATE = "jp.panta.misskeyandroidclient.EXTRA_STATE"

        const val EXTRA_SORTED_USERS_TYPE = "jp.panta.misskeyandroidclient.EXTRA_STATE_TYPE"

        const val EXTRA_TITLE = "jp.panta.misskeyandroidclient.EXTRA_TITLE"

    }

    val binding: ActivitySortedUsesBinding by dataBinding()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_sorted_uses)
        setSupportActionBar(binding.sortedUsersToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val type = intent.getSerializableExtra(EXTRA_SORTED_USERS_TYPE) as? SortedUsersViewModel.Type?

        intent.getStringExtra(EXTRA_TITLE)?.let{
            supportActionBar?.title = it
        }


        if(savedInstanceState == null){
            val fragment = if(type == null){
                val sort = intent.getStringExtra(EXTRA_SORT)
                val origin = intent.getSerializableExtra(EXTRA_ORIGIN) as? RequestUser.Origin?
                val state = intent.getSerializableExtra(EXTRA_STATE) as? RequestUser.State?
                SortedUsersFragment.newInstance(origin, sort, state)
            }else{
                SortedUsersFragment.newInstance(type)
            }

            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.sortedUsersFragmentBase, fragment)
            ft.commit()

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
