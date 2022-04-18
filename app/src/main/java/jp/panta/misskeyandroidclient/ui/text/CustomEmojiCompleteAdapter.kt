package jp.panta.misskeyandroidclient.ui.text

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemReactionPreviewBinding
import net.pantasystem.milktea.model.emoji.Emoji

class CustomEmojiCompleteAdapter(
    private val emojis: List<net.pantasystem.milktea.model.emoji.Emoji>,
    private val context: Context
) : BaseAdapter(), Filterable {

    var suggestions = listOf<String>()
        private set

    /**
     * 入力中のテキスト
     */
    var constraint: CharSequence? = null
        private set



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
            binding = DataBindingUtil.inflate(inflater, R.layout.item_reaction_preview, parent, false)
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
            this@CustomEmojiCompleteAdapter.constraint = constraint
            suggestions = listOf()

            suggestions = listOf()

            val text = constraint?.toString()
            if(text != null){
                suggestions = emojis.filter{
                    it.name.startsWith(text.replace(":", "")) || it.aliases?.any { alias ->
                        alias.startsWith(text.replace(":", ""))
                    }?: false
                }.map {
                    ":${it.name}:"
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


}