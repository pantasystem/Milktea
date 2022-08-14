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
import javax.inject.Inject

@AndroidEntryPoint
class GroupActivity : AppCompatActivity() {

    private val groupListViewModel by viewModels<GroupListViewModel>()

    @Inject
    lateinit var applyTheme: ApplyTheme

    private val groupDetailViewModel: GroupDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme.invoke()
        setContent {
            val navController = rememberNavController()
            val uiState by groupListViewModel.uiState.collectAsState()
            val detailUiState by groupDetailViewModel.uiState.collectAsState()

            fun popBackStack() {
                if (!navController.popBackStack()) {
                    finish()
                }
            }

            MdcTheme {
                NavHost(navController = navController, startDestination = "groups") {
                    composable("groups") {
                        GroupCardListPage(uiState = uiState, onAction = { action ->
                            when(action) {
                                is GroupCardListAction.OnClick -> {
                                    groupDetailViewModel.setState(
                                        GroupDetailUiStateType.Show(action.group.group.id)
                                    )
                                    navController.navigate("detail")
                                }
                                GroupCardListAction.OnFabClick -> {
                                    groupDetailViewModel.setState(GroupDetailUiStateType.Editing(null))
                                    navController.navigate("detail")
                                }
                                GroupCardListAction.OnNavigateUp -> {
                                    popBackStack()
                                }
                            }
                        })
                    }

                    composable("detail") {
                        GroupDetailPage(detailUiState, onAction = { action ->
                            when(action) {
                                GroupDetailPageAction.OnNavigateUp -> {
                                    groupDetailViewModel.cancelEditing()
                                    popBackStack()
                                }
                                is GroupDetailPageAction.OnInputName -> {
                                    groupDetailViewModel.setName(action.text)
                                }
                                GroupDetailPageAction.OnConfirmedSave -> {
                                    groupDetailViewModel.save()
                                }
                                GroupDetailPageAction.OnEditingCanceled -> {
                                    groupDetailViewModel.cancelEditing()
                                }
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