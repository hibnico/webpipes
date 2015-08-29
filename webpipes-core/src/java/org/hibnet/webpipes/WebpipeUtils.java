/*
 *  Copyright 2015 WebPipes contributors
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hibnet.webpipes;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.hibnet.jsourcemap.SourceMap;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebpipeUtils {

    public static final Charset UTF8 = Charset.forName("UTF-8");

    private static final ObjectMapper SOURCEMAP_JSON_MAPPER = new ObjectMapper();

    static {
        SOURCEMAP_JSON_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SOURCEMAP_JSON_MAPPER.getSerializationConfig().withSerializationInclusion(Include.NON_NULL);
    }

    public static MessageDigest buildSHA1Digest() {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 is not supported", e);
        }
        return digest;
    }

    public static byte[] sha1(String content) {
        return buildSHA1Digest().digest(content.getBytes(UTF8));
    }

    public static String sha1HexEncoded(String content) {
        return hexEncode(sha1(content));
    }

    private static final char[] ALPHABET = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    public static String hexEncode(byte[] data) {
        StringBuilder buffer = new StringBuilder(16);
        for (int i = 0; i < data.length; i++) {
            int highBits = (data[i] & 0xF0) >> 4;
            int lowBits = data[i] & 0x0F;
            buffer.append(ALPHABET[highBits]);
            buffer.append(ALPHABET[lowBits]);
        }
        return buffer.toString();
    }

    /**
     * Tokenize the given String into a String array via a StringTokenizer.
     * <p>
     * The given delimiters string is supposed to consist of any number of delimiter characters. Each of those characters can be used to separate
     * tokens. A delimiter is always a single character.
     * 
     * @param str
     *            the String to tokenize
     * @param delimiters
     *            the delimiter characters, assembled as String (each of those characters is individually considered as delimiter)
     * @param trimTokens
     *            trim the tokens via String's {@code trim}
     * @param ignoreEmptyTokens
     *            omit empty tokens from the result array (only applies to tokens that are empty after trimming; StringTokenizer will not consider
     *            subsequent delimiters as token in the first place).
     * @return an array of the tokens ({@code null} if the input String was {@code null})
     * @see java.util.StringTokenizer
     * @see String#trim()
     */
    public static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {

        if (str == null) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(str, delimiters);
        List<String> tokens = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (trimTokens) {
                token = token.trim();
            }
            if (!ignoreEmptyTokens || token.length() > 0) {
                tokens.add(token);
            }
        }
        return toStringArray(tokens);
    }

    /**
     * Copy the given Collection into a String array. The Collection must contain String elements only.
     * 
     * @param collection
     *            the Collection to copy
     * @return the String array ({@code null} if the passed-in Collection was {@code null})
     */
    public static String[] toStringArray(Collection<String> collection) {
        if (collection == null) {
            return null;
        }
        return collection.toArray(new String[collection.size()]);
    }

    /**
     * Check whether the given CharSequence has actual text. More specifically, returns {@code true} if the string not {@code null}, its length is
     * greater than 0, and it contains at least one non-whitespace character.
     * <p>
     * 
     * <pre class="code">
     * StringUtils.hasText(null) = false
     * StringUtils.hasText("") = false
     * StringUtils.hasText(" ") = false
     * StringUtils.hasText("12345") = true
     * StringUtils.hasText(" 12345 ") = true
     * </pre>
     * 
     * @param str
     *            the CharSequence to check (may be {@code null})
     * @return {@code true} if the CharSequence is not {@code null}, its length is greater than 0, and it does not contain whitespace only
     * @see Character#isWhitespace
     */
    public static boolean hasText(CharSequence str) {
        if (!hasLength(str)) {
            return false;
        }
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check that the given CharSequence is neither {@code null} nor of length 0. Note: Will return {@code true} for a CharSequence that purely
     * consists of whitespace.
     * <p>
     * 
     * <pre class="code">
     * StringUtils.hasLength(null) = false
     * StringUtils.hasLength("") = false
     * StringUtils.hasLength(" ") = true
     * StringUtils.hasLength("Hello") = true
     * </pre>
     * 
     * @param str
     *            the CharSequence to check (may be {@code null})
     * @return {@code true} if the CharSequence is not null and has length
     * @see #hasText(CharSequence)
     */
    public static boolean hasLength(CharSequence str) {
        return (str != null && str.length() > 0);
    }

    /**
     * Count the occurrences of the substring in string s.
     * 
     * @param str
     *            string to search in. Return 0 if this is null.
     * @param sub
     *            string to search for. Return 0 if this is null.
     */
    public static int countOccurrencesOf(String str, String sub) {
        if (str == null || sub == null || str.length() == 0 || sub.length() == 0) {
            return 0;
        }
        int count = 0;
        int pos = 0;
        int idx;
        while ((idx = str.indexOf(sub, pos)) != -1) {
            ++count;
            pos = idx + sub.length();
        }
        return count;
    }

    /**
     * Replace all occurrences of a substring within a string with another string.
     * 
     * @param inString
     *            String to examine
     * @param oldPattern
     *            String to replace
     * @param newPattern
     *            String to insert
     * @return a String with the replacements
     */
    public static String replace(String inString, String oldPattern, String newPattern) {
        if (!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
            return inString;
        }
        StringBuilder sb = new StringBuilder();
        int pos = 0; // our position in the old string
        int index = inString.indexOf(oldPattern);
        // the index of an occurrence we've found, or -1
        int patLen = oldPattern.length();
        while (index >= 0) {
            sb.append(inString.substring(pos, index));
            sb.append(newPattern);
            pos = index + patLen;
            index = inString.indexOf(oldPattern, pos);
        }
        sb.append(inString.substring(pos));
        // remember to append any characters to the right of a match
        return sb.toString();
    }

    public static File urlToFile(URL url) {
        try {
            return new File(url.toURI()).getAbsoluteFile();
        } catch (URISyntaxException e) {
            return new File(url.getPath()).getAbsoluteFile();
        }
    }

    public static String getPackageDir(Class<?> cl) {
        String packageDir = cl.getCanonicalName();
        packageDir = packageDir.substring(0, packageDir.lastIndexOf("."));
        packageDir = "/" + packageDir.replaceAll("\\.", "/");
        return packageDir;
    }

    public static SourceMap parseSourceMap(String json) throws JsonParseException, JsonMappingException, IOException {
        if (json == null) {
            return null;
        }
        return SOURCEMAP_JSON_MAPPER.readValue(json, SourceMap.class);
    }

    public static SourceMap parseSourceMap(byte[] json) throws JsonParseException, JsonMappingException, IOException {
        if (json == null) {
            return null;
        }
        return SOURCEMAP_JSON_MAPPER.readValue(json, SourceMap.class);
    }

    public static void serializeSourceMap(SourceMap sourceMap, OutputStream out) throws JsonGenerationException, JsonMappingException, IOException {
        SOURCEMAP_JSON_MAPPER.writeValue(out, sourceMap);
    }

    public static void appendSourceMap(SourceMap sourceMap, final StringBuilder builder)
            throws JsonGenerationException, JsonMappingException, IOException {
        SOURCEMAP_JSON_MAPPER.writeValue(new Writer() {

            @Override
            public Writer append(char value) {
                builder.append(value);
                return this;
            }

            @Override
            public Writer append(CharSequence value) {
                builder.append(value);
                return this;
            }

            @Override
            public Writer append(CharSequence value, int start, int end) {
                builder.append(value, start, end);
                return this;
            }

            @Override
            public void close() {
            }

            @Override
            public void flush() {
            }

            @Override
            public void write(String value) {
                if (value != null) {
                    builder.append(value);
                }
            }

            @Override
            public void write(char[] value, int offset, int length) {
                if (value != null) {
                    builder.append(value, offset, length);
                }
            }

        }, sourceMap);
    }

    public static String addSuffix(String name, String suffix) {
        int i = name.lastIndexOf(".");
        if (i == -1) {
            return name + suffix;
        } else {
            return name.substring(0, i) + suffix + name.substring(i);
        }
    }

    public static String getDotExtension(String name) {
        int i = name.lastIndexOf(".");
        if (i == -1) {
            return "";
        } else {
            return name.substring(i);
        }
    }
}
