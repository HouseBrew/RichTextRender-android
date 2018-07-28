package com.housebrew.library

import android.text.SpannableStringBuilder

fun SpannableStringBuilder.setSpanExc(span: Any, startIndex: Int, endIndex: Int) = this.setSpan(span, startIndex, endIndex, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)