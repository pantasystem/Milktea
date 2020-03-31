package jp.panta.misskeyandroidclient.view.notes.editor

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemReactionPreviewBinding
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel

class CustomEmojiCompleteAdapter(
    private val emojis: List<String>,
    private val context: Context
) : BaseAdapter(), Filterable, TextWatcher {

    var suggestions = listOf<String>()
        private set

    /**
     * 入力中のテキスト
     */
    private var mText: CharSequence? = null

    /**
     * 絵文字の可能性のあるテキストの開始位置
     */
    private var mEmojiStart: Int? = null

    /**
     * カーソルの位置
     */
    private var mInputtingLatestStart: Int? = null

    override fun getCount(): Int {
        return suggestions.size
    }

    override fun getItem(position: Int): String {
        return suggestions[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val binding: ItemReactionPreviewBinding
        if(view == null){
            val inflater = LayoutInflater.from(context)
            binding = DataBindingUtil.inflate<ItemReactionPreviewBinding>(inflater, R.layout.item_reaction_preview, parent, false)
            view = binding.root
            view.tag = binding
        }else{
            binding = view.tag as ItemReactionPreviewBinding
        }

        binding.reaction = getItem(position)

        return binding.root
    }

    private val mFilter = object : Filter(){
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            suggestions = listOf()

            val text = mText
            val inputLatestStart = mInputtingLatestStart
            val emojiStart = mEmojiStart
            if(inputLatestStart == null || emojiStart == null || text == null){
                suggestions = emptyList()
            }else if(emojiStart > inputLatestStart || text.length < emojiStart && text.length <= inputLatestStart + 1){
                suggestions = emptyList()
            }else{
                val emojiPossibility = text.substring(emojiStart, inputLatestStart + 1)
                suggestions = emojis.filter{
                    it.contains(emojiPossibility)
                }
            }

            val results = FilterResults()
            results.values = suggestions
            results.count = suggestions.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            val resultCount = results?.count?: 0
            if(resultCount > 0){
                notifyDataSetChanged()
            }else{
                notifyDataSetInvalidated()
            }
        }
    }
    override fun getFilter(): Filter {
        return mFilter
    }

    override fun afterTextChanged(p0: Editable?) = Unit

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
        if(text.isNullOrBlank()){
            return
        }
        if(text[start] == ':'){
            mEmojiStart = start
        }else{
            mInputtingLatestStart = start
        }
        mText = text
    }
}