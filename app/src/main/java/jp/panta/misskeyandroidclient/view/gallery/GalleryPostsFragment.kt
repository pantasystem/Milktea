package jp.panta.misskeyandroidclient.view.gallery

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentSwipeRefreshRecyclerViewBinding
import jp.panta.misskeyandroidclient.model.account.page.Pageable

class GalleryPostsFragment : Fragment(R.layout.fragment_swipe_refresh_recycler_view){

    companion object {
        private const val EXTRA_ACCOUNT_ID = "jp.panta.misskeyandroidclient.view.gallery.ACCOUNT_ID"
        private const val EXTRA_PAGEABLE = "jp.panta.misskeyandroidclient.view.gallery.EXTRA_PAGEABLE"

        fun newInstance(pageable: Pageable.Gallery, accountId: Long?) : GalleryPostsFragment {
            return GalleryPostsFragment().apply {
                arguments = Bundle().also {
                    it.putSerializable(EXTRA_PAGEABLE, pageable)
                    if(accountId != null) {
                        it.putLong(EXTRA_ACCOUNT_ID, accountId)
                    }
                }
            }
        }
    }

    val binding: FragmentSwipeRefreshRecyclerViewBinding by dataBinding()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pageable = arguments?.getSerializable(EXTRA_PAGEABLE) as Pageable.Gallery
        val accountId = arguments?.getLong(EXTRA_ACCOUNT_ID)

    }
}