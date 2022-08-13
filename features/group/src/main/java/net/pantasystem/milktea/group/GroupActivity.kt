package net.pantasystem.milktea.group

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ui.ApplyTheme
import javax.inject.Inject

@AndroidEntryPoint
class GroupActivity : AppCompatActivity() {

    private val groupListViewModel by viewModels<GroupViewModel>()

    @Inject
    lateinit var applyTheme: ApplyTheme
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme.invoke()
        setContent {
            MdcTheme {
                val uiState by groupListViewModel.uiState.collectAsState()
                Scaffold(
                    Modifier.fillMaxSize()
                ) { padding ->
                    LazyColumn(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    ) {
                        item {
                            Text(uiState.toString())
                        }
                        items(count = uiState.joinedGroups.size) {
                            Text(uiState.joinedGroups[it].group.name)
                        }
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