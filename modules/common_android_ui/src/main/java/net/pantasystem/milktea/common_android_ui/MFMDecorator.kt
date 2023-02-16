package net.pantasystem.milktea.common_android_ui

import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.text.*
import android.text.style.*
import android.util.Log
import android.view.View
import android.widget.TextView
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.internal.managers.FragmentComponentManager
import jp.panta.misskeyandroidclient.mfm.*
import net.pantasystem.milktea.common.glide.GlideApp
import net.pantasystem.milktea.common_android.mfm.*
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
        skipEmojis: SkipEmojiHolder = SkipEmojiHolder(),
        retryCounter: Int = 0,
    ): Spanned? {
        lazyDecorateResult ?: return null
        val emojiAdapter = EmojiAdapter(textView)
        textView.setTag(R.id.TEXT_VIEW_MFM_TAG_ID, lazyDecorateResult.sourceText)

        return LazyEmojiDecorator(
            WeakReference(textView),
            lazyDecorateResult,
            skipEmojis,
            emojiAdapter,
            retryCounter,
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
//        private val emojiAdapter: EmojiAdapter,
//        private val skipEmojis: SkipEmojiHolder,
//        private val retryCounter: Int,
    ) {
        private var position = 0
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
                        lazyDecorateSkipElementsHolder
                    ).decorate()
                        ?: closeErrorElement(ele)
                    spannableStringBuilder.append(spanned)
                    position += spanned.length - 1
                }
            }

            // parentを装飾してしまう
            return try {
                // 自己がLeafであればparentの情報から装飾され、自己がNodeであればstartとpositionをもとに装飾される
                decorate(parent)
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

            val emojiSpan = DrawableEmojiSpan(null)
            spanned.setSpan(emojiSpan, 0, emojiElement.text.length, 0)
            lazyDecorateSkipElementsHolder.add(spanned, emojiElement, emojiSpan)
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
        val textView: WeakReference<TextView>,
        val lazyDecorateResult: LazyDecorateResult,
        val skipEmojis: SkipEmojiHolder,
        val emojiAdapter: EmojiAdapter,
        val retryCounter: Int,
    ) {

        fun decorate(): Spanned {
            val spanned = lazyDecorateResult.spanned
            lazyDecorateResult.skippedEmojis.forEach {
                decorateEmoji(it)
            }
            return spanned
        }

        private fun decorateEmoji(skippedEmoji: SkippedEmoji) {
            val emojiElement = skippedEmoji.emoji
//            if (skipEmojis.contains(emojiElement.emoji)) {
//                return
//            }
            textView.get()?.let { textView ->
                //val emojiSpan = EmojiSpan(textView)
                val emojiSpan = skippedEmoji.emojiSpan
                emojiSpan.adapter = emojiAdapter
                GlideApp.with(textView)
                    .load(emojiElement.emoji.url)
                    .override(max(textView.textSize.toInt(), 10))
//                    .addListener(object : RequestListener<Drawable> {
//                        override fun onLoadFailed(
//                            e: GlideException?,
//                            model: Any?,
//                            target: Target<Drawable>?,
//                            isFirstResource: Boolean
//                        ): Boolean {
//                            val t = this@LazyEmojiDecorator.textView.get()
//                            if (t != null && !skipEmojis.contains(emojiElement.emoji) && t.getTag(R.id.TEXT_VIEW_MFM_TAG_ID) == lazyDecorateResult.sourceText) {
//                                if (retryCounter < 100) {
//
//                                    decorate(
//                                        t,
//                                        lazyDecorateResult = lazyDecorateResult,
//                                        skipEmojis = skipEmojis.add(emojiElement.emoji),
//                                        retryCounter + 1
//                                    )
//                                }
//                            }
//
//                            return false
//                        }
//
//                        override fun onResourceReady(
//                            resource: Drawable?,
//                            model: Any?,
//                            target: Target<Drawable>?,
//                            dataSource: DataSource?,
//                            isFirstResource: Boolean
//                        ): Boolean {
//                            return false
//                        }
//                    })
                    .into(emojiSpan.generateTarget())
            }
        }
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

data class LazyDecorateResult(
    val sourceText: String,
    val spanned: Spanned,
    val skippedEmojis: List<SkippedEmoji>,
)

data class SkippedEmoji(
    val spanned: Spanned,
    val emoji: EmojiElement,
    val emojiSpan: DrawableEmojiSpan,
)

class LazyDecorateSkipElementsHolder {
    var skipped = mutableListOf<SkippedEmoji>()
    fun add(spanned: Spanned, element: EmojiElement, emojiSpan: DrawableEmojiSpan) {
        skipped.add(SkippedEmoji(spanned, element, emojiSpan))
    }
}