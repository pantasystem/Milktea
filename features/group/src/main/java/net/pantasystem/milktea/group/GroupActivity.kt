package net.pantasystem.milktea.group

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_navigation.UserDetailNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigationArgs
import javax.inject.Inject

@AndroidEntryPoint
class GroupActivity : AppCompatActivity() {

    private val groupListViewModel by viewModels<GroupListViewModel>()

    @Inject
    lateinit var applyTheme: ApplyTheme

    @Inject
    lateinit var userDetailNavigation: UserDetailNavigation

    private val groupDetailViewModel: GroupDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme.invoke()
        setContent {
            val navController = rememberNavController()
            val uiState by groupListViewModel.uiState.collectAsState()

            fun popBackStack() {
                if (!navController.popBackStack()) {
                    finish()
                }
            }

            MdcTheme {
                NavHost(navController = navController, startDestination = "groups") {
                    composable("groups") {
                        GroupCardListPage(uiState = uiState, onAction = { action ->
                            when (action) {
                                is GroupCardListAction.OnClick -> {
                                    groupDetailViewModel.setState(
                                        GroupDetailUiStateType.Show(action.group.group.id)
                                    )
                                    navController.navigate("detail")
                                }
                                GroupCardListAction.OnFabClick -> {
                                    groupDetailViewModel.setState(
                                        GroupDetailUiStateType.Editing(
                                            null
                                        )
                                    )
                                    navController.navigate("detail")
                                }
                                GroupCardListAction.OnNavigateUp -> {
                                    popBackStack()
                                }
                            }
                        })
                    }

                    composable("detail") {
                        GroupDetailStatePage(
                            groupDetailViewModel = groupDetailViewModel,
                            onAction = { action ->
                                when (action) {
                                    is GroupDetailStatePageAction.OnShowUser -> {
                                        userDetailNavigation.newIntent(
                                            UserDetailNavigationArgs.UserId(
                                                action.user.id
                                            )
                                        )
                                    }
                                    GroupDetailStatePageAction.PopBackStack -> popBackStack()
                                }
                            })
                    }
                }


            }


        }
    }

    override fun onResume() {
        super.onResume()

        groupListViewModel.sync()
    }
}