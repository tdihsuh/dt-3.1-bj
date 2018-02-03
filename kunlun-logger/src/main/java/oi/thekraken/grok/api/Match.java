/*******************************************************************************
 * Copyright 2014 Anthony Corbacho and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package oi.thekraken.grok.api;


import com.google.code.regexp.Matcher;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * {@code Match} is a representation in {@code Grok} world of your log.
 *
 * @author anthonycorbacho
 * @since 0.0.1
 */
public class Match {

    private String subject; // texte
    private Map<String, Object> capture;

    private Grok grok;
    private Matcher match;
    private int start;
    private int end;

    /**
     * For thread safety
     */
    private static ThreadLocal<Match> matchHolder = new ThreadLocal<Match>() {
        @Override
        protected Match initialValue() {
            return new Match();
        }
    };

    /**
     * Create a new {@code Match} object.
     */
    public Match() {
        this(null);
    }

    public Match(Garbage garbage) {
        subject = "Nothing";
        grok = null;
        match = null;
        capture = new TreeMap<>();

        start = 0;
        end = 0;
    }

    /**
     * Create Empty grok matcher
     */
    public static final Match EMPTY = new Match();

    public void setGrok(Grok grok) {
        if (grok != null) {
            this.grok = grok;
        }
    }

    public Matcher getMatch() {
        return match;
    }

    public void setMatch(Matcher match) {
        this.match = match;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * Singleton.
     *
     * @return instance of Match
     */
    public static Match getInstance() {
        return matchHolder.get();
    }

    /**
     * Set the single line of log to parse.
     *
     * @param text : single line of log
     */
    public void setSubject(String text) {
        if (text == null) {
            return;
        }
        if (text.isEmpty()) {
            return;
        }
        subject = text;
    }

    /**
     * Retrurn the single line of log.
     *
     * @return the single line of log
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Match to the <tt>subject</tt> the <tt>regex</tt> and save the matched element into a map.
     */
    public void captures() {
        captures(false);
    }

    public void captures(boolean onlyKeys) {
        if (match == null) {
            return;
        }
        capture.clear();

        // _capture.put("LINE", this.line);
        // _capture.put("LENGTH", this.line.length() +"");

        Map<String, String> mapped = this.match.namedGroups();
        Iterator<Entry<String, String>> it = mapped.entrySet().iterator();
        while (it.hasNext()) {

            @SuppressWarnings("rawtypes")
            Entry<String, String> pairs = it.next();
            String key = pairs.getKey();
            String value = pairs.getValue();
            if (onlyKeys && !pairs.getKey().startsWith("field"))
                continue;
            String kn = this.grok.getNamedRegexCollectionById(pairs.getKey());
            if (kn != null && !kn.isEmpty()) {
                key = kn;
            }
            if (value != null) {
                if (this.isInteger(value)) {
                    capture.put(key, Integer.parseInt(value));
                } else {
                    capture.put(key, value);
                }
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
    }


    /**
     * remove from the string the quote and double quote.
     *
     * @param value to pure: "my/text"
     * @return unquoted string: my/text
     */
    private String cleanString(String value) {
        if (value == null) {
            return null;
        }
        if (value.isEmpty()) {
            return value;
        }
        char[] tmp = value.toCharArray();
        if ((tmp[0] == '"' && tmp[value.length() - 1] == '"')
                || (tmp[0] == '\'' && tmp[value.length() - 1] == '\'')) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }


    /**
     * Get the json representation of the matched element.
     * <p>
     * example:
     * map [ {IP: 127.0.0.1}, {status:200}]
     * will return
     * {"IP":"127.0.0.1", "status":200}
     * </p>
     * If pretty is set to true, json will return prettyprint json string.
     *
     * @return Json of the matched element in the text
     */
    public String toJson(Boolean pretty) {
        if (capture == null) {
            return "{}";
        }
        if (capture.isEmpty()) {
            return "{}";
        }

        this.cleanMap();
        Gson gs;
        if (pretty) {
            gs = new GsonBuilder().setPrettyPrinting().create();
        } else {
            gs = new Gson();
        }
        return gs.toJson(/* cleanMap( */capture/* ) */);
    }

    /**
     * Get the json representation of the matched element.
     * <p>
     * example:
     * map [ {IP: 127.0.0.1}, {status:200}]
     * will return
     * {"IP":"127.0.0.1", "status":200}
     * </p>
     *
     * @return Json of the matched element in the text
     */
    public String toJson() {
        return toJson(false);
    }

    /**
     * Get the map representation of the matched element in the text.
     *
     * @return map object from the matched element in the text
     */
    public Map<String, Object> toMap() {
        this.cleanMap();
        return capture;
    }

    /**
     * Remove and rename the unwanted elelents in the matched map.
     */
    private void cleanMap() {
        if (grok != null && grok.getGarbage() != null)
            grok.getGarbage().remove(capture);
    }

    /**
     * Util fct.
     *
     * @return boolean
     */
    public Boolean isNull() {
        return this.match == null;
    }

    /**
     * Util fct.
     *
     * @param s
     * @return boolean
     */
    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
