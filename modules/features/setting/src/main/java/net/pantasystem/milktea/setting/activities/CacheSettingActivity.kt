package net.pantasystem.milktea.setting.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ui.ApplyTheme
import javax.inject.Inject

@AndroidEntryPoint
class CacheSettingActivity : AppCompatActivity() {

    @Inject
    internal lateinit var applyTheme: ApplyTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        setContent {

        }
    }
}