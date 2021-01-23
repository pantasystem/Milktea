package jp.panta.misskeyandroidclient.view.antenna

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.flexbox.*
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentAntennaEditorBinding
import jp.panta.misskeyandroidclient.api.v12.antenna.Antenna
import jp.panta.misskeyandroidclient.view.users.UserChipListAdapter
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.antenna.AntennaEditorViewModel

class AntennaEditorFragment : Fragment(R.layout.fragment_antenna_editor){

    companion object {
        const val EXTRA_ANTENNA = "jp.panta.misskeyandroidclient.view.antenna.AntennaEditorFragment.EXTRA_ANTENNA"
        fun newInstance(antenna: Antenna?): AntennaEditorFragment{
            return AntennaEditorFragment().apply{
                arguments = Bundle().apply{
                    putSerializable(EXTRA_ANTENNA, antenna)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = DataBindingUtil.bind<FragmentAntennaEditorBinding>(view)
            ?: return
        binding.lifecycleOwner = viewLifecycleOwner

        val receivedSourceStringArray = resources.getStringArray(R.array.receiving_source_type)



        val antenna = arguments?.getSerializable(EXTRA_ANTENNA) as? Antenna?

        val miCore: MiCore = view.context.applicationContext as MiApplication
        miCore.getCurrentAccount().observe(viewLifecycleOwner, {  ar ->
            val viewModel = ViewModelProvider(requireActivity(), AntennaEditorViewModel.Factory(ar, miCore, antenna))[AntennaEditorViewModel::class.java]
            binding.antennaEditorViewModel = viewModel


            binding.receivingSourceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                   resourceStringToSource(receivedSourceStringArray[position])?.let{
                       viewModel.source.value = it
                   }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }

            viewModel.source.observe(viewLifecycleOwner, {

                val srcIndex = receivedSourceStringArray.indexOf(sourceToResourceString(it))
                Log.d("AntennaEditorViewModel", "srcIndex:$srcIndex, type:$it")
                binding.receivingSourceSpinner.setSelection(srcIndex)


            })


            viewModel.userListList.observe( viewLifecycleOwner, { list ->
                val userListListAdapter = ArrayAdapter(view.context, android.R.layout.simple_spinner_dropdown_item, list.map{
                    it.name
                })
                binding.userListListSpinner.adapter = userListListAdapter


                binding.userListListSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        viewModel.userList.value = list[position]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                }

            })
            viewModel.userList.observe(viewLifecycleOwner, {
                it?.let{ ul ->
                    val index = viewModel.userListList.value?.indexOfFirst { list ->
                        ul.id == list.id
                    }?: 0
                    Log.d("AntennaEditorFragment", "選択したIndex:$index, userList:${viewModel.userList.value}")
                    binding.userListListSpinner.setSelection(index)
                }

            })


            viewModel.groupList.observe( viewLifecycleOwner, {
                it?.let{ groups ->
                    val groupsAdapter = ArrayAdapter(view.context, android.R.layout.simple_spinner_dropdown_item, groups.map{ group ->
                        group.name
                    })
                    binding.groupListSpinner.adapter = groupsAdapter
                    binding.groupListSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            viewModel.group.value = groups[position]
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) = Unit

                    }

                }
            })

            viewModel.group.observe( viewLifecycleOwner, { g ->
                g?.let{
                    val index = viewModel.groupList.value?.indexOfFirst { inG ->
                        g.id == inG.id
                    }?: 0
                    binding.groupListSpinner.setSelection(index)
                }

            })


            val userChipAdapter = UserChipListAdapter(viewLifecycleOwner)
            binding.specifiedUserListView.adapter = userChipAdapter
            val flexBoxLayoutManager = FlexboxLayoutManager(view.context)
            flexBoxLayoutManager.flexDirection = FlexDirection.ROW
            flexBoxLayoutManager.flexWrap = FlexWrap.WRAP
            flexBoxLayoutManager.justifyContent = JustifyContent.FLEX_START
            flexBoxLayoutManager.alignItems = AlignItems.STRETCH
            binding.specifiedUserListView.layoutManager = flexBoxLayoutManager

            viewModel.users.observe( viewLifecycleOwner, {
                userChipAdapter.submitList(it)
            })
        })





    }

    fun sourceToResourceString(src: AntennaEditorViewModel.Source): String{

        return when(src){
            AntennaEditorViewModel.Source.ALL -> getString(R.string.all_notes)
            AntennaEditorViewModel.Source.HOME -> getString(R.string.notes_from_following_users)
            AntennaEditorViewModel.Source.LIST -> getString(R.string.notes_from_specific_list)
            AntennaEditorViewModel.Source.USERS -> getString(R.string.notes_from_specific_users)
            AntennaEditorViewModel.Source.GROUP -> getString(R.string.notes_from_users_in_the_specified_group)
        }
    }

    fun resourceStringToSource(str: String): AntennaEditorViewModel.Source?{
        return when(str){
            getString(R.string.all_notes) -> AntennaEditorViewModel.Source.ALL
            getString(R.string.notes_from_following_users) -> AntennaEditorViewModel.Source.HOME
            getString(R.string.notes_from_specific_list) ->  AntennaEditorViewModel.Source.LIST
            getString(R.string.notes_from_specific_users) ->  AntennaEditorViewModel.Source.USERS
            getString(R.string.notes_from_users_in_the_specified_group) -> AntennaEditorViewModel.Source.GROUP
            else -> null
        }
    }
}