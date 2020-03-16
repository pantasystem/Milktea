package jp.panta.misskeyandroidclient.view.notes.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentPollEditorBinding
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.poll.PollChoice
import kotlinx.android.synthetic.main.fragment_poll_editor.*

class PollEditorFragment : Fragment(R.layout.fragment_poll_editor){

    lateinit var mBinding: FragmentPollEditorBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        return super.onCreateView(inflater, container, savedInstanceState)
        val binding = DataBindingUtil.inflate<FragmentPollEditorBinding>(inflater, R.layout.fragment_poll_editor, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        mBinding = binding
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity
            ?:return

        val layoutManager = LinearLayoutManager(this.context)
        choices.layoutManager = layoutManager
        val miApplication = context?.applicationContext as MiApplication

        miApplication.currentAccount.observe(viewLifecycleOwner, Observer {
            val viewModel = ViewModelProvider(activity, NoteEditorViewModelFactory(it, miApplication)).get(NoteEditorViewModel::class.java)
            val poll = viewModel.poll.value ?: return@Observer
            mBinding.pollEditor = poll
            mBinding.noteEditorViewModel = viewModel

            val adapter = PollChoicesAdapter(poll, viewLifecycleOwner)
            choices.adapter = adapter
            poll.choices.observe(viewLifecycleOwner, Observer{list ->
                adapter.submitList(list)
            })
        })
    }
}