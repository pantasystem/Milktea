package jp.panta.misskeyandroidclient.view.notes.reaction.choices

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.view.notes.reaction.ReactionResourceMap
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import kotlinx.android.synthetic.main.fragment_reaction_choices.*
import kotlinx.android.synthetic.main.item_detail_note.*

class ReactionChoicesFragment : Fragment(R.layout.fragment_reaction_choices){

    companion object{
        private const val TAG = "ReactionChoicesFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val notesViewModel = ViewModelProvider(activity!!)[NotesViewModel::class.java]

        val columns = view.context.resources.getInteger(R.integer.reaction_choices_columns)

        val adapter = ReactionChoicesAdapter(notesViewModel)
        val layoutManager = GridLayoutManager(view.context, columns)

        reactions_view.layoutManager = layoutManager
        reaction_view.adapter = adapter

        val miApplication = view.context.applicationContext as MiApplication
        val emojis = miApplication.nowInstanceMeta?.emojis
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
}