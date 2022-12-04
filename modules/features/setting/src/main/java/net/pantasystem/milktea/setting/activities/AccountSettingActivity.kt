package net.pantasystem.milktea.setting.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_android_ui.account.AccountSwitchingDialogLayout
import net.pantasystem.milktea.common_android_ui.account.AccountTile
import net.pantasystem.milktea.common_android_ui.account.viewmodel.AccountViewModel
import net.pantasystem.milktea.common_navigation.*
import net.pantasystem.milktea.setting.R
import javax.inject.Inject

@AndroidEntryPoint
class AccountSettingActivity : AppCompatActivity() {

    @Inject
    lateinit var authorizationNavigation: AuthorizationNavigation

    @Inject
    lateinit var userDetailNavigation: UserDetailNavigation

    val viewModel: AccountViewModel by viewModels()

    @Inject
    lateinit var applyTheme: ApplyTheme
    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        setContent {
            MdcTheme {
                val uiState by viewModel.uiState.collectAsState()
                val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
                val scope = rememberCoroutineScope()
                ModalBottomSheetLayout(sheetState = sheetState, sheetContent = {
                    AccountSwitchingDialogLayout(
                        uiState = uiState,
                        onSettingButtonClicked = {
                            scope.launch {
                                sheetState.hide()
                            }
                        },
                        onAvatarIconClicked = {
                            UserDetailNavigationArgs.UserName(it.user?.userName ?: it.account.userName)
                        },
                        onAccountClicked = {
                            viewModel.setSwitchTargetConnectionInstance(it.account)
                        },
                        onAddAccountButtonClicked = {
                            startActivity(authorizationNavigation.newIntent(AuthorizationArgs.New))
                        }
                    )
                }) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                navigationIcon = {
                                    IconButton(onClick = { finish() }) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                                    }
                                },
                                title = {
                                    Text(stringResource(id = R.string.account))
                                },
                                elevation = 0.dp,
                                backgroundColor = MaterialTheme.colors.surface,
                            )
                        }
                    ) { paddingValues ->
                        Column(
                            modifier = Modifier
                                .padding(paddingValues)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                if (uiState.currentAccountInfo != null) {
                                    AccountTile(
                                        account = uiState.currentAccountInfo!!,
                                        onClick = {
                                            scope.launch {
                                                sheetState.show()
                                            }
                                        },
                                        onAvatarClick = {
                                            startActivity(userDetailNavigation.newIntent(
                                                UserDetailNavigationArgs.UserName(it.user?.userName ?: it.account.userName)
                                            ))
                                        }
                                    )
                                    TextButton(
                                        onClick = {
                                            viewModel.signOut(uiState.currentAccountInfo!!.account)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(stringResource(id = R.string.sign_out))
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp)
                            ) {

                                Button(
                                    onClick = {
                                        scope.launch {
                                            sheetState.show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(32.dp)
                                ) {
                                    Text(stringResource(id = R.string.switch_account))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Button(
                                    onClick = {
                                        startActivity(authorizationNavigation.newIntent(AuthorizationArgs.New))
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(32.dp)
                                ) {
                                    Text(stringResource(id = R.string.add_account))
                                }

                            }
                        }
                    }
                }

            }
        }

    }
}


class AccountSettingActivityNavigationImpl @Inject constructor(
    val activity: Activity
) : AccountSettingNavigation {
    override fun newIntent(args: Unit): Intent {
        return Intent(activity, AccountSettingActivity::class.java)
    }
}