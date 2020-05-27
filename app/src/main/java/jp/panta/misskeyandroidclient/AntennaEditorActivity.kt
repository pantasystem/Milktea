package jp.panta.misskeyandroidclient

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.v12.antenna.Antenna
import jp.panta.misskeyandroidclient.view.antenna.AntennaEditorFragment
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.antenna.AntennaEditorViewModel
import kotlinx.android.synthetic.main.activity_antenna_editor.*

class AntennaEditorActivity : AppCompatActivity() {
    companion object{
        const val EXTRA_ANTENNA = "jp.panta.misskeyandroidclient.AntennaEditorActivity.EXTRA_ANTENNA"
        private const val REQUEST_SEARCH_AND_SELECT_USER = 110
    }

    private var mViewModel: AntennaEditorViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_antenna_editor)
        setSupportActionBar(antennaEditorToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val antenna = intent.getSerializableExtra(EXTRA_ANTENNA) as? Antenna?

        if(savedInstanceState == null){
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.antennaEditorBase, AntennaEditorFragment.newInstance(antenna))
            ft.commit()
        }

        val miCore = applicationContext as MiCore
        miCore.currentAccount.observe(this, Observer { ar ->
            val viewModel = ViewModelProvider(this, AntennaEditorViewModel.Factory(ar, miCore, antenna))[AntennaEditorViewModel::class.java]
            this.mViewModel = viewModel
            viewModel.selectUserEvent.observe(this, Observer {
                showSearchAndSelectUserActivity(it)
            })
            viewModel.name.observe(this, Observer {
                supportActionBar?.title = it
            })
            viewModel.antennaRemovedEvent.observe(this, Observer {
                Toast.makeText(this, getString(R.string.remove), Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            })

            viewModel.antennaAddedStateEvent.observe(this, Observer {
                if(it){
                    Toast.makeText(this, getString(R.string.success), Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this, getString(R.string.failure), Toast.LENGTH_LONG).show()
                }
            })
        })
    }

    private fun showSearchAndSelectUserActivity(userIds: List<String>){
        val intent = Intent(this, SearchAndSelectUserActivity::class.java)
        intent.putExtra(SearchAndSelectUserActivity.EXTRA_SELECTED_USER_IDS, userIds.toTypedArray())
        startActivityForResult(intent, REQUEST_SEARCH_AND_SELECT_USER)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home ->{
                setResult(RESULT_OK)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            REQUEST_SEARCH_AND_SELECT_USER ->{
                if(resultCode == Activity.RESULT_OK && data != null){
                    data.getStringArrayExtra(SearchAndSelectUserActivity.EXTRA_SELECTED_USER_IDS)?.toList()?.let{
                        mViewModel?.setUserIds(it)

                    }
                }
            }
        }
    }
}
