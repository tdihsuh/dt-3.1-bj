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
import com.google.code.regexp.Pattern;
import oi.thekraken.grok.api.exception.GrokException;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * {@code Grok} parse arbitrary text and structure it.<p/>
 * <p/>
 * {@code Grok} is simple API that allows you to easily parse logs
 * and other files (single line). With {@code Grok},
 * you can turn unstructured log and event data into structured data (JSON).
 * <p/>
 * example:<p/>
 * <pre>
 *  Grok grok = Grok.create("patterns/patterns");
 *  grok.compile("%{USER}");
 *  Match gm = grok.match("root");
 *  gm.captures();
 * </pre>
 *
 * @author anthonycorbacho
 * @since 0.0.1
 */
public class Grok {
    private static Map<String, String> definitionPatterns;
    private Garbage garbage;
    /**
     * Extract Grok patter like %{FOO} to FOO, Also Grok pattern with semantic.
     */
    private static Pattern GROK_PATTERN;


    /**
     * Named regex of the originalPattern.
     */
    private String namedRegex;
    /**
     * Pattern of the namedRegex.
     */
    private Pattern compiledNamedRegex;
    /**
     * Original {@code Grok} pattern (expl: %{IP}).
     */
    private String originalPattern;
    /**
     * Map of the named regex of the originalPattern
     * with id = namedregexid and value = namedregex.
     */
    private Map<String, String> namedOriginalRegexMap;
    /**
     * {@code Grok} discovery.
     */
    private Discovery disco;
    /**
     * only use in grok discovery.
     */
    private String savedPattern;

    /**
     * Create a new <i>empty</i>{@code Grok} object.
     */
    public Grok() {
        this(null);
    }

    public Grok(Garbage garbage) {
        originalPattern = StringUtils.EMPTY;
        disco = null;
        namedRegex = StringUtils.EMPTY;
        compiledNamedRegex = null;
        namedOriginalRegexMap = new TreeMap<>();
        savedPattern = StringUtils.EMPTY;
        if (garbage == null)
            this.garbage = new Garbage();
        else
            this.garbage = garbage;
    }

    static {
        init();
    }

