package net.pantasystem.milktea.user.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_compose.MilkteaStyleConfigApplyAndTheme
import net.pantasystem.milktea.common_navigation.SearchAndSelectUserNavigation
import net.pantasystem.milktea.common_navigation.SearchAndSelectUserNavigationArgs
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.user.search.SearchAndSelectUserScreen
import net.pantasystem.milktea.user.search.SearchUserViewModel
import net.pantasystem.milktea.user.viewmodel.SelectedUserViewModel
import net.pantasystem.milktea.user.viewmodel.provideViewModel
import java.io.Serializable
import javax.inject.Inject

class SearchAndSelectUserNavigationImpl @Inject constructor(
    val activity: Activity
) : SearchAndSelectUserNavigation {
    override fun newIntent(args: SearchAndSelectUserNavigationArgs): Intent {
        return SearchAndSelectUserActivity.newIntent(
            activity,
            args.selectableMaximumSize,
            args.selectedUserIds,
            args.accountId,
        )
    }
}

@AndroidEntryPoint
class SearchAndSelectUserActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_SELECTABLE_MAXIMUM_SIZE =
            SearchAndSelectUserNavigation.EXTRA_SELECTABLE_MAXIMUM_SIZE
        private const val EXTRA_SELECTED_USER_IDS =
            SearchAndSelectUserNavigation.EXTRA_SELECTED_USER_IDS

        const val EXTRA_SELECTED_USER_CHANGED_DIFF =
            SearchAndSelectUserNavigation.EXTRA_SELECTED_USER_CHANGED_DIFF

        fun newIntent(
            context: Context,
            selectableMaximumSize: Int = Int.MAX_VALUE,
            selectedUserIds: List<User.Id> = emptyList(),
            accountId: Long? = null,
        ): Intent {
            return Intent(context, SearchAndSelectUserActivity::class.java).apply {
                putExtra(EXTRA_SELECTABLE_MAXIMUM_SIZE, selectableMaximumSize)
                putExtra(EXTRA_SELECTED_USER_IDS, ArrayList<Serializable>(selectedUserIds))
                putExtra(SearchUserViewModel.EXTRA_ACCOUNT_ID, accountId)
            }
        }
    }

    @Inject
    lateinit var factory: SelectedUserViewModel.AssistedViewModelFactory

    @Inject
    lateinit var applyTheme: ApplyTheme

    @Inject
    internal lateinit var configRepository: LocalConfigRepository

    private val selectedUserViewModel: SelectedUserViewModel by viewModels {
        val selectedUserIdList =
            (intent.getSerializableExtra(EXTRA_SELECTED_USER_IDS) as? ArrayList<*>)?.mapNotNull {
                it as? User.Id
            } ?: emptyList()
        SelectedUserViewModel.provideViewModel(factory, selectedUserIdList, emptyList())
    }

    private val searchUserViewModel: SearchUserViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()

        setContent {
            MilkteaStyleConfigApplyAndTheme(configRepository = configRepository) {
                SearchAndSelectUserScreen(
                    searchUserViewModel = searchUserViewModel,
                    selectedUserViewModel = selectedUserViewModel,
                    onNavigateUp = {
                        setResultFinish()
                    }
                )
            }

        }

        onBackPressedDispatcher.addCallback {
            setResultFinish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> setResultFinish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setResultFinish() {
        val selectedDiff = selectedUserViewModel.getSelectedUserIdsChangedDiff()

        val intent = Intent()

        Log.d("SearchAndSelectAC", "新たに追加:${selectedDiff.added}, 削除:${selectedDiff.removed}")
        intent.putExtra(EXTRA_SELECTED_USER_CHANGED_DIFF, selectedDiff)
        setResult(RESULT_OK, intent)
        finish()

    }


}
