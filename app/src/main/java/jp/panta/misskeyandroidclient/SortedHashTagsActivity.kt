package jp.panta.misskeyandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.databinding.ActivitySortedHashTagsBinding
import jp.panta.misskeyandroidclient.view.tags.SortedHashTagFragment
import jp.panta.misskeyandroidclient.viewmodel.tags.SortedHashTagListViewModel

class SortedHashTagsActivity : AppCompatActivity() {

    companion object{

        const val EXTRA_HASH_TAG_CONDITION = "jp.panta.misskeyandroidclient.view.tags.EXTRA_HASH_TAG_CONDITION"
        const val EXTRA_TITLE = "jp.panta.misskeyandroidclient.view.tags.EXTRA_TITLE"
    }

    val binding: ActivitySortedHashTagsBinding by dataBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_sorted_hash_tags)

        setSupportActionBar(binding.sortedHashTagToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val conditions = intent.getSerializableExtra(EXTRA_HASH_TAG_CONDITION) as SortedHashTagListViewModel.Conditions

        intent.getStringExtra(EXTRA_TITLE)?.let{
            supportActionBar?.title = it
        }

        if(savedInstanceState == null){
            val ft = supportFragmentManager.beginTransaction()
            val fragment = SortedHashTagFragment.newInstance(conditions)
            ft.replace(R.id.hashTagListBaseView, fragment)
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
