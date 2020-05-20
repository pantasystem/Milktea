package jp.panta.misskeyandroidclient.view.settings.page

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.PageType
import jp.panta.misskeyandroidclient.model.v12.MisskeyAPIV12
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.setting.page.PageSettingViewModel
import kotlinx.android.synthetic.main.dialog_select_page_to_add.view.*

class SelectPageToAddDialog : BottomSheetDialogFragment(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(dialog.context, R.layout.dialog_select_page_to_add, null)
        dialog.setContentView(view)
        val miCore = view.context.applicationContext as MiCore

        val viewModel = ViewModelProvider(requireActivity())[PageSettingViewModel::class.java]
        viewModel.pageAddedEvent.observe(requireActivity(), Observer {
            dismiss()
        })

        val pageTypeList = ArrayList(PageType.values().toList())
        if(miCore.getMisskeyAPI(miCore.currentAccount.value) !is MisskeyAPIV12){
            pageTypeList.remove(PageType.ANTENNA)
        }
        val adapter = PageTypeListAdapter(viewModel)
        view.pageTypeListView.adapter = adapter
        view.pageTypeListView.layoutManager = LinearLayoutManager(view.context)
        adapter.submitList(pageTypeList)
        return dialog
    }
}