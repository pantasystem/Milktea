package net.pantasystem.milktea.common_android_ui

import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.SpannedString
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.QuoteSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.internal.managers.FragmentComponentManager
import jp.panta.misskeyandroidclient.mfm.EmojiElement
import jp.panta.misskeyandroidclient.mfm.HashTag
import jp.panta.misskeyandroidclient.mfm.Mention
import jp.panta.misskeyandroidclient.mfm.Node
import jp.panta.misskeyandroidclient.mfm.Search
import jp.panta.misskeyandroidclient.mfm.Text
import net.pantasystem.milktea.common.glide.GlideApp
import net.pantasystem.milktea.common_android.mfm.Element
import net.pantasystem.milktea.common_android.mfm.ElementType
import net.pantasystem.milktea.common_android.mfm.Leaf
import net.pantasystem.milktea.common_android.mfm.Link
import net.pantasystem.milktea.common_android.mfm.Root
import net.pantasystem.milktea.common_android.ui.Activities
import net.pantasystem.milktea.common_android.ui.putActivity
import net.pantasystem.milktea.common_android.ui.text.DrawableEmojiSpan
import net.pantasystem.milktea.common_android.ui.text.EmojiAdapter
import net.pantasystem.milktea.common_navigation.SearchNavType
import net.pantasystem.milktea.common_navigation.UserDetailNavigationArgs
import net.pantasystem.milktea.model.emoji.Emoji
import java.lang.ref.WeakReference
import kotlin.math.max

object MFMDecorator {




    fun decorate(
        textView: TextView,
        lazyDecorateResult: LazyDecorateResult?,
        customEmojiScale: Float = 1f,
        skipEmojis: SkipEmojiHolder = SkipEmojiHolder(),
    ): Spanned? {
        lazyDecorateResult ?: return null
        val emojiAdapter = EmojiAdapter(textView)
        textView.setTag(R.id.TEXT_VIEW_MFM_TAG_ID, lazyDecorateResult.sourceText)

        return LazyEmojiDecorator(
            WeakReference(textView),
            lazyDecorateResult,
            skipEmojis,
            emojiAdapter,
            customEmojiScale,
        ).decorate()
    }

