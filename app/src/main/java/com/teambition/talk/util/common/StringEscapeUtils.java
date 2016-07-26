package com.teambition.talk.util.common;

import com.teambition.talk.util.common.text.translate.AggregateTranslator;
import com.teambition.talk.util.common.text.translate.CharSequenceTranslator;
import com.teambition.talk.util.common.text.translate.EntityArrays;
import com.teambition.talk.util.common.text.translate.LookupTranslator;
import com.teambition.talk.util.common.text.translate.OctalUnescaper;
import com.teambition.talk.util.common.text.translate.UnicodeUnescaper;


/**
 * <p>Escapes and unescapes {@code String}s for
 * Java, Java Script, HTML and XML.</p>
 *
 * <p>#ThreadSafe#</p>
 * @since 2.0
 */
public class StringEscapeUtils {

    /**
     * Translator object for unescaping escaped Java. 
     * 
     * While {@link #unescapeJava(String)} is the expected method of use, this 
     * object allows the Java unescaping functionality to be used 
     * as the foundation for a custom translator. 
     *
     * @since 3.0
     */
    // TODO: throw "illegal character: \92" as an Exception if a \ on the end of the Java (as per the compiler)?
    public static final CharSequenceTranslator UNESCAPE_JAVA =
        new AggregateTranslator(
            new OctalUnescaper(),     // .between('\1', '\377'),
            new UnicodeUnescaper(),
            new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_UNESCAPE()),
            new LookupTranslator(
                      new String[][] { 
                            {"\\\\", "\\"},
                            {"\\\"", "\""},
                            {"\\'", "'"},
                            {"\\", ""}
                      })
        );

    /**
     * Translator object for unescaping escaped Json.
     *
     * While {@link #unescapeJson(String)} is the expected method of use, this
     * object allows the Json unescaping functionality to be used
     * as the foundation for a custom translator.
     *
     * @since 3.2
     */
    public static final CharSequenceTranslator UNESCAPE_JSON = UNESCAPE_JAVA;

    /* Helper functions */

    /**
     * <p>{@code StringEscapeUtils} instances should NOT be constructed in
     * standard programming.</p>
     *
     * <p>Instead, the class should be used as:</p>
     * <pre>StringEscapeUtils.escapeJava("foo");</pre>
     *
     * <p>This constructor is public to permit tools that require a JavaBean
     * instance to operate.</p>
     */
    public StringEscapeUtils() {
      super();
    }

    /**
     * <p>Unescapes any Java literals found in the {@code String}.
     * For example, it will turn a sequence of {@code '\'} and
     * {@code 'n'} into a newline character, unless the {@code '\'}
     * is preceded by another {@code '\'}.</p>
     * 
     * @param input  the {@code String} to unescape, may be null
     * @return a new unescaped {@code String}, {@code null} if null string input
     */
    public static final String unescapeJava(final String input) {
        return UNESCAPE_JAVA.translate(input);
    }

    /**
     * <p>Unescapes any Json literals found in the {@code String}.</p>
     *
     * <p>For example, it will turn a sequence of {@code '\'} and {@code 'n'}
     * into a newline character, unless the {@code '\'} is preceded by another
     * {@code '\'}.</p>
     *
     * @see #unescapeJava(String)
     * @param input  the {@code String} to unescape, may be null
     * @return A new unescaped {@code String}, {@code null} if null string input
     *
     * @since 3.2
     */
    public static final String unescapeJson(final String input) {
        return UNESCAPE_JSON.translate(input);
    }


}