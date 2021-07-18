package jp.panta.misskeyandroidclient.view.notes.reaction.choices

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentReactionChoicesBinding
import jp.panta.misskeyandroidclient.view.notes.reaction.ReactionResourceMap
import jp.panta.misskeyandroidclient.view.reaction.ReactionChoicesAdapter
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ReactionChoicesFragment : Fragment(){

    companion object{
        private const val TAG = "ReactionChoicesFragment"
        private const val EXTRA_TYPE = "jp.panta.misskeyandroidclient.view.notes.reaction.choices.ReactionChoicesFragment.EXTRA_TYPE"
        private const val EXTRA_CATEGORY = "jp.panta.misskeyandroidclient.vaie.notes.reaction.choices.ReactionChoicesFragment.EXTRA_CATEGORY"
        fun newInstance(type: Type, category: String? = null): ReactionChoicesFragment{
            val fragment = ReactionChoicesFragment()
            val bundle = Bundle().apply{
                putInt(EXTRA_TYPE, type.ordinal)
                putString(EXTRA_CATEGORY, category)
            }
            fragment.arguments = bundle
            return fragment

        }

    }

    enum class Type{
        DEFAULT,
        FREQUENCY,
        CATEGORY,
        USER
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reaction_choices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentReactionChoicesBinding.bind(view)

        val notesViewModel = ViewModelProvider(requireActivity())[NotesViewModel::class.java]

        val columns = view.context.resources.getInteger(R.integer.reaction_choices_columns)

        val adapter =
            ReactionChoicesAdapter(
                notesViewModel
            )
        val layoutManager = GridLayoutManager(view.context, columns)

        binding.reactionsView.layoutManager = layoutManager
        binding.reactionsView.adapter = adapter


        val typeOrdinal = arguments?.getInt(EXTRA_TYPE)?: 0

        when(Type.values()[typeOrdinal]){
            Type.DEFAULT -> showDefault(adapter)
            Type.FREQUENCY -> showFrequency(adapter)
            Type.CATEGORY ->{
                val category = arguments?.getString(EXTRA_CATEGORY)?: return
                showCategoryBy(category, adapter)
            }
            Type.USER ->{
                showUserSettings(adapter)
            }
        }

    }

    private fun showDefault(adapter: ReactionChoicesAdapter){
        val miApplication = context?.applicationContext as MiApplication
        val emojis = miApplication.getCurrentInstanceMeta()?.emojis
        if(emojis == null){
            Log.d(TAG, "emojiの取得に失敗しましたインスタンスの初期化に失敗しているのかな？ emojis is null")
            return
        }

        val defaultReaction = ReactionResourceMap.reactionDrawableMap.map{
            it.key
        }

        val reactions = ArrayList<String>(defaultReaction).apply{
            addAll(emojis.map{
                ":${it.name}:"
            })
        }
        adapter.submitList(reactions)
    }

    private fun showFrequency(adapter: ReactionChoicesAdapter){
        val miApplication = context?.applicationContext as MiApplication
        val ac = miApplication.getCurrentAccount().value?: return
        val emojis = miApplication.getCurrentInstanceMeta()?.emojis
        if(emojis == null){
            Log.d(TAG, "emojiの取得に失敗しました")
            return
        }
        lifecycleScope.launch(Dispatchers.IO){
            val list = miApplication.reactionHistoryDao.sumReactions(ac.instanceDomain).map{
                it.reaction
            }

            Handler(Looper.getMainLooper()).post{
                adapter.submitList(list)
            }
        }
    }

    private fun showCategoryBy(category: String, adapter: ReactionChoicesAdapter){
        val miApplication = context?.applicationContext as MiApplication
        val emojis = miApplication.getCurrentInstanceMeta()?.emojis?.filter{
            it.category == category
        }?.map{
            ":${it.name}:"
        }
        adapter.submitList(emojis)
    }

    private fun showUserSettings(adapter: ReactionChoicesAdapter){
        val miApplication = context?.applicationContext as MiApplication
        lifecycleScope.launch(Dispatchers.IO){
            try{
                val instance = miApplication.getCurrentAccount().value?.instanceDomain
                var reactions = miApplication.reactionUserSettingDao.findByInstanceDomain(instance!!)?.map{
                    it.reaction
                }?: ReactionResourceMap.defaultReaction
                if(reactions.isEmpty()){
                    reactions = ReactionResourceMap.defaultReaction
                }
                Handler(Looper.getMainLooper()).post{
                    adapter.submitList(reactions)
                }
            }catch(e: Exception){
                Log.e("ReactionChoicesFragment", "load error", e)
            }
        }
    }

}