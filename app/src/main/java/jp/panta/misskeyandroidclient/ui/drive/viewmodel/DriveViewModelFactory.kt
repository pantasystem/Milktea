package jp.panta.misskeyandroidclient.ui.drive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class DriveViewModelFactory(
    private val driveSelectableMode: DriveSelectableMode?
) : ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass == DriveViewModel::class.java){
            return DriveViewModel(driveSelectableMode) as T
        }
        throw IllegalArgumentException("DriveViewModel::class.javaを指定してください")
    }
}