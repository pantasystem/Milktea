package net.pantasystem.milktea.note.detail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.FragmentNoteDetailPagerBinding
import net.pantasystem.milktea.note.detail.pager.NoteDetailViewPagerAdapter
import net.pantasystem.milktea.note.detail.viewmodel.NoteDetailPagerViewModel
import javax.inject.Inject

@AndroidEntryPoint
class NoteDetailPagerFragment : Fragment(R.layout.fragment_note_detail_pager) {

    companion object {
        fun newInstance(noteId: String, fromPageable: Pageable?, accountId: Long?): Fragment {
            return NoteDetailPagerFragment().apply {
                arguments = Bundle().apply {
                    putString(NoteDetailPagerViewModel.EXTRA_NOTE_ID, noteId)
                    if (accountId != null) {
                        putLong(NoteDetailPagerViewModel.EXTRA_ACCOUNT_ID, accountId)
                    }
                    if (fromPageable != null) {
                        putSerializable(NoteDetailPagerViewModel.EXTRA_FROM_PAGEABLE, fromPageable)
                    }
                }
            }
        }
    }

    @Inject
    internal lateinit var pageableFragmentFactory: PageableFragmentFactory

    private val binding by dataBinding<FragmentNoteDetailPagerBinding>()

    val viewModel: NoteDetailPagerViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = NoteDetailViewPagerAdapter(this, pageableFragmentFactory)

        binding.viewPager.adapter = viewPager

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.noteIds.collect { noteIds ->
                    viewPager.submitList(noteIds)
                }
            }
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 0) {
                    viewModel.loadFuture()
                    return
                }
                val ids = viewModel.noteIds.value
                if (position == ids.size - 1) {
                    viewModel.loadPrevious()
                }
            }
        })
    }
}