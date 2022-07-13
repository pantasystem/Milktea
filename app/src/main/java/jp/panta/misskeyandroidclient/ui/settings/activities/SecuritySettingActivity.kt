package jp.panta.misskeyandroidclient.ui.settings.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.setTheme
import net.pantasystem.milktea.common_compose.SwitchTile
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import javax.inject.Inject

@AndroidEntryPoint
class SecuritySettingActivity : AppCompatActivity() {

    @Inject
    lateinit var settingStore: SettingStore

    @Inject
    lateinit var localConfigRepository: LocalConfigRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContent {
            val configState by settingStore.configState.collectAsState()

            var currentConfigState by remember {
                mutableStateOf(configState)
            }


            LaunchedEffect(key1 = currentConfigState) {
                localConfigRepository.save(currentConfigState).onFailure {
                    Log.d("SettingAppearance", "save error", it)
                }
            }
            MdcTheme {

                Scaffold(
                    topBar = {
                        TopAppBar(
                            navigationIcon = {
                                IconButton(onClick = {
                                    finish()
                                }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            },
                            title = {
                                Text(stringResource(id = R.string.security_setting))
                            }
                        )
                    }
                ) { padding ->
                    LazyColumn(modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()) {

                        item {
                            SwitchTile(checked = configState.isCrashlyticsCollectionEnabled.isEnable, onChanged = {
                                currentConfigState = configState.setCrashlyticsCollectionEnabled(it)
                            }) {
                                Text(stringResource(id = R.string.send_a_crash_report))
                            }

                            SwitchTile(checked = configState.isAnalyticsCollectionEnabled.isEnabled, onChanged = {
                                currentConfigState = configState.setAnalyticsCollectionEnabled(it)
                            }) {
                                Text(stringResource(id = R.string.enable_google_analytics))
                            }
                        }
                    }

                }
            }
        }
    }
}