    private static void init() {
        GROK_PATTERN = Pattern.compile(
                "%\\{(?<name>(?<pattern>[A-z0-9]+)(?::(?<subname>[A-z0-9_:@]+))?)(?:=(?<definition>(?:(?:[^{}]+|\\.+)+)+))?\\}");
        definitionPatterns = new TreeMap<>();
        URL uRl = Grok.class.getResource("/");
        String path = uRl.getPath();
        if (path.endsWith("test-classes/")) {
            path = path.replace("test-classes", "classes");
        }
        File file = new File(path + "/patterns");
        try {
            addPatternFromFile(file);
        } catch (GrokException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@code Grok} patterns definition.
     */


    /**
     * Add custom pattern to grok in the runtime.
     *
     * @param name    : Pattern Name
     * @param pattern : Regular expression Or {@code Grok} pattern
     * @throws oi.thekraken.grok.api.exception.GrokException
     */
    public static void addPattern(String name, String pattern) throws GrokException {
        if (name == null || pattern == null || name.isEmpty() || pattern.isEmpty()) {
            throw new GrokException("Invalid Pattern");
        }
        definitionPatterns.put(name, pattern);
    }


    /**
     * Get the current map of {@code Grok} pattern.
     *
     * @return Patterns (name, regular expression)
     */
    public Map<String, String> getPatterns() {
        return definitionPatterns;
    }

    /**
     * Copy the given Map of patterns (pattern name, regular expression) to {@code Grok},
     * duplicate element will be override.
     *
     * @param cpy : Map to copy
     * @throws GrokException
     */
    public void copyPatterns(Map<String, String> cpy) throws GrokException {
        if (cpy == null || cpy.isEmpty()) {
            throw new GrokException("Invalid Patterns");
        }
        definitionPatterns.putAll(cpy);
    }

    /**
     * Add patterns to {@code Grok} from the given file.
     *
     * @param f : Path of the grok pattern
     * @throws GrokException
     */
    public static void addPatternFromFile(File f) throws GrokException {
        if (!f.exists()) {
            throw new GrokException("Pattern not found");
        }
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null)
                for (File sub : files) {
                    addPatternFromFile(sub);
                }
        } else {
            if (!f.canRead()) {
                throw new GrokException("Pattern cannot be read");
            }
            FileReader r = null;
            try {
                r = new FileReader(f);
                addPatternFromReader(r);
            } catch (@SuppressWarnings("hiding") IOException e) {
                throw new GrokException(e.getMessage());
            } finally {
                try {
                    if (r != null) {
                        r.close();
                    }
                } catch (IOException io) {
                    // TODO(anthony) : log the error
                }
            }
        }


    }

    /**
     * Add patterns to {@code Grok} from a Reader.
     *
     * @param r : Reader with {@code Grok} patterns
     * @throws GrokException
     */
    public static void addPatternFromReader(Reader r) throws GrokException {
        BufferedReader br = new BufferedReader(r);
        String line;
        // We dont want \n and commented line
        Pattern pattern = Pattern.compile("^([A-z0-9_]+)\\s+(.*)$");
        try {
            while ((line = br.readLine()) != null) {
                Matcher m = pattern.matcher(line);
                if (m.matches()) {
                    addPattern(m.group(1), m.group(2));
                }
            }
            br.close();
        } catch (IOException | GrokException e) {
            throw new GrokException(e.getMessage());
        }

    }

    public String getSavedPattern() {
        return savedPattern;
    }

    public void setSavedPattern(String savedPattern) {
        this.savedPattern = savedPattern;
    }


    /**
     * Get the named regex from the {@code Grok} pattern. <p></p>
     * See {@link #compile(String)} for more detail.
     *
     * @return named regex
     */
    public String getNamedRegex() {
        return namedRegex;
    }

    /**
     * Match the given <tt>log</tt> with the named regex.
     * And return the json representation of the matched element
     *
     * @param log : log to match
     * @return json representation og the log
     */
    public String capture(String log) {
        Match match = match(log);
        match.captures();
        return match.toJson();
    }

    /**
     * Match the given list of <tt>log</tt> with the named regex
     * and return the list of json representation of the matched elements.
     *
     * @param logs : list of log
     * @return list of json representation of the log
     */
    public List<String> captures(List<String> logs) {
        List<String> matched = new ArrayList<String>();
        for (String log : logs) {
            Match match = match(log);
            match.captures();
            matched.add(match.toJson());
        }
        return matched;
    }

    public Match match(String text) {
        return match(text, null);
    }

    /**
     * Match the given <tt>text</tt> with the named regex
     * {@code Grok} will extract data from the string and get an extence of {@link Match}.
     *
     * @param text : Single line of log
     * @return Grok Match
     */
    public Match match(String text, Garbage garbage) {
        if (compiledNamedRegex == null || StringUtils.isBlank(text)) {
            return Match.EMPTY;
        }

        Matcher m = compiledNamedRegex.matcher(text);
        Match match = new Match(garbage);
        match.setGrok(this);
        if (m.find()) {
            match.setSubject(text);
            match.setMatch(m);
            match.setStart(m.start(0));
            match.setEnd(m.end(0));
        }
        return match;
    }

    public static Grok getInstance(String pattern) {
        Grok grok = new Grok();

        try {
            grok.compile(pattern);
        } catch (GrokException e) {
            // TODO(anthony) : log the error
        }
        return grok;
    }

    /**
     * Compile the {@code Grok} pattern to named regex pattern.
     *
     * @param pattern : Grok pattern (ex: %{IP})
     * @throws GrokException
     */
    public void compile(String pattern) throws GrokException {

        if (StringUtils.isBlank(pattern)) {
            throw new GrokException("{pattern} should not be empty or null");
        }

        namedRegex = pattern;
        originalPattern = pattern;
        int index = 0;
        /** flag for infinite recurtion */
        int iterationLeft = 1000;
        Boolean continueIteration = true;

        // Replace %{foo} with the regex (mostly groupname regex)
        // and then compile the regex
        while (continueIteration) {
            continueIteration = false;
            if (iterationLeft <= 0) {
                throw new GrokException("Deep recursion pattern compilation of " + originalPattern);
            }
            iterationLeft--;

            Matcher m = GROK_PATTERN.matcher(namedRegex);
            // Match %{Foo:bar} -> pattern name and subname
            // Match %{Foo=regex} -> add new regex definition
            if (m.find()) {
                continueIteration = true;
                Map<String, String> group = m.namedGroups();
                if (group.get("definition") != null) {
                    try {
                        addPattern(group.get("pattern"), group.get("definition"));
                        group.put("name", group.get("name") + "=" + group.get("definition"));
                    } catch (GrokException e) {
                        // Log the exeception
                    }
                }
                String subname = group.get("subname");
                String key;
                if (subname != null) {
                    key = "field" + index;
                    namedOriginalRegexMap.put(key, subname);
                } else {
                    key = "name" + index;
                    namedOriginalRegexMap.put(key, group.get("name"));
                }
                namedRegex =
                        StringUtils.replace(namedRegex, "%{" + group.get("name") + "}", "(?<" + key + ">"
                                + definitionPatterns.get(group.get("pattern")) + ")");
                // System.out.println(_expanded_pattern);
                index++;
            }
        }

        if (namedRegex.isEmpty()) {
            throw new GrokException("Pattern not fount");
        }
        // Compile the regex
        compiledNamedRegex = Pattern.compile(namedRegex);
    }

    /**
     * {@code Grok} will try to find the best expression that will match your input.
     * {@link Discovery}
     *
     * @param input : Single line of log
     * @return the Grok pattern
     */
    public String discover(String input) {

        if (disco == null) {
            disco = new Discovery();
        }
        return disco.discover(input, this);
    }

    /**
     * Original grok pattern used to compile to the named regex.
     *
     * @return String Original Grok pattern
     */
    public String getOriginalPattern() {
        return originalPattern;
    }

    /**
     * Get the named regex from the given id.
     *
     * @param id : named regex id
     * @return String of the named regex
     */
    public String getNamedRegexCollectionById(String id) {
        return namedOriginalRegexMap.get(id);
    }

    /**
     * Get the full collection of the named regex.
     *
     * @return named RegexCollection
     */
    public Map<String, String> getNamedOriginalRegexMap() {
        return namedOriginalRegexMap;
    }

    public Garbage getGarbage() {
        return garbage;
    }

    public void setGarbage(Garbage garbage) {
        this.garbage = garbage;
    }
}
