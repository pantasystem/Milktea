package net.pantasystem.milktea.gallery

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import net.pantasystem.milktea.common_compose.MilkteaStyleConfigApplyAndTheme
import net.pantasystem.milktea.common_navigation.*
import net.pantasystem.milktea.gallery.viewmodel.EditType
import net.pantasystem.milktea.gallery.viewmodel.GalleryEditorViewModel
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class GalleryEditorFragment : Fragment() {

    companion object {
        fun newInstance(editType: EditType): GalleryEditorFragment {
            return GalleryEditorFragment().also {
                it.arguments = Bundle().also { args ->
                    args.putSerializable("EDIT_TYPE", editType)
                }
            }
        }
    }

    @Inject
    lateinit var driveFileRepository: DriveFileRepository

    @Inject
    lateinit var dataSource: FilePropertyDataSource

    @Inject
    lateinit var driveNavigation: DriveNavigation

    @Inject
    lateinit var mediaNavigation: MediaNavigation

    @Inject
    internal lateinit var configRepository: LocalConfigRepository

    val viewModel: GalleryEditorViewModel by viewModels()

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments?.getSerializable("EDIT_TYPE") as? EditType ?: EditType.Create(null)
        viewModel.setType(args)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                MilkteaStyleConfigApplyAndTheme(configRepository = configRepository) {
                    GalleryEditorPage(
                        galleryEditorViewModel = viewModel,
                        onAction = this@GalleryEditorFragment::onAction
                    )
                }
            }
        }.rootView
    }

    private fun onAction(action: GalleryEditorPageAction) {
        when (action) {
            is GalleryEditorPageAction.OnSave -> {
                onSave()
            }
            is GalleryEditorPageAction.NavigateToMediaPreview -> {
                val intent = mediaNavigation.newIntent(
                    MediaNavigationArgs.Files(
                        listOf(action.file),
                        0
                    )
                )
                requireActivity().startActivity(intent)
            }
            GalleryEditorPageAction.NavigateUp -> {
                (requireActivity() as? GalleryPostsActivity)?.pop()
            }
            GalleryEditorPageAction.PickDriveFile -> {
                showDrivePicker()
            }
            GalleryEditorPageAction.PickLocalFile -> {
                if (!checkPermission()) {
                    requestReadExternalStoragePermissionResultListener.launch(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                } else {
                    showFilePicker()
                }
            }
        }
    }

    private fun onSave() {
        if (!viewModel.validate()) {
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.save()
            withContext(Dispatchers.Main) {
                (requireActivity() as? GalleryPostsActivity)?.pop()
            }
        }
    }


    private fun showDrivePicker() {
        val intent = driveNavigation.newIntent(
            DriveNavigationArgs(
                selectableFileMaxSize = Int.MAX_VALUE,
            )
        )
        intent.action = Intent.ACTION_OPEN_DOCUMENT

        driveActivityResult.launch(intent)
    }

    @Suppress("DEPRECATION")
    private val driveActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                val result =
                    it.data?.getSerializableExtra(EXTRA_SELECTED_FILE_PROPERTY_IDS) as? ArrayList<*>
                val list = result?.mapNotNull { obj ->
                    obj as? FileProperty.Id
                } ?: emptyList()
                viewModel.addFilePropertyIds(list)

            }
        }

    private fun checkPermission(): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }

    private val pickFileResultListener =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            if (uri != null) {
                viewModel.addFile(uri)
            }
        }

    private fun showFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        pickFileResultListener.launch(intent)
    }

    private val requestReadExternalStoragePermissionResultListener =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                showFilePicker()
            }
        }
}