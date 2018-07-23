package com.housebrew.library

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import org.htmlcleaner.ContentNode
import org.htmlcleaner.HtmlCleaner
import org.htmlcleaner.TagNode

object HtmlTagHandler {

    private const val htmlString: String = "<p>Atlanta's <strong>leaders <i>faced</i></strong> an urgent decision that would impact every citizen of their city. An \"extreme weather\" event was bearing down on them, and they needed a plan of action.</p>"

    private val cleaner by lazy { HtmlCleaner() }

    private val SUPPORTED_TAG = setOf(
        "p", "ul", "li", "ol", "sup", "sub", "b", "i",
        "strike", "strong", "em", "br", "h1", "h2", "h3",
        "h4", "h5", "h6", "big", "small", "a", "font"
    )

    fun htmlToSpannable(contentType: ContentType = ContentType.TEXT, htmlText: String = htmlString): SpannableStringBuilder {
        val spannableStrBuilder = SpannableStringBuilder()
        when (contentType) {
            ContentType.TEXT,
            ContentType.REFERENCES,
            ContentType.NEWLINE,
            ContentType.LIST -> handleHtmlContent(spannableStrBuilder, htmlText)
            else -> throw IllegalArgumentException()
        }

        return spannableStrBuilder
    }

    private fun handleHtmlContent(spannableStrBuilder: SpannableStringBuilder, htmlText: String): SpannableStringBuilder {
        val node = cleaner.clean(htmlText)
        loopInnerTags(spannableStrBuilder, node)
        return spannableStrBuilder
    }

    private fun loopInnerTags(builder: SpannableStringBuilder, node: TagNode) {
        println(node.name)
        for (childNode in node.allChildren) {
            when (childNode) {
                is ContentNode -> {
                    val startIndex = builder.length
                    builder.append(childNode.content)
                    val endIndex = builder.length

                    when (node.name) {
                        "b", "strong" -> builder.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        "i" -> builder.setSpan(StyleSpan(Typeface.ITALIC), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        else -> builder.setSpan(StrikethroughSpan(), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                is TagNode -> loopInnerTags(builder, childNode)
            }
        }
    }
}