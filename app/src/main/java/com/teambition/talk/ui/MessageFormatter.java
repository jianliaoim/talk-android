package com.teambition.talk.ui;

import android.content.res.Resources;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;

import com.teambition.talk.BizLogic;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.client.data.InfoType;
import com.teambition.talk.ui.span.ActionSpan;
import com.teambition.talk.ui.span.HighlightSpan;
import com.teambition.talk.ui.span.ImageTagSpan;
import com.teambition.talk.ui.span.QuoteSpan;
import com.teambition.talk.ui.span.TalkURLSpan;
import com.teambition.talk.util.DateUtil;
import com.teambition.talk.util.EmojiUtil;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.ThemeUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zeatual on 15/7/23.
 */
public class MessageFormatter {

    private static final long MIN = 60 * 1000;
    private static final long HOUR = 60 * 60 * 1000;
    private static final long DAY = 60 * 60 * 1000 * 24;
    private static final String PATTERN = "<\\$(.+?)\\|(.+?)\\|(.*?)\\$>";
    private static final Pattern pattern = Pattern.compile(PATTERN);
    private static final Pattern chinesePattern = Pattern.compile("[\\u4e00-\\u9fa5]");

    public static Spannable formatToSpannable(String message) {
        return formatToSpannable(message, null);
    }

    public static Spannable formatToSpannable(String message, String name) {
        Spannable result;
        if (StringUtil.isNotBlank(message)) {
            // replace emoji
            message = EmojiUtil.replaceCheatSheetEmojis(message);
            // replace system information
            if (name != null) {
                for (InfoType info : InfoType.values()) {
                    if (message.contains(info.toString())) {
                        message = message.replace(info.toString(), StringUtil.getFormatText(info, name));
                    }
                }
            }
            // replace DSL
            result = formatActionSpan(message);
            // highlight url
            result = formatURLSpan(result);
            return result;
        } else {
            return new SpannableStringBuilder("");
        }
    }

    public static List<String> getMentions(String s) {
        if (s == null) {
            return Collections.EMPTY_LIST;
        }
        List<String> mentions = new ArrayList<>();
        Matcher matcher = pattern.matcher(s);
        while (matcher.find()) {
            if ("at".equals(matcher.group(1))) {
                mentions.add(matcher.group(2));
            }
        }
        return mentions;
    }

