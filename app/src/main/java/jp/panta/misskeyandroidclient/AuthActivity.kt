package jp.panta.misskeyandroidclient

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.databinding.ActivityAuthBinding
import jp.panta.misskeyandroidclient.model.auth.AuthStorage
import jp.panta.misskeyandroidclient.model.auth.Instance
import jp.panta.misskeyandroidclient.view.auth.InstanceAdapter
import jp.panta.misskeyandroidclient.viewmodel.auth.AuthViewModel
import jp.panta.misskeyandroidclient.viewmodel.auth.AuthViewModelFactory
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_auth)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        this.title = "インスタンスを選択"

        val binding: ActivityAuthBinding = DataBindingUtil.setContentView(this, R.layout.activity_auth)
        setSupportActionBar(binding.authToolbar)
        val viewModel = ViewModelProvider(viewModelStore, AuthViewModelFactory()).get(AuthViewModel::class.java)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel
        binding.authListView.layoutManager = LinearLayoutManager(this)

        val adapter = InstanceAdapter(object : DiffUtil.ItemCallback<Instance>(){
            override fun areContentsTheSame(oldItem: Instance, newItem: Instance): Boolean {
                return oldItem.appSecret == newItem.appSecret
            }

            override fun areItemsTheSame(oldItem: Instance, newItem: Instance): Boolean {
                return oldItem == newItem
            }
        }, viewModel)
        binding.authListView.adapter = adapter
        adapter.submitList(viewModel.instances)

        val sessionStorage = AuthStorage(PreferenceManager.getDefaultSharedPreferences(this))
        viewModel.errorMessage.observe(this, Observer {
            runOnUiThread {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.sessionLiveData.observe(this, Observer {
            val domain = viewModel.currentInstance.value?.domain
            if(domain != null){
                sessionStorage.setInstanceDomain(domain)
                sessionStorage.setSession(it)
                val uri = Uri.parse(it.url)
                runOnUiThread {
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                    finish()
                }
            }

        })





    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_auth_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home ->{
                finish()
                return true
            }
            R.id.menu_sign_in ->{
                finish()
                startActivity(Intent(this, SignInActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
