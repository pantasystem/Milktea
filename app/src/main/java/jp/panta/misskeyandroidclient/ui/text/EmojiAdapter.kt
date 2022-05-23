package jp.panta.misskeyandroidclient.ui.text

import android.util.Log
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class EmojiAdapter (view: View){

    private val weakReference = WeakReference(view)

    private val subject = PublishSubject.create<Boolean>()




    fun throttleUpdate(){
        subject.onNext(true)
    }

    fun update(){
        weakReference.get()?.invalidate()
    }

    fun subscribe(): Disposable = subject.distinctUntilChanged()
        .debounce(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            Log.d("EmojiAdapter", "TextViewの更新")
            weakReference.get()?.invalidate()
        }
}