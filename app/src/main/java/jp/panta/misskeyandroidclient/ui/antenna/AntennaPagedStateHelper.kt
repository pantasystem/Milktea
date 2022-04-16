package jp.panta.misskeyandroidclient.ui.antenna

import android.widget.ImageButton
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R
import net.pantasystem.milktea.data.model.antenna.Antenna

object AntennaPagedStateHelper{

    @JvmStatic
    @BindingAdapter("targetAntenna", "pagedAntennaIds")
    fun ImageButton.setPagedState(antenna: Antenna?, pagedAntennaIds: Set<Antenna.Id>?){
        if(pagedAntennaIds?.contains(antenna?.id) == true){
            this.setImageResource(R.drawable.ic_remove_to_tab_24px)
        }else {
            this.setImageResource(R.drawable.ic_add_to_tab_24px)
        }
    }
}