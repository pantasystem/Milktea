package jp.panta.misskeyandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import jp.panta.misskeyandroidclient.view.tags.SortedHashTagFragment
import jp.panta.misskeyandroidclient.viewmodel.tags.SortedHashTagListViewModel
import kotlinx.android.synthetic.main.activity_sorted_hash_tags.*

class SortedHashTagsActivity : AppCompatActivity() {

    companion object{

        const val EXTRA_HASH_TAG_CONDITION = "jp.panta.misskeyandroidclient.view.tags.EXTRA_HASH_TAG_CONDITION"
        const val EXTRA_TITLE = "jp.panta.misskeyandroidclient.view.tags.EXTRA_TITLE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_sorted_hash_tags)

        setSupportActionBar(sortedHashTagToolbar)
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
