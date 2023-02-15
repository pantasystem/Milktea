package net.pantasystem.milktea.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common_viewmodel.CurrentPageType
import net.pantasystem.milktea.common_viewmodel.CurrentPageableTimelineViewModel

@AndroidEntryPoint
class AccountFragment : Fragment() {

    private val currentPageableViewModel: CurrentPageableTimelineViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MdcTheme {

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        currentPageableViewModel.setCurrentPageType(CurrentPageType.Account)
    }
}