    public static Spannable formatActionSpan(String s) {
        if (s == null) {
            return new SpannableStringBuilder("");
        }
        Spannable result;
        Matcher matcher = pattern.matcher(s);
        List<Integer> positions = new ArrayList<>();
        List<ActionSpan> spans = new ArrayList<>();
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            ActionSpan actionSpan = new ActionSpan(matcher.group(), matcher.group(1),
                    matcher.group(2), matcher.group(3));
            positions.add(start);
            spans.add(actionSpan);
            s = s.substring(0, start) + "～ " + s.substring(end);
            matcher = pattern.matcher(s);
        }
        result = new SpannableStringBuilder(s);
        for (int i = 0; i < positions.size(); i++) {
            result.setSpan(spans.get(i), positions.get(i), positions.get(i) + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return result;
    }

    public static Spannable formatURLSpan(Spannable s) {
        Linkify.addLinks(s, Linkify.WEB_URLS);
        URLSpan[] urlSpans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan urlSpan : urlSpans) {
            final String url = urlSpan.getURL();
            final Matcher m = chinesePattern.matcher(url);
            if (m.find()) {
                s.removeSpan(urlSpan);
                continue;
            }
            int start = s.getSpanStart(urlSpan);
            int end = s.getSpanEnd(urlSpan);
            s.removeSpan(urlSpan);
            s.setSpan(new TalkURLSpan(urlSpan.getURL(), ThemeUtil.getThemeColor(MainApp.CONTEXT.
                            getResources(), BizLogic.getTeamColor())), start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return s;
    }

    public static Spannable formatURLSpan(String str) {
        Spannable s = new SpannableStringBuilder(str);
        Linkify.addLinks(s, Linkify.WEB_URLS);
        URLSpan[] urlSpans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan urlSpan : urlSpans) {
            int start = s.getSpanStart(urlSpan);
            int end = s.getSpanEnd(urlSpan);
            s.removeSpan(urlSpan);
            s.setSpan(new TalkURLSpan(urlSpan.getURL(), ThemeUtil.getThemeColor(MainApp.CONTEXT.
                            getResources(), BizLogic.getTeamColor())), start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return s;
    }

    public static Spannable formatQuoteSpan(String str) {
        Spannable s = new SpannableStringBuilder(str);
        s.setSpan(new QuoteSpan(), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return s;
    }

    public static Spannable formatFromHtml(Spanned s) {
        Spannable result = new SpannableStringBuilder(s);

        // replace URLSpan
        URLSpan[] urlSpans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan urlSpan : urlSpans) {
            int start = s.getSpanStart(urlSpan);
            int end = s.getSpanEnd(urlSpan);
            result.removeSpan(urlSpan);
            result.setSpan(new TalkURLSpan(urlSpan.getURL(), ThemeUtil.getThemeColor(MainApp.CONTEXT.
                            getResources(), BizLogic.getTeamColor())), start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // replace ImageSpan
        ImageSpan[] imgSpans = s.getSpans(0, s.length(), ImageSpan.class);
        for (ImageSpan imgSpan : imgSpans) {
            int start = s.getSpanStart(imgSpan);
            int end = s.getSpanEnd(imgSpan);
            result.removeSpan(imgSpan);
            result.setSpan(new ImageTagSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return result;
    }

    public static Spannable formatHighlightSpan(String html, Resources res) {
        HighlightSpan highlightSpan;
        if (Html.fromHtml(html) instanceof SpannableStringBuilder) {
            SpannableStringBuilder value = (SpannableStringBuilder) Html.fromHtml(html);
            StyleSpan[] spans = value.getSpans(0, html.length(), StyleSpan.class);
            for (StyleSpan span : spans) {
                int start = value.getSpanStart(span);
                int end = value.getSpanEnd(span);
                value.removeSpan(span);
                highlightSpan = new HighlightSpan(res);
                value.setSpan(highlightSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return value;
        } else {
            return new SpannableStringBuilder(html);
        }
    }

    public static String formatToPureText(String message) {
        return formatToPureText(message, null);
    }

    public static String formatToPureText(String message, String name) {
        if (message == null) {
            return "";
        }
        // replace emoji
        message = EmojiUtil.replaceCheatSheetEmojis(message);
        // replace system information
        if (name != null) {
            for (InfoType info : InfoType.values()) {
                if (message.contains(info.toString())) {
                    message = message.replace(info.toString(), StringUtil.getFormatText(info, name));
                }
            }
        }
        // replace DSL
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            message = message.substring(0, start) + matcher.group(3) + message.substring(end);
            matcher = pattern.matcher(message);
        }
        return message;
    }

    /**
     * 将待发送文本格式化为纯文本
     *
     * @param s 待发送文本
     * @return 包含DSL的数据字符串
     */
    public static String formatToPost(Spannable s) {
        if (s == null) {
            return "";
        }
        String result;
        // replace ActionSpan with DSL
        ActionSpan[] spans = s.getSpans(0, s.length(), ActionSpan.class);
        if (spans != null && spans.length > 0) {
            List<Integer> positions = new ArrayList<>();
            List<String> data = new ArrayList<>();
            for (ActionSpan span : spans) {
                positions.add(s.getSpanStart(span));
                data.add(span.getSource());
            }
            result = s.toString().substring(0, positions.get(0));
            for (int i = 0; i < positions.size(); i++) {
                result += data.get(i) + s.toString().substring(positions.get(i) + 1,
                        i == positions.size() - 1 ? s.toString().length() :
                                positions.get(i + 1));
            }
        } else {
            result = s.toString();
        }
        //replace emoji
        result = EmojiUtil.replaceUnicodeEmojis(result);
        return result;
    }

    /**
     * 从html中取出图片url
     *
     * @param str html
     * @return image url
     */
    public static String filterImage(String str) {
        if (str != null) {
            Pattern pattern = Pattern.compile("<img.*?[\'\"](http(s)?://[\\x21-\\x7F*]+)[\'\"].*?>");
            Matcher matcher = pattern.matcher(str);
            StringBuilder sb = new StringBuilder();
            if (matcher.find()) {
                if (StringUtil.isNotBlank(matcher.group(1))) {
                    sb.append(matcher.group(1));
                }
            }
            return sb.toString().trim();
        } else {
            return "";
        }
    }

    /**
     * 过滤html标签
     *
     * @param str html
     * @return 无样式纯文本
     */
    public static String filterHtml(String str) {
        if (StringUtil.isNotBlank(str)) {
            Pattern pattern = Pattern.compile("<([^>]*)>");
            Matcher matcher = pattern.matcher(str);
            StringBuffer sb = new StringBuffer();
            boolean result1 = matcher.find();
            while (result1) {
                matcher.appendReplacement(sb, "");
                result1 = matcher.find();
            }
            matcher.appendTail(sb);
            return sb.toString().trim();
        } else {
            return str;
        }
    }

    /**
     * calculate the offset from now to created time
     *
     * @param createAt
     * @return offset time
     */
    public static String formatCreateTime(Date createAt) {
        String strTime = "";
        long beginOfToday = (new Date(Calendar.getInstance().get(Calendar.YEAR) - 1900,
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                0, 0, 0)).getTime();
        long beginOfThisYear = (new Date(Calendar.getInstance().get(Calendar.YEAR) - 1900,
                0, 0, 0, 0, 0)).getTime();
        if (createAt != null) {
            long interval = System.currentTimeMillis() - createAt.getTime();
            if (interval <= MIN) {
                strTime = MainApp.CONTEXT.getString(R.string.just_now);
            } else if (createAt.getTime() >= beginOfToday) {
                strTime = DateUtil.formatDate(createAt, MainApp.CONTEXT.
                        getString(R.string.date_format));
            } else if (createAt.getTime() < beginOfToday && createAt.getTime() >= beginOfThisYear) {
                strTime = DateUtil.formatDate(createAt, MainApp.CONTEXT.
                        getString(R.string.date_format_with_date));
            } else {
                strTime = DateUtil.formatDate(createAt, MainApp.CONTEXT.
                        getString(R.string.date_format_with_year));
            }
        }
        return strTime;
    }

    public static String formatCreateTimeForShort(Date createAt) {
        String strTime = "";
        long beginOfToday = (new Date(Calendar.getInstance().get(Calendar.YEAR) - 1900,
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                0, 0, 0)).getTime();
        long beginOfThisYear = (new Date(Calendar.getInstance().get(Calendar.YEAR) - 1900,
                0, 0, 0, 0, 0)).getTime();
        if (createAt != null) {
            long interval = System.currentTimeMillis() - createAt.getTime();
            if (interval <= MIN) {
                strTime = MainApp.CONTEXT.getString(R.string.just_now);
            } else if (createAt.getTime() >= beginOfToday) {
                strTime = DateUtil.formatDate(createAt, MainApp.CONTEXT.
                        getString(R.string.date_format));
            } else if (createAt.getTime() < beginOfToday && createAt.getTime() >= beginOfThisYear) {
                strTime = DateUtil.formatDate(createAt, MainApp.CONTEXT.
                        getString(R.string.date_format_with_date));
            } else {
                strTime = DateUtil.formatDate(createAt, MainApp.CONTEXT.
                        getString(R.string.date_format_only_date));
            }
        }
        return strTime;
    }

    public static String formatNotification(String message, String name) {
        if (StringUtil.isNotBlank(message)) {
            boolean hasSystemInfo = false;
            // replace emoji
            message = EmojiUtil.replaceCheatSheetEmojis(message);
            // replace system information
            if (name != null) {
                for (InfoType info : InfoType.values()) {
                    if (message.contains(info.toString())) {
                        message = message.replace(info.toString(), StringUtil.getFormatText(info, name));
                        hasSystemInfo = true;
                    }
                }
            }
            // replace DSL
            Matcher matcher = pattern.matcher(message);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                message = message.substring(0, start) + matcher.group(3) + message.substring(end);
                matcher = pattern.matcher(message);
            }
            message = (hasSystemInfo ? "" : (name + ": ")) + message;
        }
        return message;
    }

}
