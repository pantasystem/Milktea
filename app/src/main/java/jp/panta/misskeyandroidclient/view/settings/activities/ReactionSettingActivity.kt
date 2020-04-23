package jp.panta.misskeyandroidclient.view.settings.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.Observer
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.view.text.CustomEmojiDecorator
import kotlinx.android.synthetic.main.activity_reaction_setting.*

class ReactionSettingActivity : AppCompatActivity() {

    private lateinit var mCustomEmojiDecorator: CustomEmojiDecorator
    private var mEmojis: List<Emoji> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reaction_setting)
        setSupportActionBar(reactionSettingToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val miApplication = applicationContext as MiApplication
        mCustomEmojiDecorator = CustomEmojiDecorator()
        miApplication.currentAccount.observe(this, Observer {
            mEmojis = miApplication.getCurrentInstanceMeta()?.emojis?: emptyList()
        })


    }

    override fun onStop(){
        super.onStop()
    }
}
