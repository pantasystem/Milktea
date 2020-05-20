package jp.panta.misskeyandroidclient.view.notes.reaction

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.view.notes.reaction.choices.ReactionChoicesFragment
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory
import kotlinx.android.synthetic.main.dialog_select_reaction.*

class ReactionSelectionDialog : BottomSheetDialogFragment() {

    private var mNoteViewModel: NotesViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("ReactionSelectionDialog", "ReactionSelectionDialog#onCreateView")
        return inflater.inflate(R.layout.dialog_select_reaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity
        val miApplication = context?.applicationContext as MiApplication
        val ar  = miApplication.currentAccount.value
        /*val emojis = miApplication.getCurrentInstanceMeta()?.emojis?.map{
            ":${it.name}:"
        }*/
        activity?: return
        ar?: return
        val notesViewModel = ViewModelProvider(activity, NotesViewModelFactory(ar, miApplication)).get(NotesViewModel::class.java)
        mNoteViewModel = notesViewModel

        notesViewModel.submittedNotesOnReaction.observe(activity, Observer {
            Log.d("ReactionSelectionDialog", "終了が呼び出された")
            dismiss()
        })

        /*val columns = view.context.resources.getInteger(R.integer.reaction_choices_columns)
        val adapter = ReactionChoicesAdapter(notesViewModel)
        val layoutManager = GridLayoutManager(view.context, columns)*/

        //val ft = activity.supportFragmentManager.beginTransaction()
        /*val ft = childFragmentManager.beginTransaction()
        ft.replace(R.id.reactionChoicesContainer, ReactionChoicesFragment())
        ft.commit()*/
        /*miApplication.getCurrentInstanceMeta()?.emojis?.groupBy {

        }*/
        val category = miApplication.getCurrentInstanceMeta()?.emojis?.filter {
            !it.category.isNullOrBlank()
        }?.groupBy {
            it.category?: ""
        }?.keys?: emptySet()

        val pagerAdapter = ReactionChoicesPagerAdapter(category)

        reaction_choices_view_pager.adapter = pagerAdapter
        reaction_choices_tab.setupWithViewPager(reaction_choices_view_pager)

        reaction_input_keyboard.setOnClickListener {

            /*val dialog = ReactionInputDialog()
            dialog.show(childFragmentManager, "ReactionSelectionDialog")*/
            dismiss()
            notesViewModel.showInputReactionEvent.event = Unit
        }


    }

    inner class ReactionChoicesPagerAdapter(
        category: Set<String>
    ) : FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

        private val categoryList = category.toList()
        override fun getCount(): Int {
            return 3 + categoryList.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when(position){
                0 ->{
                    getString(R.string.user)
                }
                1 ->{
                    getString(R.string.often_use)
                }
                2 ->{
                    getString(R.string.all)
                }

                else ->{
                    categoryList[position - 3]
                }
            }
        }
        override fun getItem(position: Int): Fragment {
            return when(position){
                0 ->{
                    ReactionChoicesFragment.newInstance(ReactionChoicesFragment.Type.USER)
                }
                1 ->{
                    ReactionChoicesFragment.newInstance(ReactionChoicesFragment.Type.FREQUENCY)
                }
                2 ->{
                    ReactionChoicesFragment.newInstance(ReactionChoicesFragment.Type.DEFAULT)
                }
                else ->{
                    val categoryName = categoryList[position - 3]
                    ReactionChoicesFragment.newInstance(ReactionChoicesFragment.Type.CATEGORY, categoryName)
                }
            }
        }
    }

}