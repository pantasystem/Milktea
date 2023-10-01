package net.pantasystem.milktea.common_android_ui.reaction

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import net.pantasystem.milktea.common_android_ui.R
import net.pantasystem.milktea.common_android_ui.databinding.ItemReactionPreviewBinding
import net.pantasystem.milktea.model.emoji.CustomEmoji

class ReactionAutoCompleteArrayAdapter(
    private val reactions: List<CustomEmoji>,
    private val context: Context
) : BaseAdapter(), Filterable{

    var suggestions = listOf<String>()
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
            var suggestions = listOf<String>()

            val text = constraint?.toString()
            if(text != null){
                suggestions = reactions.filter{
                    it.name.startsWith(text.replace(":", ""))
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
            suggestions = (results?.values as? List<*>?)?.mapNotNull {
                it as? String
            } ?: emptyList()
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