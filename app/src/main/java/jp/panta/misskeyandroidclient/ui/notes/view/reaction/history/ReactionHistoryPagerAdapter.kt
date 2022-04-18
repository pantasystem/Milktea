package jp.panta.misskeyandroidclient.ui.notes.view.reaction.history

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryRequest

class ReactionHistoryPagerAdapter(
    fragmentManager: FragmentManager,
    val types: List<net.pantasystem.milktea.model.notes.reaction.ReactionHistoryRequest>,
    val noteId: net.pantasystem.milktea.model.notes.Note.Id
) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){



    override fun getCount(): Int {
        return types.size
    }

    override fun getItem(position: Int): Fragment {
        val req = types[position]
        return ReactionHistoryListFragment.newInstance(req.noteId, req.type)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return types[position].type?: "All"
    }
}

