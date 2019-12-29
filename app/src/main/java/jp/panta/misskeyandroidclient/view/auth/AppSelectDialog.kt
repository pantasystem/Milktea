package jp.panta.misskeyandroidclient.view.auth

import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.DialogSelectAppBinding
import jp.panta.misskeyandroidclient.viewmodel.auth.custom.CustomAppViewModel

class AppSelectDialog : BottomSheetDialogFragment(){

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        //val view = View.inflate(dialog.context, R.layout.dialog_select_app, null)
        val binding = DataBindingUtil.inflate<DialogSelectAppBinding>(LayoutInflater.from(dialog.context), R.layout.dialog_select_app, null, false)
        dialog.setContentView(binding.root)
        binding.lifecycleOwner = activity
        val ac = activity?: return
        val viewModel = ViewModelProvider(ac)[CustomAppViewModel::class.java]
        binding.customAppViewModel = viewModel

        val adapter = AppListAdapter(ac, viewModel)
        binding.appsView.adapter = adapter
        binding.appsView.layoutManager = LinearLayoutManager(dialog.context)
        adapter.submitList(viewModel.apps.value)

        viewModel.createAppEvent.observe(ac, Observer {
            dismiss()
        })

        viewModel.appSelectedEvent.observe(ac, Observer {
            dismiss()
        })
    }
}