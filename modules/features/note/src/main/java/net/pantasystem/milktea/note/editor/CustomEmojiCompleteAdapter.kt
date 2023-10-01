package net.pantasystem.milktea.note.editor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import net.pantasystem.milktea.common_android_ui.databinding.ItemReactionPreviewBinding
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.note.R

class CustomEmojiCompleteAdapter(
    private val emojis: List<Emoji>,
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

            val text = constraint?.toString()
            val suggestions = if(text != null){
                emojis.filter{
                    it.name.startsWith(text.replace(":", ""))
                }.map {
                    ":${it.name}:"
                }
            } else {
                emptyList()
            }

            val results = FilterResults()
            results.values = suggestions
            results.count = suggestions.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            val resultCount = results?.count?: 0
            if(resultCount > 0){
                suggestions = (results?.values as? List<*>?)?.mapNotNull {
                    it as? String?
                } ?: emptyList()
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