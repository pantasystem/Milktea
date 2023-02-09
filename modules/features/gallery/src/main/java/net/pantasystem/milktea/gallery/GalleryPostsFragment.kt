package net.pantasystem.milktea.gallery

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.APIError
import net.pantasystem.milktea.common.ErrorType
import net.pantasystem.milktea.common_navigation.*
import net.pantasystem.milktea.common_viewmodel.CurrentPageableTimelineViewModel
import net.pantasystem.milktea.gallery.viewmodel.GalleryPostsViewModel
import net.pantasystem.milktea.gallery.viewmodel.provideFactory
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.file.FilePreviewSource
import javax.inject.Inject

@Suppress("DEPRECATION")
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

    @Inject
    lateinit var userDetailNavigation: UserDetailNavigation


    @Inject
    lateinit var mediaNavigation: MediaNavigation

    @Inject
    lateinit var authorizationNavigation: AuthorizationNavigation

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
                                        mediaNavigation.newIntent(
                                            MediaNavigationArgs.Files(
                                                files = it.files.map { property ->
                                                    FilePreviewSource.Remote(
                                                        AppFile.Remote(property.id),
                                                        property
                                                    )
                                                },
                                                index = it.index
                                            )
                                        )
                                    )
                                }
                                is GalleryPostCardAction.OnAvatarIconClicked -> {
                                    startActivity(
                                        userDetailNavigation.newIntent(
                                            UserDetailNavigationArgs.UserId(
                                                it.galleryPost.userId
                                            )
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

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.error.collect {
                    if (it is APIError.ClientException && (it.error as? ErrorType.Misskey)?.error?.error?.code == "PERMISSION_DENIED") {
                        Toast.makeText(requireContext(), "再認証が必要です。", Toast.LENGTH_LONG).show()
                        // 再認証をする
                        startActivity(authorizationNavigation.newIntent(AuthorizationArgs.New))
                    }
                }
            }
        }


    }

    override fun onResume() {
        super.onResume()
        currentTimelineViewModel.setCurrentPageable(pageable)
    }

}