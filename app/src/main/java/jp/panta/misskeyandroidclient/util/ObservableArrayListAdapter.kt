package jp.panta.misskeyandroidclient.util


import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.recyclerview.widget.RecyclerView

abstract class ObservableArrayListAdapter<T, VH: RecyclerView.ViewHolder>(private val observableArrayList: ObservableArrayList<T>) : RecyclerView.Adapter<VH>(){

    val handler = Handler(Looper.getMainLooper())

    private val onListChangeCallBackListener = object : ObservableList.OnListChangedCallback<ObservableList<T>>(){
        override fun onChanged(sender: ObservableList<T>?) {
            Log.d("", "onChanged")
            handler.post{
                notifyDataSetChanged()
            }
        }

        override fun onItemRangeChanged(sender: ObservableList<T>?, positionStart: Int, itemCount: Int) {
            Log.d("", "onItemRangeChanged")
            handler.post{
                notifyItemRangeChanged(positionStart, itemCount)
            }
        }

        override fun onItemRangeInserted(sender: ObservableList<T>?, positionStart: Int, itemCount: Int) {
            Log.d("", "onItemRangeInserted")
            handler.post{
                notifyItemRangeInserted(positionStart, itemCount)
            }
        }

        override fun onItemRangeMoved(sender: ObservableList<T>?, fromPosition: Int, toPosition: Int, itemCount: Int) {
            Log.d("", "onItemRangeMoved")
            handler.post{
                notifyItemRangeChanged(fromPosition, itemCount)
                notifyItemRangeChanged(toPosition, itemCount)
            }
        }

        override fun onItemRangeRemoved(sender: ObservableList<T>?, positionStart: Int, itemCount: Int) {
            Log.d("", "onItemRangeRemoved")
            handler.post{
                notifyItemRangeRemoved(positionStart, itemCount)
            }
        }
    }

    init{
        observableArrayList.addOnListChangedCallback(onListChangeCallBackListener)
    }




}