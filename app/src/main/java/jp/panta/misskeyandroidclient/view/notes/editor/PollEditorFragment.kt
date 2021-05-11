package jp.panta.misskeyandroidclient.view.notes.editor

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentPollEditorBinding
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.poll.PollEditor
import java.util.*

class PollEditorFragment : Fragment(R.layout.fragment_poll_editor){

    private val mBinding: FragmentPollEditorBinding by dataBinding()
    private var mPollEditor: PollEditor? = null
    private var mNoteEditorViewModel: NoteEditorViewModel? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val layoutManager = LinearLayoutManager(this.context)
        mBinding.choices.layoutManager = layoutManager
        val miApplication = context?.applicationContext as MiApplication
        val viewModel = ViewModelProvider(requireActivity(), NoteEditorViewModelFactory(miApplication)).get(NoteEditorViewModel::class.java)
        mNoteEditorViewModel = viewModel
        val poll = viewModel.poll.value ?: return
        mPollEditor = poll
        mBinding.pollEditor = poll
        mBinding.noteEditorViewModel = viewModel

        val adapter = PollChoicesAdapter(poll, viewLifecycleOwner)
        mBinding.choices.adapter = adapter
        poll.choices.observe(viewLifecycleOwner, { list ->
            adapter.submitList(list)
        })

        val deadLineType = view.context.resources.getStringArray(R.array.deadline_choices)
        mBinding.deadLineType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {
                if(deadLineType[position] == getString(R.string.indefinite_period)){
                    mPollEditor?.deadLineType?.value = PollEditor.DeadLineType.INDEFINITE_PERIOD
                }else{
                    mPollEditor?.deadLineType?.value = PollEditor.DeadLineType.DATE_AND_TIME
                    val time = mPollEditor?.expiresAt?.value
                    if(time == null){
                        val c = Calendar.getInstance()
                        c.add(Calendar.DATE, 1)

                        mPollEditor?.expiresAt?.value = c.time
                    }

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