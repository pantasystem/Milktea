package net.pantasystem.milktea.setting.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.setting.R
import net.pantasystem.milktea.setting.viewmodel.muteword.ClientWordFilterSettingViewModel
import javax.inject.Inject

@AndroidEntryPoint
class ClientWordFilterSettingActivity : AppCompatActivity() {


    @Inject
    lateinit var applyTheme: ApplyTheme

    private val viewModel by viewModels<ClientWordFilterSettingViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applyTheme()
        setContent {
            MdcTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(stringResource(id = R.string.client_word_mute))
                            },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, null)
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = { viewModel.save() }) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                    }
                ) {
                    LazyColumn(Modifier.padding(it)) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                value = viewModel.muteWordsFieldState,
                                onValueChange = {
                                    viewModel.updateText(it)
                                }
                            )
                            Text(
                                stringResource(R.string.client_word_mute_format_description),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}