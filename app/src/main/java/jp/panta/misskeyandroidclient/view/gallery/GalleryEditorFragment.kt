package jp.panta.misskeyandroidclient.view.gallery

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.R
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments?.getSerializable("EDIT_TYPE") as? EditType ?: EditType.Create(null)

        val miCore = requireContext().applicationContext as MiCore

        val viewModel = ViewModelProvider(this, GalleryEditorViewModel.Factory(args, miCore))[GalleryEditorViewModel::class.java]

        (requireActivity() as AppCompatActivity).also { appCompatActivity ->
            appCompatActivity.setSupportActionBar(binding.toolbar)
            appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        val pickedImageAdapter = SimpleImagePreviewAdapter(
            viewModel
        )
        binding.pickedImages.adapter = pickedImageAdapter
        binding.pickedImages.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.pickedImageButton.setOnClickListener {
            // TODO: Galleryからなのかローカルからなのかソースを選択できるようにする
        }

    }
}