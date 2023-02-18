package net.pantasystem.milktea.setting.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.setting.R
import net.pantasystem.milktea.setting.SettingSection
import net.pantasystem.milktea.setting.compose.SettingSwitchTile
import javax.inject.Inject

@AndroidEntryPoint
class SecuritySettingActivity : AppCompatActivity() {

    @Inject
    lateinit var settingStore: SettingStore

    @Inject
    lateinit var localConfigRepository: LocalConfigRepository

    @Inject
    lateinit var applyTheme: ApplyTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
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
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                    ) {
                        SettingSection(title = "Tracking") {
                            SettingSwitchTile(checked = configState.isCrashlyticsCollectionEnabled.isEnable, onChanged = {
                                currentConfigState = configState.setCrashlyticsCollectionEnabled(it)
                            }) {
                                Text(stringResource(id = R.string.send_a_crash_report))
                            }

                            SettingSwitchTile(checked = configState.isAnalyticsCollectionEnabled.isEnabled, onChanged = {
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