package jp.panta.misskeyandroidclient

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.ui.users.SearchAndSelectUserScreen
import jp.panta.misskeyandroidclient.ui.users.viewmodel.search.SearchUserViewModel
import jp.panta.misskeyandroidclient.ui.users.viewmodel.selectable.SelectedUserViewModel
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.model.user.User
import java.io.Serializable

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SearchAndSelectUserActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_SELECTABLE_MAXIMUM_SIZE =
            "jp.panta.misskeyandroidclient.EXTRA_SELECTABLE_MAXIMUM_SIZE"
        private const val EXTRA_SELECTED_USER_IDS =
            "jp.panta.misskeyandroidclient.EXTRA_SELECTED_USER_IDS"

        const val EXTRA_SELECTED_USER_CHANGED_DIFF =
            "jp.panta.misskeyandroidclient.EXTRA_SELECTED_USER_CHANGED_DIFF"

        fun newIntent(
            context: Context,
            selectableMaximumSize: Int = Int.MAX_VALUE,
            selectedUserIds: List<User.Id> = emptyList()
        ): Intent {
            return Intent(context, SearchAndSelectUserActivity::class.java).apply {
                putExtra(EXTRA_SELECTABLE_MAXIMUM_SIZE, selectableMaximumSize)
                putExtra(EXTRA_SELECTED_USER_IDS, ArrayList<Serializable>(selectedUserIds))
            }
        }
    }

    private var mSelectedUserIds: List<User.Id>? = null

    @FlowPreview
    @ExperimentalCoroutinesApi
    private lateinit var selectedUserViewModel: SelectedUserViewModel

    private val searchUserViewModel: SearchUserViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()


        val selectedUserIdList =
            (intent.getSerializableExtra(EXTRA_SELECTED_USER_IDS) as? ArrayList<*>)?.mapNotNull {
                it as? User.Id
            }
        mSelectedUserIds = selectedUserIdList
        Log.d(this.localClassName, "selected user ids :$selectedUserIdList")


        val miCore = applicationContext as MiCore


        this.selectedUserViewModel =
            ViewModelProvider(
                this,
                SelectedUserViewModel.Factory(
                    miCore,
                    selectedUserIdList,
                    null
                )
            )[SelectedUserViewModel::class.java]

        setContent {
            MdcTheme {
                SearchAndSelectUserScreen(
                    searchUserViewModel = searchUserViewModel,
                    selectedUserViewModel = selectedUserViewModel,
                    onNavigateUp = {
                        setResultFinish()
                    }
                )
            }

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> setResultFinish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        setResultFinish()
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