    fun decorate(
        node: Root?,
        lazyDecorateSkipElementsHolder: LazyDecorateSkipElementsHolder
    ): LazyDecorateResult? {
        node ?: return null
        val spanned = Visitor(
            node,
            node,
            lazyDecorateSkipElementsHolder,
            0,
        ).decorate()
        return LazyDecorateResult(
            node.sourceText,
            spanned ?: SpannableString(node.sourceText),
            if (spanned == null) emptyList() else lazyDecorateSkipElementsHolder.skipped.toList()
        )
    }
    /**
     * VisitorパターンのVisitorとは少し違う
     */
    class Visitor(
//        val textView: WeakReference<TextView>,
        val root: Root,
        val parent: Element,
        val lazyDecorateSkipElementsHolder: LazyDecorateSkipElementsHolder,
        val start: Int,
//        private val emojiAdapter: EmojiAdapter,
//        private val skipEmojis: SkipEmojiHolder,
//        private val retryCounter: Int,
    ) {
        private var position = start
        private val spannableStringBuilder = SpannableStringBuilder()


        /**
         * parent のchildElementsをここで処理する
         * @return 担当した要素（Element）のspannableStringBuilderの最終的なポジション
         */
        fun decorate(): Spanned? {
            if (parent is Node) {
                parent.childElements.forEach { ele ->
                    val spanned = Visitor(
                        root,
                        ele,
                        lazyDecorateSkipElementsHolder,
                        position,
                    ).decorate()
                        ?: closeErrorElement(ele)
                    spannableStringBuilder.append(spanned)
                    position = start + spannableStringBuilder.length
                }
            }

            // parentを装飾してしまう
            return try {
                // 自己がLeafであればparentの情報から装飾され、自己がNodeであればstartとpositionをもとに装飾される
                val result = decorate(parent)
                position = start + spannableStringBuilder.length
                result
            } catch (e: Exception) {
                // 子要素の処理がすべて無駄になることになってしまうがとりあえずはこうする
                null
            }

            // Leafの処理が終わったら自分(parent)のDecorateを開始する
        }


        private fun closeErrorElement(element: Element): Spanned {
            return SpannableString(root.sourceText.substring(element.start, element.end))
        }

        private fun decorate(element: Element): Spanned? {
            return when (element) {
                is Leaf ->
                    when (element) {
                        is Text -> decorateText(element)
                        is EmojiElement -> decorateEmoji(element)
                        is Search -> decorateSearch(element)
                        is Mention -> decorateMention(element)
                        is Link -> decorateLink(element)
                        is HashTag -> decorateHashTag(element)
                        else -> null
                    }
                is Node -> {
                    decorateNode()
                    spannableStringBuilder
                }
                else -> null
            }

        }

//        private fun decorateEmoji(emojiElement: EmojiElement): Spanned {
//            val spanned = SpannableString(emojiElement.text)
//            lazyDecorateSkipElementsHolder.add(spanned, emojiElement)
//            return spanned
//        }

        private fun decorateEmoji(emojiElement: EmojiElement): Spanned {
            val spanned = SpannableString(emojiElement.text)
//            if (skipEmojis.contains(emojiElement.emoji)) {
//                return spanned
//            }

            lazyDecorateSkipElementsHolder.add(SkippedEmoji(spanned, emojiElement, position, position + spanned.length))
            return spanned
        }

        private fun decorateText(text: Text): Spanned {
            return SpannedString(text.text)
        }

        private fun decorateSearch(search: Search): Spanned {
            val intent = Intent(Intent.ACTION_SEARCH)
            //intent.setClassName("com.google.android.googlequicksearchbox",  "com.google.android.googlequicksearchbox.SearchActivity")
            intent.putExtra(SearchManager.QUERY, search.text)
            return makeClickableSpan(
                "${search.text} Search",
            ) {
                Intent(Intent.ACTION_SEARCH).apply {
                    putExtra(SearchManager.QUERY, search.text)
                }
            }
        }

        private fun decorateMention(mention: Mention): Spanned {
            return makeClickableSpan(mention.text) {
                val activity = FragmentComponentManager.findActivity(it.context) as Activity
                val intent = EntryPointAccessors.fromActivity(
                    activity,
                    NavigationEntryPointForBinding::class.java
                )
                    .userDetailNavigation()
                    .newIntent(UserDetailNavigationArgs.UserName(userName = mention.text))
                intent.putActivity(Activities.ACTIVITY_IN_APP)

                intent
            }

        }

        private fun decorateLink(link: Link): Spanned {
            return makeClickableSpan(link.text) {
                Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
            }
        }


        private fun decorateHashTag(hashTag: HashTag): Spanned {
            return makeClickableSpan(hashTag.text) {
                val activity = FragmentComponentManager.findActivity(it.context) as Activity

                val navigation = EntryPointAccessors.fromActivity(
                    activity,
                    NavigationEntryPointForBinding::class.java
                )
                navigation.searchNavigation()
                    .newIntent(SearchNavType.ResultScreen(hashTag.text))
            }

        }

        private fun makeClickableSpan(text: String, makeIntent: (View) -> Intent): SpannableString {
            val spanned = SpannableString(text)
            spanned.setSpan(
                object : ClickableSpan() {
                    override fun onClick(p0: View) {
                        p0.context?.startActivity(makeIntent(p0))

                    }
                }, 0, text.length, 0
            )
            return spanned
        }

        /**
         * 最後に一度だけ実行すること
         */
        private fun decorateNode() {
            fun setSpan(any: Any) {
                spannableStringBuilder.setSpan(any, 0, spannableStringBuilder.length, 0)
            }

            if (parent is Node) {
                when (parent.elementType) {
                    ElementType.QUOTE -> {
                        setSpan(QuoteSpan())
                    }
                    ElementType.BOLD -> {
                        setSpan(StyleSpan(Typeface.BOLD))
                    }
                    ElementType.ITALIC -> {
                        setSpan(StyleSpan(Typeface.ITALIC))
                    }
                    ElementType.CENTER -> {
                        setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER))
                    }
                    ElementType.STRIKE -> {
                        setSpan(StrikethroughSpan())
                    }
                    ElementType.CODE -> {
                        setSpan(BackgroundColorSpan(Color.parseColor("#000000")))
                        setSpan(ForegroundColorSpan(Color.WHITE))
                    }
                    ElementType.TITLE -> {
                        spannableStringBuilder.append("\n")
                        setSpan(RelativeSizeSpan(1.5F))
                        setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER))
                    }
                    ElementType.SMALL -> {
                        setSpan(RelativeSizeSpan(0.6F))
                    }
                    ElementType.FnX2 -> {
                        setSpan(RelativeSizeSpan(2.0F))
                    }
                    ElementType.FnX3 -> {
                        setSpan(RelativeSizeSpan(3.0F))
                    }
                    ElementType.FnX4 -> {
                        setSpan(RelativeSizeSpan(4.0F))
                    }
                    ElementType.ROOT -> {

                    }
                    else -> {
                        Log.d("MFMDecorator", "error:${parent.elementType}")
                    }
                }
            }

        }


    }

    class LazyEmojiDecorator(
        private val textView: WeakReference<TextView>,
        private val lazyDecorateResult: LazyDecorateResult,
        private val skipEmojis: SkipEmojiHolder,
        private val emojiAdapter: EmojiAdapter,
        private val customEmojiScale: Float,
    ) {

        private val spannableString = SpannableString(lazyDecorateResult.spanned)

        fun decorate(): Spanned {
            //val emojiSpan = EmojiSpan(textView)

            val spanned = spannableString
            lazyDecorateResult.skippedEmojis.forEach {
                decorateEmoji(it)
            }
            return spanned
        }

        private fun decorateEmoji(skippedEmoji: SkippedEmoji) {
            val emojiElement = skippedEmoji.emoji
            if (skipEmojis.contains(emojiElement.emoji)) {
                return
            }
            textView.get()?.let { textView ->
                val emojiSpan = DrawableEmojiSpan(emojiAdapter, emojiElement.emoji.url, emojiElement.emoji.aspectRatio)
                spannableString.setSpan(emojiSpan, skippedEmoji.start, skippedEmoji.end, 0)
                spannableString.setSpan(RelativeSizeSpan(customEmojiScale), skippedEmoji.start, skippedEmoji.end, 0)
                val height = max(textView.textSize * 0.75f, 10f)
                val width = when(val aspectRatio = emojiElement.emoji.aspectRatio) {
                    null -> height
                    else -> height * aspectRatio
                }
                GlideApp.with(textView)
                    .load(emojiElement.emoji.cachePath)
                    .error(
                        GlideApp.with(textView)
                            .load(emojiElement.emoji.url ?: emojiElement.emoji.uri)
                            .override((width * customEmojiScale).toInt(), (height * customEmojiScale).toInt())
                    )
                    .override((width * customEmojiScale).toInt(), (height * customEmojiScale).toInt())
                    .into(emojiSpan.target)
            }
        }
    }
}


data class LazyDecorateResult(
    val sourceText: String,
    val spanned: Spanned,
    val skippedEmojis: List<SkippedEmoji>,
)

data class SkippedEmoji(
    val spanned: Spanned,
    val emoji: EmojiElement,
    val start: Int,
    val end: Int,
)

class LazyDecorateSkipElementsHolder {
    var skipped = mutableListOf<SkippedEmoji>()
    fun add(skipped: SkippedEmoji) {
        this.skipped.add(skipped)
    }
}

class SkipEmojiHolder {
    private var skipEmojis = mutableSetOf<Emoji>()
    fun add(emoji: Emoji): SkipEmojiHolder {
        skipEmojis.add(emoji)
        return this
    }

    fun contains(emoji: Emoji): Boolean {
        return skipEmojis.contains(emoji)
    }
}