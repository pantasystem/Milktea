package jp.panta.misskeyandroidclient

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val keys = KeyStore.BooleanKey.values().toList()
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        keys.forEach{

        }


    }
}
