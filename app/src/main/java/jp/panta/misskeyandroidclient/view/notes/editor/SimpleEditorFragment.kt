package jp.panta.misskeyandroidclient.view.notes.editor

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.R

interface SimpleEditor{

    val isShowEditorMenu: MutableLiveData<Boolean>
    fun goToNormalEditor()

    fun closeMenu()
    fun openMenu()
}

class SimpleEditorFragment : Fragment(R.layout.fragment_simple_editor){

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)



    }
}