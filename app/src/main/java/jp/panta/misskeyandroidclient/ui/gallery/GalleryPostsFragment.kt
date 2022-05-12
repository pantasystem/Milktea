package jp.panta.misskeyandroidclient.ui.gallery

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.AuthorizationActivity
import jp.panta.misskeyandroidclient.MediaActivity
import jp.panta.misskeyandroidclient.UserDetailActivity
import jp.panta.misskeyandroidclient.ui.gallery.viewmodel.GalleryPostsViewModel
import jp.panta.misskeyandroidclient.ui.gallery.viewmodel.provideFactory
import jp.panta.misskeyandroidclient.viewmodel.timeline.CurrentPageableTimelineViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.common.APIError
import net.pantasystem.milktea.model.account.page.Pageable
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class GalleryPostsFragment : Fragment() {

    companion object {
        private const val EXTRA_ACCOUNT_ID = "jp.panta.misskeyandroidclient.view.gallery.ACCOUNT_ID"
        private const val EXTRA_PAGEABLE =
            "jp.panta.misskeyandroidclient.view.gallery.EXTRA_PAGEABLE"

        fun newInstance(pageable: Pageable.Gallery, accountId: Long?): GalleryPostsFragment {
            return GalleryPostsFragment().apply {
                arguments = Bundle().also {
                    it.putSerializable(EXTRA_PAGEABLE, pageable)
                    if (accountId != null) {
                        it.putLong(EXTRA_ACCOUNT_ID, accountId)
                    }
                }
            }
        }
    }


    val pageable: Pageable.Gallery by lazy {
        arguments?.getSerializable(EXTRA_PAGEABLE) as Pageable.Gallery
    }

    private val currentTimelineViewModel: CurrentPageableTimelineViewModel by activityViewModels()

    @Inject
    lateinit var viewModelFactory: GalleryPostsViewModel.ViewModelAssistedFactory


    val viewModel: GalleryPostsViewModel by viewModels {
        val pageable = arguments?.getSerializable(EXTRA_PAGEABLE) as Pageable.Gallery
        var accountId = arguments?.getLong(EXTRA_ACCOUNT_ID, -1)
        if (accountId == -1L) {
            accountId = null
        }

        GalleryPostsViewModel.provideFactory(viewModelFactory, pageable, accountId)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MdcTheme {
                    GalleryPostCardList(
                        viewModel = viewModel,
                        onAction = {
                            when (it) {
                                is GalleryPostCardAction.OnFavoriteButtonClicked -> {
                                    viewModel.toggleFavorite(it.galleryPost.id)
                                }
                                is GalleryPostCardAction.OnThumbnailClicked -> {
                                    startActivity(
                                        MediaActivity.newInstance(
                                            requireActivity(),
                                            files = it.files.map { property ->
                                                property.toFile()
                                            },
                                            index = it.index
                                        )
                                    )
                                }
                                is GalleryPostCardAction.OnAvatarIconClicked -> {
                                    startActivity(
                                        UserDetailActivity.newInstance(
                                            requireActivity(),
                                            it.galleryPost.userId
                                        )
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenResumed {
            viewModel.error.collect {
                if (it is APIError.ClientException && it.error?.error?.code == "PERMISSION_DENIED") {
                    Toast.makeText(requireContext(), "再認証が必要です。", Toast.LENGTH_LONG).show()
                    // 再認証をする
                    startActivity(Intent(requireContext(), AuthorizationActivity::class.java))
                }
            }
        }


    }

    override fun onResume() {
        super.onResume()
        currentTimelineViewModel.setCurrentPageable(pageable)
    }

}