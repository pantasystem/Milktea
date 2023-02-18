package net.pantasystem.milktea.setting.activities

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.setting.RememberVisibility
import net.pantasystem.milktea.setting.R
import net.pantasystem.milktea.setting.SettingSection
import net.pantasystem.milktea.setting.compose.SettingSwitchTile
import javax.inject.Inject


@AndroidEntryPoint
class SettingMovementActivity : AppCompatActivity() {


    @Inject
    lateinit var accountStore: AccountStore

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
            val currentAccount by accountStore.observeCurrentAccount.collectAsState(initial = null)

            var currentConfigState by remember {
                mutableStateOf(configState)
            }

            val rv: RememberVisibility by accountStore.observeCurrentRememberVisibility()
                .collectAsState(initial = RememberVisibility.None)

            val scope = rememberCoroutineScope()


            LaunchedEffect(key1 = currentConfigState) {
                Log.d("SettingMovementActivity", "save:$currentConfigState")
                localConfigRepository.save(
                    currentConfigState
                ).onFailure {
                    Log.d("SettingMovementActivity", "error", it)
                }
            }


            MdcTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                                }
                            },
                            title = {
                                Text(stringResource(id = R.string.movement))
                            }
                        )
                    },
                ) { padding ->

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(padding)
                            .verticalScroll(
                                rememberScrollState()
                            )
                    ) {

                        SettingSection(title = stringResource(id = R.string.timeline)) {
                            SettingSwitchTile(
                                checked = currentConfigState.isIncludeLocalRenotes,
                                onChanged = {
                                    currentConfigState =
                                        currentConfigState.copy(isIncludeLocalRenotes = it)
                                }) {
                                Text(text = stringResource(id = R.string.include_local_renotes))
                            }

                            SettingSwitchTile(
                                checked = currentConfigState.isIncludeRenotedMyNotes,
                                onChanged = {
                                    currentConfigState =
                                        currentConfigState.copy(isIncludeRenotedMyNotes = it)
                                }) {
                                Text(text = stringResource(id = R.string.include_renoted_my_notes))
                            }

                            SettingSwitchTile(
                                checked = currentConfigState.isIncludeMyRenotes,
                                onChanged = {
                                    currentConfigState =
                                        currentConfigState.copy(isIncludeMyRenotes = it)
                                }) {
                                Text(text = stringResource(id = R.string.include_my_renotes))
                            }
                        }


                        SettingSection(title = stringResource(id = R.string.auto_note_folding)) {
                            Text(text = stringResource(id = R.string.height_limit), modifier = Modifier.padding(horizontal = 14.dp))
                            Slider(
                                value = currentConfigState.noteExpandedHeightSize.let {
                                    val v =
                                        currentConfigState.noteExpandedHeightSize.toFloat() / 1000f
                                    if (it in 0..1000) {
                                        v
                                    } else {
                                        1f
                                    }
                                },
                                onValueChange = {
                                    val v = (it * 1000f).toInt()
                                    currentConfigState = currentConfigState.copy(
                                        noteExpandedHeightSize = if (v > 50) {
                                            v
                                        } else 50
                                    )

                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        if (currentAccount != null) {
                            SettingSection(title = stringResource(id = R.string.learn_note_visibility)) {
                                SettingSwitchTile(
                                    checked = rv is RememberVisibility.Remember,
                                    onChanged = {
                                        val config = if (it) {
                                            DefaultConfig.getRememberVisibilityConfig(currentAccount!!.accountId)
                                        } else {
                                            RememberVisibility.None
                                        }
                                        scope.launch(Dispatchers.IO) {
                                            localConfigRepository.save(config)
                                        }

                                    }) {
                                    Text(
                                        text = stringResource(id = R.string.learn_note_visibility)
                                    )
                                }
                            }

                        }


                        SettingSection(title = stringResource(id = R.string.notification_sound)) {
                            SettingSwitchTile(
                                checked = currentConfigState.isEnableNotificationSound,
                                onChanged = {
                                    currentConfigState =
                                        currentConfigState.copy(isEnableNotificationSound = it)
                                }) {
                                Text(stringResource(id = R.string.inapp_notification_sound))
                            }

                        }

                        SettingSection(title = stringResource(id = R.string.streaming)) {
                            SettingSwitchTile(
                                checked = currentConfigState.isEnableStreamingAPIAndNoteCapture,
                                onChanged = {
                                    currentConfigState = currentConfigState.copy(
                                        isEnableStreamingAPIAndNoteCapture = it
                                    )
                                }
                            ) {
                                Text(stringResource(id = R.string.enable_automic_updates))
                            }
                            if (currentConfigState.isEnableStreamingAPIAndNoteCapture) {
                                SettingSwitchTile(
                                    checked = currentConfigState.isStopStreamingApiWhenBackground,
                                    onChanged = {
                                        currentConfigState =
                                            currentConfigState.copy(isStopStreamingApiWhenBackground = it)
                                    }) {
                                    Text(stringResource(id = R.string.is_stop_timeline_streaming_when_background))
                                }
                                SettingSwitchTile(
                                    checked = currentConfigState.isStopNoteCaptureWhenBackground,
                                    onChanged = {
                                        currentConfigState =
                                            currentConfigState.copy(isStopNoteCaptureWhenBackground = it)
                                    }) {
                                    Text(stringResource(id = R.string.is_stop_note_capture_when_background))
                                }
                            }
                        }
                    }

                }
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun AccountStore.observeCurrentRememberVisibility(): Flow<RememberVisibility> {
        return this.observeCurrentAccount.filterNotNull()
            .flatMapLatest {
                localConfigRepository.observeRememberVisibility(it.accountId)
            }
    }
}
