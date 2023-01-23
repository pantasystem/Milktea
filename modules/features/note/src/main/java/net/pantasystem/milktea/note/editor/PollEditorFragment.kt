package net.pantasystem.milktea.note.editor

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import net.pantasystem.milktea.model.notes.PollExpiresAt
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.FragmentPollEditorBinding
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorViewModel
import kotlin.time.Duration.Companion.days

@AndroidEntryPoint
class PollEditorFragment : Fragment(R.layout.fragment_poll_editor){

    private val mBinding: FragmentPollEditorBinding by dataBinding()
    private var mNoteEditorViewModel: NoteEditorViewModel? = null


    val viewModel: NoteEditorViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("PollEditorFragment", "onViewCreated")

        val layoutManager = LinearLayoutManager(this.context)
        mBinding.choices.layoutManager = layoutManager
        mNoteEditorViewModel = viewModel
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.poll.filterNotNull().collect { poll ->
                    mBinding.pollEditingState = poll
                }
            }


        }
        mBinding.noteEditorViewModel = viewModel

        val adapter = PollChoicesAdapter(
            viewLifecycleOwner,
            onChoiceTextChangedListener = viewModel::changePollChoice,
            onChoiceDeleteButtonClickListener = viewModel::removePollChoice
        )
        mBinding.choices.adapter = adapter

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
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
        }


        mBinding.addChoiceButton.setOnClickListener {
            viewModel.addPollChoice()
        }


        val deadLineType = view.context.resources.getStringArray(R.array.deadline_choices)
        mBinding.deadLineType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {
                if(deadLineType[position] == getString(R.string.indefinite_period)){
                    viewModel.setPollExpiresAt(PollExpiresAt.Infinity)
                }else{
                    viewModel.setPollExpiresAt(PollExpiresAt.DateAndTime(
                        Clock.System.now().plus(1.days)
                    ))
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