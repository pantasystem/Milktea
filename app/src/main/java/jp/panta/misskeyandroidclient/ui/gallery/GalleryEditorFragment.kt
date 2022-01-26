package jp.panta.misskeyandroidclient.ui.gallery

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.composethemeadapter.MdcTheme
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.DriveActivity
import jp.panta.misskeyandroidclient.GalleryPostsActivity
import jp.panta.misskeyandroidclient.MediaActivity
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentGalleryEditorBinding
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.file.toFile
import jp.panta.misskeyandroidclient.ui.components.FilePreviewTarget
import jp.panta.misskeyandroidclient.util.file.toAppFile
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.gallery.EditType
import jp.panta.misskeyandroidclient.viewmodel.gallery.GalleryEditorViewModel
import kotlinx.coroutines.*

@FlowPreview
@ExperimentalCoroutinesApi
class GalleryEditorFragment : Fragment(R.layout.fragment_gallery_editor) {

    companion object {
        fun newInstance(editType: EditType) : GalleryEditorFragment {
            return GalleryEditorFragment().also {
                it.arguments = Bundle().also { args ->
                    args.putSerializable("EDIT_TYPE", editType)
                }
            }
        }
    }

    val binding: FragmentGalleryEditorBinding by dataBinding()
    private lateinit var viewModel: GalleryEditorViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments?.getSerializable("EDIT_TYPE") as? EditType ?: EditType.Create(null)

        val miCore = requireContext().applicationContext as MiCore

        viewModel = ViewModelProvider(this, GalleryEditorViewModel.Factory(args, miCore))[GalleryEditorViewModel::class.java]
        binding.viewModel = viewModel

        (requireActivity() as AppCompatActivity).also { appCompatActivity ->
            appCompatActivity.setSupportActionBar(binding.toolbar)
            appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        binding.pickedImages.apply {
            setContent {
                MdcTheme {
                    PickedImagePreview(
                        viewModel = viewModel,
                        repository = miCore.getDriveFileRepository(),
                        dataSource = miCore.getFilePropertyDataSource(),
                        onShow = {
                            val file = when(it) {
                                is FilePreviewTarget.Remote -> {
                                    it.fileProperty.toFile()
                                }
                                is FilePreviewTarget.Local -> {
                                    it.file.toFile()
                                }
                            }
                            val intent = MediaActivity.newInstance(
                                requireActivity(),
                                listOf(file),
                                0
                            )
                            requireActivity().startActivity(intent)
                        }
                    )
                }
            }
        }



        binding.pickedImageFromLocalButton.setOnClickListener {
            if(!checkPermission()) {
                requestReadExternalStoragePermissionResultListener.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }else{
                showFilePicker()
            }
        }

        binding.pickedImageFromDriveButton.setOnClickListener {
            showDrivePicker()
        }

        binding.saveButton.setOnClickListener {
            if(!viewModel.validate()) {
                return@setOnClickListener
            }
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.save()
                withContext(Dispatchers.Main) {
                    (requireActivity() as? GalleryPostsActivity)?.pop()
                }
            }
        }

    }


    private fun showDrivePicker() {
        val intent = Intent(requireContext(), DriveActivity::class.java)
        intent.putExtra(DriveActivity.EXTRA_INT_SELECTABLE_FILE_MAX_SIZE, Int.MAX_VALUE)
        intent.action = Intent.ACTION_OPEN_DOCUMENT

        driveActivityResult.launch(intent)
    }

    private val driveActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == RESULT_OK && it.data != null) {
            val result = it.data?.getSerializableExtra(DriveActivity.EXTRA_SELECTED_FILE_PROPERTY_IDS) as? ArrayList<*>
            val list = result?.mapNotNull { obj ->
                obj as? FileProperty.Id
            }?: emptyList()
            viewModel.addFilePropertyIds(list)

        }
    }

    private fun checkPermission(): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }

    private val pickFileResultListener = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val uri = it.data?.data
        if(uri != null) {
            viewModel.addFile(uri.toAppFile(requireContext()))
        }
    }

    private fun showFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        pickFileResultListener.launch(intent)
    }

    private val requestReadExternalStoragePermissionResultListener = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if(it) {
            showFilePicker()
        }
    }
}