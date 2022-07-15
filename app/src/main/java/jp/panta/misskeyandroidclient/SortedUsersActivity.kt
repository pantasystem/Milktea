package jp.panta.misskeyandroidclient

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.databinding.ActivitySortedUsesBinding
import jp.panta.misskeyandroidclient.ui.users.SortedUsersFragment
import net.pantasystem.milktea.model.user.query.FindUsersQuery
import net.pantasystem.milktea.model.user.query.from

@AndroidEntryPoint
class SortedUsersActivity : AppCompatActivity() {

    companion object{
        private const val EXTRA_SORT = "jp.panta.misskeyandroidclient.EXTRA_SORT"
        private const val EXTRA_ORIGIN = "jp.panta.misskeyandroidclient.EXTRA_ORIGIN"
        private const val EXTRA_STATE = "jp.panta.misskeyandroidclient.EXTRA_STATE"

//        const val EXTRA_SORTED_USERS_TYPE = "jp.panta.misskeyandroidclient.EXTRA_STATE_TYPE"

        const val EXTRA_TITLE = "jp.panta.misskeyandroidclient.EXTRA_TITLE"
        fun newIntent(activity: Activity, findUsersQuery: FindUsersQuery, title: String): Intent {
            val intent = Intent(activity, SortedUsersActivity::class.java)
            intent.putExtra(EXTRA_SORT, findUsersQuery.sort?.str())
            intent.putExtra(EXTRA_ORIGIN, findUsersQuery.origin?.origin)
            intent.putExtra(EXTRA_STATE, findUsersQuery.state?.state)
            intent.putExtra(EXTRA_TITLE, title)
            return intent
        }

    }

    val binding: ActivitySortedUsesBinding by dataBinding()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_sorted_uses)
        setSupportActionBar(binding.sortedUsersToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        val type = intent.getSerializableExtra(EXTRA_SORTED_USERS_TYPE) as? SortedUsersViewModel.Type?

        intent.getStringExtra(EXTRA_TITLE)?.let{
            supportActionBar?.title = it
        }


        if(savedInstanceState == null){
            val query = FindUsersQuery(
                origin = FindUsersQuery.Origin.from(intent.getStringExtra(SortedUsersFragment.EXTRA_ORIGIN) ?: ""),
                state = FindUsersQuery.State.from(intent.getStringExtra(SortedUsersFragment.EXTRA_STATE) ?: ""),
                sort = FindUsersQuery.OrderBy.from(intent.getStringExtra(SortedUsersFragment.EXTRA_SORT) ?: "")
            )
            val fragment = SortedUsersFragment.newInstance(query)


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
