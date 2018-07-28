package com.housebrew.library

import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import org.htmlcleaner.ContentNode
import org.htmlcleaner.HtmlCleaner
import org.htmlcleaner.TagNode
import java.util.Stack

object HtmlTagHandler {

    val typeface: Typeface? = null

    private const val htmlString: String = "<p>Atlanta's <strike><strong>leaders <br><i>faced</i></strong></strike> an urgent decision that would impact every citizen of their city. An \"extreme weather\" event was bearing down on them, and they needed a plan of action.</p>"

    private val cleaner by lazy { HtmlCleaner() }

    private val SUPPORTED_TAG = setOf(
        "html", "body",
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

    private fun loopInnerTags(builder: SpannableStringBuilder, node: TagNode, attrStack: Stack<String> = Stack()) {
        // Exception case, no span needed
        when (node.name) {
            !in SUPPORTED_TAG -> return
            "br" -> {
                builder.append("\n")
                return
            }
        }

        attrStack.push(node.name)
        for (childNode in node.allChildren) {
            when (childNode) {
                is ContentNode -> {
                    val startIndex = builder.length
                    builder.append(childNode.content)
                    val endIndex = builder.length
                    for (attr in attrStack) {
                        when (attr) {
                            "b", "strong" -> builder.setSpanExc(StyleSpan(Typeface.BOLD), startIndex, endIndex)
                            "i" -> {
                                builder.setSpanExc(StyleSpan(Typeface.ITALIC), startIndex, endIndex)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    builder.setSpanExc(TypefaceSpan(Typeface.MONOSPACE), startIndex, endIndex)
                                } else {
                                    builder.setSpanExc(TypefaceSpan("monospace"), startIndex, endIndex)
                                }
                            }
                            "strike" -> builder.setSpanExc(StrikethroughSpan(), startIndex, endIndex)
                            "br" -> builder.append("\n")
                        }
                    }
                }
                is TagNode -> {
                    loopInnerTags(builder, childNode, attrStack)
                }
            }
        }
        attrStack.pop()
    }

    private fun setSpan(builder: SpannableStringBuilder, startIndex: Int, endIndex: Int, span: Any) {
        builder.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}