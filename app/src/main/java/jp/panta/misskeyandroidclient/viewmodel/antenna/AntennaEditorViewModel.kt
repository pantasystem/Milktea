package jp.panta.misskeyandroidclient.viewmodel.antenna

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.v12.antenna.Antenna
import jp.panta.misskeyandroidclient.viewmodel.MiCore

/**
 * @param antenna 新規作成時はnullになる
 */
class AntennaEditorViewModel (
    val miCore: MiCore,
    val antenna: Antenna?
) : ViewModel(){


}