package net.pantasystem.milktea.antenna

import android.widget.ImageButton
import androidx.databinding.BindingAdapter
import net.pantasystem.milktea.antenna.viewmodel.AntennaListItem

object AntennaPagedStateHelper{

    @JvmStatic
    @BindingAdapter("targetAntenna")
    fun ImageButton.setPagedState(antenna: AntennaListItem?){
        if(antenna?.isAddedToTab == true){
            this.setImageResource(R.drawable.ic_remove_to_tab_24px)
        }else {
            this.setImageResource(R.drawable.ic_add_to_tab_24px)
        }
    }
}