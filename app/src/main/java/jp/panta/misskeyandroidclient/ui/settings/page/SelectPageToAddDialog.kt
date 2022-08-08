package jp.panta.misskeyandroidclient.ui.settings.page

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.DialogSelectPageToAddBinding
import jp.panta.misskeyandroidclient.ui.settings.viewmodel.page.PageSettingViewModel
import net.pantasystem.milktea.api.misskey.v12.MisskeyAPIV12
import net.pantasystem.milktea.api.misskey.v12_75_0.MisskeyAPIV1275
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.model.account.page.PageType
import net.pantasystem.milktea.model.account.page.galleryTypes
import javax.inject.Inject

/**
 * タブに追加する要素の候補を表示するダイアログ
 */
@AndroidEntryPoint
class SelectPageToAddDialog : BottomSheetDialogFragment(){

    @Inject lateinit var accountStore: AccountStore
    @Inject lateinit var misskeyAPIProvider: MisskeyAPIProvider

    private val viewModel: PageSettingViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(dialog.context, R.layout.dialog_select_page_to_add, null)
        val binding = DataBindingUtil.bind<DialogSelectPageToAddBinding>(view)
        requireNotNull(binding)
        dialog.setContentView(view)
        viewModel.pageAddedEvent.observe(requireActivity()) {
            dismiss()
        }

        var pageTypeList = PageType.values().toList().toMutableList()
        val api = misskeyAPIProvider.get(accountStore.state.value.currentAccount!!)
        if(api !is MisskeyAPIV12){
            pageTypeList.remove(PageType.ANTENNA)
        }
        if(api !is MisskeyAPIV1275) {
            pageTypeList = pageTypeList.filterNot {
                galleryTypes.contains(it)
            }.toMutableList()
        }
        val adapter = PageTypeListAdapter(viewModel)
        binding.pageTypeListView.adapter = adapter
        binding.pageTypeListView.layoutManager = LinearLayoutManager(view.context)
        adapter.submitList(pageTypeList)
        return dialog
    }
}