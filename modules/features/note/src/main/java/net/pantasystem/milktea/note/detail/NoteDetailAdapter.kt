package net.pantasystem.milktea.note.detail

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.ItemConversationBinding
import net.pantasystem.milktea.note.databinding.ItemDetailNoteBinding
import net.pantasystem.milktea.note.databinding.ItemNoteBinding
import net.pantasystem.milktea.note.detail.viewmodel.NoteConversationViewData
import net.pantasystem.milktea.note.detail.viewmodel.NoteDetailViewData
import net.pantasystem.milktea.note.detail.viewmodel.NoteDetailViewModel
import net.pantasystem.milktea.note.reaction.ReactionCountAdapter
import net.pantasystem.milktea.note.timeline.NoteFontSizeBinder
import net.pantasystem.milktea.note.view.NoteCardAction
import net.pantasystem.milktea.note.view.NoteCardActionListenerAdapter
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData

class NoteDetailAdapter(
    private val configRepository: LocalConfigRepository,
    private val noteDetailViewModel: NoteDetailViewModel,
    private val viewLifecycleOwner: LifecycleOwner,
    diffUtil: DiffUtil.ItemCallback<PlaneNoteViewData> = object :
        DiffUtil.ItemCallback<PlaneNoteViewData>() {
        override fun areContentsTheSame(
            oldItem: PlaneNoteViewData,
            newItem: PlaneNoteViewData,
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areItemsTheSame(
            oldItem: PlaneNoteViewData,
            newItem: PlaneNoteViewData,
        ): Boolean {
            return oldItem.id == newItem.id
        }
    },
    val onAction: (NoteCardAction) -> Unit,
) : ListAdapter<PlaneNoteViewData, NoteDetailAdapter.ViewHolder>(diffUtil) {

    companion object {
        const val NOTE = 0
        const val DETAIL = 1
        const val CONVERSATION = 2
    }

    abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    class NoteHolder(val binding: ItemNoteBinding) : ViewHolder(binding.root)
    class DetailNoteHolder(val binding: ItemDetailNoteBinding) : ViewHolder(binding.root)
    class ConversationHolder(val binding: ItemConversationBinding) : ViewHolder(binding.root)

    val noteCardActionListenerAdapter = NoteCardActionListenerAdapter(onAction)

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is NoteConversationViewData -> CONVERSATION
            is NoteDetailViewData -> DETAIL
            else -> NOTE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val config = configRepository.get().getOrNull() ?: DefaultConfig.config
        return when (viewType) {
            NOTE -> {
                val binding = DataBindingUtil.inflate<ItemNoteBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.item_note,
                    parent,
                    false
                )
                NoteFontSizeBinder.from(binding.simpleNote).bind(
                    headerFontSize = config.noteHeaderFontSize,
                    contentFontSize = config.noteContentFontSize,
                )
                NoteHolder(binding)
            }
            DETAIL -> {
                val binding = DataBindingUtil.inflate<ItemDetailNoteBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.item_detail_note,
                    parent,
                    false
                )
                NoteFontSizeBinder(
                    contentViews = NoteFontSizeBinder.ContentViews(
                        cwView = binding.cw,
                        textView = binding.text,
                    ),
                    userInfoViews = NoteFontSizeBinder.HeaderViews(
                        nameView = binding.mainName,
                        userNameView = binding.subName,
                        elapsedTimeView = null,
                    ),
                    quoteToContentViews = NoteFontSizeBinder.ContentViews(
                        cwView = binding.subCw,
                        textView = binding.subNoteText,
                    ),
                    quoteToUserInfoViews = NoteFontSizeBinder.HeaderViews(
                        nameView = binding.subNoteMainName,
                        userNameView = binding.subNoteSubName,
                        elapsedTimeView = null,
                    )
                ).bind(
                    headerFontSize = config.noteHeaderFontSize,
                    contentFontSize = config.noteContentFontSize
                )
                DetailNoteHolder(binding)
            }
            CONVERSATION -> {
                val binding = DataBindingUtil.inflate<ItemConversationBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.item_conversation,
                    parent,
                    false
                )
                ConversationHolder(binding)
            }
            else -> throw IllegalArgumentException("NOTE, DETAIL, CONVERSATIONしか許可されていません")

        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = getItem(position)
        //val reactionAdapter = createReactionAdapter(note)
        //val layoutManager = LinearLayoutManager(holder.itemView.context)
        when (holder) {
            is NoteHolder -> {
                holder.binding.note = note
//                setReactionCounter(note, holder.binding.simpleNote.reactionView)

                holder.binding.lifecycleOwner = viewLifecycleOwner
                holder.binding.noteCardActionListener = noteCardActionListenerAdapter
                holder.binding.executePendingBindings()
            }
            is DetailNoteHolder -> {
                holder.binding.note = note as NoteDetailViewData
                holder.binding.noteCardActionListener = noteCardActionListenerAdapter
//                setReactionCounter(note, holder.binding.reactionView)
                holder.binding.lifecycleOwner = viewLifecycleOwner
                holder.binding.executePendingBindings()
            }
            is ConversationHolder -> {
                Log.d(
                    "NoteDetailAdapter",
                    "conversation: ${(note as NoteConversationViewData).conversation.value?.size}"
                )
                holder.binding.childrenViewData = note
//                setReactionCounter(note, holder.binding.childNote.reactionView)

                holder.binding.noteDetailViewModel = noteDetailViewModel
                val adapter = NoteChildConversationAdapter(configRepository, viewLifecycleOwner, onAction)
                holder.binding.conversationView.adapter = adapter
                holder.binding.conversationView.layoutManager =
                    LinearLayoutManager(holder.itemView.context)
                holder.binding.noteCardActionListener = noteCardActionListenerAdapter
                note.conversation.observe(viewLifecycleOwner) {
                    adapter.submitList(it)
                }

                holder.binding.lifecycleOwner = viewLifecycleOwner

                holder.binding.executePendingBindings()
            }
        }

    }

    private var job: Job? = null

    private fun setReactionCounter(note: PlaneNoteViewData, reactionView: RecyclerView) {

        val reactionList = note.reactionCountsViewData.value
        val adapter = ReactionCountAdapter {
            noteCardActionListenerAdapter.onReactionCountAction(it)
        }
        adapter.note = note
        reactionView.adapter = adapter

        adapter.submitList(reactionList)

        job?.cancel()
        job = note.reactionCountsViewData.onEach {
            adapter.submitList(it.toList())
        }.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .launchIn(viewLifecycleOwner.lifecycleScope)

        val exLayoutManager = reactionView.layoutManager
        if (exLayoutManager !is FlexboxLayoutManager) {
            val flexBoxLayoutManager = FlexboxLayoutManager(reactionView.context)
            flexBoxLayoutManager.flexDirection = FlexDirection.ROW
            flexBoxLayoutManager.flexWrap = FlexWrap.WRAP
            flexBoxLayoutManager.justifyContent = JustifyContent.FLEX_START
            flexBoxLayoutManager.alignItems = AlignItems.STRETCH
            reactionView.layoutManager = flexBoxLayoutManager
        }

        if (reactionList.isNotEmpty()) {
            reactionView.visibility = View.VISIBLE
        }

    }


}