package jp.panta.misskeyandroidclient.view.text

import android.util.Log
import android.view.View
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import javax.security.auth.Subject

class EmojiAdapter (view: View){

    val weakReference = WeakReference(view)

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