package jp.panta.misskeyandroidclient.view.notes.reaction

import android.util.Log
import android.view.View
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

object ReactionAdapterHelper {
    @JvmStatic
    @BindingAdapter("setReactions")
    fun RecyclerView.setReactions(list: MutableLiveData<LinkedHashMap<String, Int>?>){
        Log.d("ReactionAdapterHelper", "reactions ${list.value}")
        if(list.value.isNullOrEmpty()){
            this.visibility = View.GONE
        }else{
            this.visibility = View.VISIBLE
            this.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
            val adapter = this.adapter as ReactionAdapter?
            if(adapter == null){
                Log.d("ReactionAdapterHelper", "adapter is Null")
            }
            adapter?.submitList(list.value?.toList())
        }


    }
}