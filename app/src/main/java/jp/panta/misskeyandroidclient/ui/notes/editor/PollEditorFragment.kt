package jp.panta.misskeyandroidclient.ui.notes.editor

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentPollEditorBinding
import jp.panta.misskeyandroidclient.model.notes.PollExpiresAt
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

class PollEditorFragment : Fragment(R.layout.fragment_poll_editor){

    private val mBinding: FragmentPollEditorBinding by dataBinding()
    private var mNoteEditorViewModel: NoteEditorViewModel? = null



    @OptIn(ExperimentalTime::class, FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("PollEditorFragment", "onViewCreated")

        val layoutManager = LinearLayoutManager(this.context)
        mBinding.choices.layoutManager = layoutManager
        val miApplication = context?.applicationContext as MiApplication
        val viewModel = ViewModelProvider(requireActivity(), NoteEditorViewModelFactory(miApplication)).get(NoteEditorViewModel::class.java)
        mNoteEditorViewModel = viewModel
        lifecycleScope.launchWhenResumed {
            viewModel.poll.filterNotNull().collect { poll ->
                mBinding.pollEditingState = poll
            }

        }
        mBinding.noteEditorViewModel = viewModel

        val adapter = PollChoicesAdapter(
            viewLifecycleOwner,
            onChoiceTextChangedListener = viewModel::changePollChoice,
            onChoiceDeleteButtonClickListener = viewModel::removePollChoice
        )
        mBinding.choices.adapter = adapter

        lifecycleScope.launchWhenResumed {
            viewModel.poll.map {
                it?.choices?: emptyList()
            }.distinctUntilChangedBy { list ->
                list.map {
                    it.id.toString()
                }
            }.collect {
                adapter.submitList(it)
            }
        }


        mBinding.addChoiceButton.setOnClickListener {
            viewModel.addPollChoice()
        }


        val deadLineType = view.context.resources.getStringArray(R.array.deadline_choices)
        mBinding.deadLineType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {
                if(deadLineType[position] == getString(R.string.indefinite_period)){
                    viewModel.updateState(viewModel.state.value.changePollExpiresAt(PollExpiresAt.Infinity))
                }else{
                    viewModel.updateState(
                        viewModel
                            .state
                            .value
                            .changePollExpiresAt(
                                PollExpiresAt.DateAndTime(
                                    Clock.System.now().plus(1.days)
                                )
                            )
                    )


                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) = Unit
        }

        mBinding.dateButton.setOnClickListener {
            // date picker
            mNoteEditorViewModel?.showPollDatePicker?.event = Unit
        }

        mBinding.timeButton.setOnClickListener {
            // time picker
            mNoteEditorViewModel?.showPollTimePicker?.event = Unit
        }
    }
}