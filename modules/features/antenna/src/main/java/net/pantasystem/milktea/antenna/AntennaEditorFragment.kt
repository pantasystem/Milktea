package net.pantasystem.milktea.antenna

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.antenna.databinding.FragmentAntennaEditorBinding
import net.pantasystem.milktea.antenna.viewmodel.AntennaEditorViewModel
import net.pantasystem.milktea.common_android.ui.listview.applyFlexBoxLayout
import net.pantasystem.milktea.common_android_ui.user.UserChipListAdapter
import net.pantasystem.milktea.model.antenna.Antenna
import net.pantasystem.milktea.model.antenna.AntennaSource

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AntennaEditorFragment : Fragment(R.layout.fragment_antenna_editor){

    companion object {
        const val EXTRA_ANTENNA_ID = "net.pantasystem.milktea.antenna.AntennaEditorFragment.EXTRA_ANTENNA_ID"
        fun newInstance(antennaId: Antenna.Id?): AntennaEditorFragment {
            return AntennaEditorFragment().apply{
                arguments = Bundle().apply{
                    antennaId?.let {
                        putSerializable(EXTRA_ANTENNA_ID, antennaId)
                    }
                }
            }
        }
    }

    val viewModel: AntennaEditorViewModel by activityViewModels()

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val antennaId = arguments?.getSerializable(EXTRA_ANTENNA_ID) as? Antenna.Id
        if (antennaId != null) {
            viewModel.setAntennaId(antennaId)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = DataBindingUtil.bind<FragmentAntennaEditorBinding>(view)
            ?: return
        binding.lifecycleOwner = viewLifecycleOwner

        val receivedSourceStringArray = resources.getStringArray(R.array.receiving_source_type)



        binding.antennaEditorViewModel = viewModel


        binding.receivingSourceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                resourceStringToSource(receivedSourceStringArray[position])?.let{
                    viewModel.onSourceChanged(it)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        viewModel.source.onEach {
            val srcIndex = receivedSourceStringArray.indexOf(sourceToResourceString(it))
            Log.d("AntennaEditorViewModel", "srcIndex:$srcIndex, type:$it")
            binding.receivingSourceSpinner.setSelection(srcIndex)

        }.flowWithLifecycle(viewLifecycleOwner.lifecycle).launchIn(viewLifecycleOwner.lifecycleScope)



        viewModel.userListList.observe(viewLifecycleOwner) { list ->
            val userListListAdapter =
                ArrayAdapter(view.context, android.R.layout.simple_spinner_dropdown_item, list.map {
                    it.name
                })
            binding.userListListSpinner.adapter = userListListAdapter


            binding.userListListSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        viewModel.onUserListSelected(list[position])
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                }

        }
        viewModel.userList.onEach {
            it?.let { ul ->
                val index = viewModel.userListList.value?.indexOfFirst { list ->
                    ul.id == list.id
                } ?: 0
                Log.d(
                    "AntennaEditorFragment",
                    "選択したIndex:$index, userList:${viewModel.userList.value}"
                )
                binding.userListListSpinner.setSelection(index)
            }

        }.flowWithLifecycle(viewLifecycleOwner.lifecycle).launchIn(viewLifecycleOwner.lifecycleScope)

//
//        viewModel.groupList.observe( viewLifecycleOwner) {
//            it?.let { groups ->
//                val groupsAdapter = ArrayAdapter(
//                    view.context,
//                    android.R.layout.simple_spinner_dropdown_item,
//                    groups.map { group ->
//                        group.name
//                    })
//                binding.groupListSpinner.adapter = groupsAdapter
//                binding.groupListSpinner.onItemSelectedListener =
//                    object : AdapterView.OnItemSelectedListener {
//                        override fun onItemSelected(
//                            parent: AdapterView<*>?,
//                            view: View?,
//                            position: Int,
//                            id: Long
//                        ) {
//                            viewModel.group.value = groups[position]
//                        }
//
//                        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
//
//                    }
//
//            }
//        }

//        viewModel.group.observe( viewLifecycleOwner) { g ->
//            g?.let {
//                val index = viewModel.groupList.value?.indexOfFirst { inG ->
//                    g.id == inG.id
//                } ?: 0
//                binding.groupListSpinner.setSelection(index)
//            }
//
//        }
//

        binding.antennaNameEditText.addTextChangedListener {
            viewModel.onNameValueChanged(it.toString())
        }
        binding.keywordsToReceiveEditText.addTextChangedListener {
            viewModel.onKeywordsValueChanged(it.toString())
        }

        binding.keywordsToExcludeEditText.addTextChangedListener {
            viewModel.onExcludeKeywordsValueChanged(it.toString())
        }

        binding.caseSensitiveSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onCaseSensitiveChanged(isChecked)
        }

        binding.withFilesSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onWithFileChanged(isChecked)
        }

        binding.notifySwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onNotifyChanged(isChecked)
        }

        binding.includeRepliesSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onWithRepliesChanged(isChecked)
        }

        val userChipAdapter = UserChipListAdapter(viewLifecycleOwner)
        binding.specifiedUserListView.adapter = userChipAdapter
        binding.specifiedUserListView.applyFlexBoxLayout(requireContext())

        viewModel.users.onEach {
            userChipAdapter.submitList(it)
        }.flowWithLifecycle(viewLifecycleOwner.lifecycle).launchIn(viewLifecycleOwner.lifecycleScope)

        binding.lifecycleOwner = viewLifecycleOwner

    }

    @FlowPreview
    fun sourceToResourceString(src: AntennaSource): String{

        return when(src){
            AntennaSource.All -> getString(R.string.all_notes)
            AntennaSource.Home -> getString(R.string.notes_from_following_users)
            AntennaSource.List -> getString(R.string.notes_from_specific_list)
            AntennaSource.Users -> getString(R.string.notes_from_specific_users)
            AntennaSource.Group -> getString(R.string.notes_from_users_in_the_specified_group)
        }
    }

    @FlowPreview
    fun resourceStringToSource(str: String): AntennaSource?{
        return when(str){
            getString(R.string.all_notes) -> AntennaSource.All
            getString(R.string.notes_from_following_users) -> AntennaSource.Home
            getString(R.string.notes_from_specific_list) ->  AntennaSource.List
            getString(R.string.notes_from_specific_users) ->  AntennaSource.Users
            getString(R.string.notes_from_users_in_the_specified_group) -> AntennaSource.Group
            else -> null
        }
    }
}