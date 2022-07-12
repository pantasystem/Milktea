package jp.panta.misskeyandroidclient.ui.settings.activities

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ActivitySettingsBinding
import jp.panta.misskeyandroidclient.setTheme
import jp.panta.misskeyandroidclient.ui.settings.compose.SettingTitleTile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common_compose.SwitchTile
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.setting.RememberVisibility
import javax.inject.Inject



@AndroidEntryPoint
class SettingMovementActivity : AppCompatActivity() {

    lateinit var mBinding: ActivitySettingsBinding

    @Inject
    lateinit var accountStore: AccountStore

    @Inject
    lateinit var settingStore: SettingStore

    @Inject
    lateinit var localConfigRepository: LocalConfigRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
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
                                Text(stringResource(id = R.string.app_name))
                            }
                        )
                    },
                ) { padding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {

                        item {
                            SettingTitleTile(text = stringResource(id = R.string.timeline))

                            SwitchTile(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                checked = currentConfigState.isIncludeLocalRenotes,
                                onChanged = {
                                    currentConfigState =
                                        currentConfigState.copy(isIncludeLocalRenotes = it)
                                }) {
                                Text(text = stringResource(id = R.string.include_local_renotes))
                            }

                            SwitchTile(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                checked = currentConfigState.isIncludeRenotedMyNotes,
                                onChanged = {
                                    currentConfigState =
                                        currentConfigState.copy(isIncludeRenotedMyNotes = it)
                                }) {
                                Text(text = stringResource(id = R.string.include_renoted_my_notes))
                            }

                            SwitchTile(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                checked = currentConfigState.isIncludeMyRenotes,
                                onChanged = {
                                    currentConfigState =
                                        currentConfigState.copy(isIncludeMyRenotes = it)
                                }) {
                                Text(text = stringResource(id = R.string.include_my_renotes))
                            }
                            SettingTitleTile(text = stringResource(id = R.string.auto_note_folding))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(text = stringResource(id = R.string.height_limit))
                                TextField(
                                    placeholder = { Text(text = stringResource(id = R.string.height_limit)) },
                                    value = currentConfigState.noteExpandedHeightSize.toString(),
                                    keyboardOptions = KeyboardOptions
                                        .Default.copy(keyboardType = KeyboardType.Number),
                                    onValueChange = {
                                        currentConfigState = currentConfigState.copy(
                                            noteExpandedHeightSize = it.toIntOrNull()
                                                ?: DefaultConfig.config.noteExpandedHeightSize
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        if (currentAccount != null) {
                            item {
                                SettingTitleTile(text = stringResource(id = R.string.auto_note_folding))
                                SwitchTile(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
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
