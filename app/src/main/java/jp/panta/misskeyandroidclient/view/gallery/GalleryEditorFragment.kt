package jp.panta.misskeyandroidclient.view.gallery

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.DriveActivity
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.api.drive.FilePropertyDTO
import jp.panta.misskeyandroidclient.databinding.FragmentGalleryEditorBinding
import jp.panta.misskeyandroidclient.view.notes.editor.SimpleImagePreviewAdapter
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.gallery.EditType
import jp.panta.misskeyandroidclient.viewmodel.gallery.GalleryEditorViewModel

class GalleryEditorFragment : Fragment(R.layout.fragment_gallery_editor) {

    companion object {
        fun newInstance(editType: EditType) : GalleryEditorFragment{
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

        (requireActivity() as AppCompatActivity).also { appCompatActivity ->
            appCompatActivity.setSupportActionBar(binding.toolbar)
            appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        val pickedImageAdapter = SimpleImagePreviewAdapter(
            viewModel
        )
        binding.pickedImages.adapter = pickedImageAdapter
        binding.pickedImages.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.pickedImageFromLocalButton.setOnClickListener {
            // TODO: Galleryからなのかローカルからなのかソースを選択できるようにする
        }

        binding.pickedImageFromDriveButton.setOnClickListener {
            showDrivePicker()
        }

    }

    private fun showDrivePicker() {
        val intent = Intent(requireContext(), DriveActivity::class.java)
        intent.putExtra(DriveActivity.EXTRA_INT_SELECTABLE_FILE_MAX_SIZE, Int.MAX_VALUE)

        driveActivityResult.launch(intent)
    }

    private val driveActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == RESULT_OK && it.data != null) {
            val result = it.data?.getSerializableExtra(DriveActivity.EXTRA_FILE_PROPERTY_LIST_SELECTED_FILE) as? ArrayList<*>
            val list = result?.mapNotNull { obj ->
                obj as? FilePropertyDTO
            }?: emptyList()
            viewModel.addFilePropertyDTOs(list)
        }
    }
}