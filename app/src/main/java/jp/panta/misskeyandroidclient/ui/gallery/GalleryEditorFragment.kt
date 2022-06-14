package jp.panta.misskeyandroidclient.ui.gallery

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
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.GalleryPostsActivity
import jp.panta.misskeyandroidclient.ui.gallery.viewmodel.EditType
import jp.panta.misskeyandroidclient.ui.gallery.viewmodel.GalleryEditorViewModel
import kotlinx.coroutines.*
import net.pantasystem.milktea.drive.DriveActivity
import net.pantasystem.milktea.drive.toAppFile
import net.pantasystem.milktea.media.MediaActivity
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.file.toFile
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


    val viewModel: GalleryEditorViewModel by viewModels()


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
                MdcTheme {
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
                val intent = MediaActivity.newInstance(
                    requireActivity(),
                    listOf(action.appFile.toFile()),
                    0
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
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.save()
            withContext(Dispatchers.Main) {
                (requireActivity() as? GalleryPostsActivity)?.pop()
            }
        }
    }


    private fun showDrivePicker() {
        val intent = Intent(requireContext(), DriveActivity::class.java)
        intent.putExtra(DriveActivity.EXTRA_INT_SELECTABLE_FILE_MAX_SIZE, Int.MAX_VALUE)
        intent.action = Intent.ACTION_OPEN_DOCUMENT

        driveActivityResult.launch(intent)
    }

    private val driveActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                val result =
                    it.data?.getSerializableExtra(DriveActivity.EXTRA_SELECTED_FILE_PROPERTY_IDS) as? ArrayList<*>
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
                viewModel.addFile(uri.toAppFile(requireContext()))
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