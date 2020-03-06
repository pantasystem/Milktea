package jp.panta.misskeyandroidclient.view.notes.editor

import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.DialogVisibilitySelectionBinding
import jp.panta.misskeyandroidclient.getAppTheme
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModel

class VisibilitySelectionDialog : BottomSheetDialogFragment(){

    private lateinit var mBinding: DialogVisibilitySelectionBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /*val binding = DataBindingUtil.inflate<DialogVisibilitySelectionBinding>(inflater, R.layout.dialog_visibility_selection, container, false)
        mBinding = binding
        return binding.root*/
        return inflater.inflate(R.layout.dialog_visibility_selection, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("VisibilitySelection", "onViewCreated")

        val binding = DataBindingUtil.bind<DialogVisibilitySelectionBinding>(view)
        val viewModel = ViewModelProvider(activity!!)[NoteEditorViewModel::class.java]
        viewModel.visibilitySelectedEvent.observe(viewLifecycleOwner, Observer {
            dismiss()
        })

        binding?.noteEditorViewModel = viewModel


    }

    override fun getTheme(): Int {
        return getAppTheme()
    }
}