package jp.panta.misskeyandroidclient.mfm

import android.app.SearchManager
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.SpannedString
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import jp.panta.misskeyandroidclient.SearchResultActivity
import jp.panta.misskeyandroidclient.UserDetailActivity
import jp.panta.misskeyandroidclient.util.svg.GlideApp
import jp.panta.misskeyandroidclient.view.text.EmojiSpan

object MFMDecorator {


    fun decorate(textView: TextView, node: Root): Spanned{
        return Visitor(textView, node, node).decorate()
            ?: SpannedString(node.sourceText)
    }

    /**
     * VisitorパターンのVisitorとは少し違う
     */
    class Visitor(
        val textView: TextView,
        val root: Root,
        val parent: Element
    ){
        private var position = 0
        private val start = position
        private val spannableStringBuilder = SpannableStringBuilder()


        /**
         * parent のchildElementsをここで処理する
         * @return 担当した要素（Element）のspannableStringBuilderの最終的なポジション
         */
        fun decorate(): Spanned?{
            if(parent is Node){
                parent.childElements.forEach{ ele ->
                    val spanned = Visitor(textView, root, ele).decorate()
                        ?: closeErrorElement(ele)
                    spannableStringBuilder.append(spanned)
                    position += spanned.length - 1
                }
            }

            // parentを装飾してしまう
            return try{
                // 自己がLeafであればparentの情報から装飾され、自己がNodeであればstartとpositionをもとに装飾される
                decorate(parent)
            }catch(e: Exception){
                // 子要素の処理がすべて無駄になることになってしまうがとりあえずはこうする
                null
            }

            // Leafの処理が終わったら自分(parent)のDecorateを開始する
        }


        private fun closeErrorElement(element: Element): Spanned{
            return SpannableString(root.sourceText.substring(element.start, element.end))
        }

        private fun decorate(element: Element): Spanned?{
            return when(element){
                is Leaf ->
                    when(element){
                        is Text-> decorateText(element)
                        is EmojiElement -> decorateEmoji(element)
                        is Search -> decorateSearch(element)
                        is Mention -> decorateMention(element)
                        is Link -> decorateLink(element)
                        is HashTag -> decorateHashTag(element)
                        else -> null
                    }
                is Node ->{
                    spannableStringBuilder
                }
                else -> null
            }

        }

        private fun decorateEmoji(emojiElement: EmojiElement): Spanned?{
            val spanned = SpannableString(emojiElement.text)
            val emojiSpan = EmojiSpan(textView)
            spanned.setSpan(emojiSpan, 0, emojiElement.text.length, 0)
            if(emojiElement.emoji.isSvg()){
                GlideApp.with(textView.context)
                    .`as`(Bitmap::class.java)
                    .load(emojiElement.emoji.url?: emojiElement.emoji.url)
                    .into(emojiSpan.bitmapTarget)
            }else{
                Glide.with(textView)
                    .asDrawable()
                    .load(emojiElement.emoji.url)
                    .into(emojiSpan.target)
            }
            return spanned
        }

        private fun decorateText(text: Text): Spanned?{
            return SpannedString(text.text)
        }

        private fun decorateSearch(search: Search): Spanned?{
            val intent = Intent(Intent.ACTION_SEARCH)
            intent.setClassName("com.google.android.googlequicksearchbox",  "com.google.android.googlequicksearchbox.SearchActivity")
            intent.putExtra(SearchManager.QUERY, search.text)
            return makeClickableSpan(search.text, intent)
        }

        private fun decorateMention(mention: Mention): Spanned?{
            val intent = Intent(textView.context, UserDetailActivity::class.java)
            intent.putExtra(UserDetailActivity.EXTRA_USER_NAME, mention.text)
            return makeClickableSpan(mention.text, intent)
        }

        private fun decorateLink(link: Link): Spanned?{
            return makeClickableSpan(link.text, Intent(Intent.ACTION_VIEW, Uri.parse(link.url)))
        }


        private fun decorateHashTag(hashTag: HashTag): Spanned?{
            val intent = Intent(textView.context, SearchResultActivity::class.java)
            intent.putExtra(SearchResultActivity.EXTRA_SEARCH_WORLD, hashTag.text)
            return makeClickableSpan(hashTag.text, intent)
        }

        private fun makeClickableSpan(text: String, intent: Intent): SpannableString{
            val spanned = SpannableString(text)
            spanned.setSpan(
                object : ClickableSpan(){
                    override fun onClick(p0: View) {
                        textView.context.startActivity(intent)
                    }
                },0, text.length, 0
            )
            return spanned
        }



    }
}