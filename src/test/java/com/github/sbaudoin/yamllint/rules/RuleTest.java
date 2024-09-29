/**
 * Copyright (c) 2018-2023, Sylvain Baudoin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.sbaudoin.yamllint.rules;

import com.github.sbaudoin.yamllint.LintProblem;
import com.github.sbaudoin.yamllint.Linter;
import com.github.sbaudoin.yamllint.Parser;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.scanner.Scanner;
import org.yaml.snakeyaml.scanner.ScannerImpl;
import org.yaml.snakeyaml.tokens.KeyToken;
import org.yaml.snakeyaml.tokens.ScalarToken;
import org.yaml.snakeyaml.tokens.Token;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class RuleTest {
    @Test
    void testGetId() {
        assertEquals("rule-test$1", new Rule() {
            @Override
            public TYPE getType() {
                return null;
            }
        }.getId());
    }

    @Test
    void testIgnore() {
        Rule rule = getSimpleRule();
        rule.setIgnore(Arrays.asList(".*\\.txt$", "foo.bar"));

        assertFalse(rule.ignores(null));
        assertTrue(rule.ignores(new File("foo.txt")));
        assertFalse(rule.ignores(new File("/foo/x.txt/bar")));
        assertTrue(rule.ignores(new File("foo.bar")));
        assertTrue(rule.ignores(new File("fooxbar")));
    }

    @Test
    void testLevel() {
        Rule rule = getSimpleRule();
        rule.setLevel(Linter.ERROR_LEVEL);
        assertEquals(Linter.ERROR_LEVEL, rule.getLevel());
    }

    @Test
    void testGetOptions() {
        Rule rule = new Rule() {
            {
                registerOption("option_name", Boolean.class);
            }

            @Override
            public TYPE getType() {
                return null;
            }
        };
        assertEquals(1, rule.getOptions().size());
        assertEquals(Boolean.class, rule.getOptions().get("option_name"));
    }

    @Test
    void testGetType() {
        assertEquals(Rule.TYPE.TOKEN, getSimpleRule().getType());
    }

    @Test
    void testSpacesAfter() {
        Rule rule = getSimpleRule();

        List<Token> tokens = getTokens("-    4SpaceKey");
        assertNull(rule.spacesAfter(tokens.get(2), tokens.get(3), null, null, "", "")); // No min or max
        assertNull(rule.spacesAfter(tokens.get(2), tokens.get(3), 2, null, "", "")); // No max
        assertEquals(new LintProblem(1, 5, null),
                rule.spacesAfter(tokens.get(2), tokens.get(3), null, 2, "", ""));
        assertNull(rule.spacesAfter(tokens.get(2), tokens.get(3), 4, null, "", "")); // No max
        assertEquals(new LintProblem(1, 6, null),
                rule.spacesAfter(tokens.get(2), tokens.get(3), 5, null, "", ""));
        assertNull(rule.spacesAfter(tokens.get(2), tokens.get(3), 4, 4, "", ""));
        assertEquals(new LintProblem(1, 5, null),
                rule.spacesAfter(tokens.get(2), tokens.get(3), 4, 2, "", ""));
    }

    @Test
    void testSpacesBefore() {
        Rule rule = getSimpleRule();

        List<Token> tokens = getTokens("-    4SpaceKey");
        assertNull(rule.spacesBefore(tokens.get(3), tokens.get(2), null, null, "", "")); // No min or max
        assertNull(rule.spacesBefore(tokens.get(3), tokens.get(2), 2, null, "", "")); // No max
        assertEquals(new LintProblem(1, 5, null),
                rule.spacesBefore(tokens.get(3), tokens.get(2), null, 2, "", ""));
        assertNull(rule.spacesBefore(tokens.get(3), tokens.get(2), 4, null, "", "")); // No max
        assertEquals(new LintProblem(1, 6, null),
                rule.spacesBefore(tokens.get(3), tokens.get(2), 5, null, "", ""));
        assertNull(rule.spacesBefore(tokens.get(3), tokens.get(2), 4, 4, "", ""));
        assertEquals(new LintProblem(1, 5, null),
                rule.spacesBefore(tokens.get(3), tokens.get(2), 4, 2, "", ""));
    }

    @Test
    void testIsExplicitKey() {
        Rule rule = getSimpleRule();

        List<Token> tokens = getTokens("key: value");
        assertTrue(tokens.get(2) instanceof KeyToken);
        assertFalse(rule.isExplicitKey(tokens.get(2)));

        tokens = getTokens("? key\n  : v");
        assertTrue(tokens.get(2) instanceof KeyToken);
        assertTrue(rule.isExplicitKey(tokens.get(2)));

        tokens = getTokens("?\n  key\n  : v");
        assertTrue(tokens.get(2) instanceof KeyToken);
        assertTrue(rule.isExplicitKey(tokens.get(2)));
    }

    @Test
    void testGetLineIndent() {
        Rule rule = getSimpleRule();

        List<Token> tokens = getTokens("""
                                       a: 1
                                       b:
                                         - c: [2, 3, {d: 4}]
                                       """);

        assertEquals("a", ((ScalarToken)tokens.get(3)).getValue());
        assertEquals(0, rule.getLineIndent(tokens.get(3)));
        assertEquals("1", ((ScalarToken)tokens.get(5)).getValue());
        assertEquals(0, rule.getLineIndent(tokens.get(5)));
        assertEquals("b", ((ScalarToken)tokens.get(7)).getValue());
        assertEquals(0, rule.getLineIndent(tokens.get(7)));
        assertEquals("c", ((ScalarToken)tokens.get(13)).getValue());
        assertEquals(2, rule.getLineIndent(tokens.get(13)));
        assertEquals("2", ((ScalarToken)tokens.get(16)).getValue());
        assertEquals(2, rule.getLineIndent(tokens.get(16)));
        assertEquals("3", ((ScalarToken)tokens.get(18)).getValue());
        assertEquals(2, rule.getLineIndent(tokens.get(18)));
        assertEquals("d", ((ScalarToken)tokens.get(22)).getValue());
        assertEquals(2, rule.getLineIndent(tokens.get(22)));
        assertEquals("4", ((ScalarToken)tokens.get(24)).getValue());
        assertEquals(2, rule.getLineIndent(tokens.get(24)));
    }

    @Test
    void testIsWhitespace() {
        Rule rule = getSimpleRule();
        assertTrue(rule.isWhitespace('\t')); // tab (ASCII 9)
        assertTrue(rule.isWhitespace('\n')); // line feed (ASCII 10)
        assertTrue(rule.isWhitespace(0x000b)); // vertical tab (ASCII 11)
        assertTrue(rule.isWhitespace('\f')); // form feed (ASCII 12)
        assertTrue(rule.isWhitespace('\r')); // carriage return (ASCII 13)
        assertTrue(rule.isWhitespace(' ')); // space (ASCII 32)
        assertFalse(rule.isWhitespace('x'));
    }

    @Test
    void testFind() {
        Rule rule = getSimpleRule();
        int[] haystack = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

        // Standard cases
        assertEquals(4, rule.find(haystack, 4, 2, 18));
        assertEquals(-1, rule.find(haystack, 10, 5, 18));
        assertEquals(-1, rule.find(haystack, 4, 16, 18));
        assertEquals(-1, rule.find(haystack, 9, 2, 8));
        assertEquals(-1, rule.find(haystack, 4, 4, 4));
        // Boundary tests
        try {
            rule.find(haystack, 4, -1, 8);
            fail("Cannot accept negative start index");
        } catch (ArrayIndexOutOfBoundsException e) {
            assertTrue(true);
        }
        try {
            rule.find(haystack, 4, 20, 8);
            fail("Cannot accept start index greater than the haystack length");
        } catch (ArrayIndexOutOfBoundsException e) {
            assertTrue(true);
        }
        try {
            rule.find(haystack, 4, 20, 40);
            fail("Cannot accept start index greater than the haystack length");
        } catch (ArrayIndexOutOfBoundsException e) {
            assertTrue(true);
        }
        assertEquals(4, rule.find(haystack, 4, 2, 10));
        try {
            rule.find(haystack, 4, 2, 21);
            fail("Cannot accept end index greater than the haystack length");
        } catch (ArrayIndexOutOfBoundsException e) {
            assertTrue(true);
        }
        try {
            rule.find(haystack, 4, 2, -1);
            fail("Cannot accept negative end index");
        } catch (ArrayIndexOutOfBoundsException e) {
            assertTrue(true);
        }
    }

    @Test
    void testRfind() {
        Rule rule = getSimpleRule();
        int[] haystack = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

        // Standard cases
        assertEquals(14, rule.rfind(haystack, 4, 2, 18));
        assertEquals(-1, rule.rfind(haystack, 10, 5, 8));
        assertEquals(-1, rule.rfind(haystack, 4, 6, 8));
        assertEquals(-1, rule.rfind(haystack, 9, 2, 8));
        assertEquals(-1, rule.rfind(haystack, 4, 4, 4));
        // Boundary tests
        try {
            rule.rfind(haystack, 4, -1, 8);
            fail("Cannot accept negative start index");
        } catch (ArrayIndexOutOfBoundsException e) {
            assertTrue(true);
        }
        try {
            rule.rfind(haystack, 4, 20, 8);
            fail("Cannot accept start index greater than the haystack length");
        } catch (ArrayIndexOutOfBoundsException e) {
            assertTrue(true);
        }
        try {
            rule.rfind(haystack, 4, 20, 40);
            fail("Cannot accept start index greater than the haystack length");
        } catch (ArrayIndexOutOfBoundsException e) {
            assertTrue(true);
        }
        assertEquals(4, rule.find(haystack, 4, 2, 10));
        try {
            rule.rfind(haystack, 4, 2, 21);
            fail("Cannot accept end index greater than the haystack length");
        } catch (ArrayIndexOutOfBoundsException e) {
            assertTrue(true);
        }
        try {
            rule.rfind(haystack, 4, 2, -1);
            fail("Cannot accept negative end index");
        } catch (ArrayIndexOutOfBoundsException e) {
            assertTrue(true);
        }
    }

    @Test
    void testGetRealEndLine() {
        Rule rule = getSimpleRule();

        List<Token> tokens = getTokens("key: value\n\n");
        assertEquals("value", ((ScalarToken)tokens.get(5)).getValue());
        assertEquals(1, rule.getRealEndLine(tokens.get(5)));

        tokens = getTokens("""
                           long text:
                               'very "long"
                                ''string'' with
                           
                                paragraph gap, \\n and
                                spaces.'
                           other text: 'much shorter'""");
        assertEquals("very \"long\" 'string' with\nparagraph gap, \\n and spaces.", ((ScalarToken)tokens.get(5)).getValue());
        assertEquals(6, rule.getRealEndLine(tokens.get(5)));

        tokens = getTokens("""
                           long text: >
                               very "long"
                                'string' with
                           
                                 paragraph gap, \\n and
                                  spaces.
                           other text: 'much shorter'""");
        assertEquals("very \"long\"\n 'string' with\n\n  paragraph gap, \\n and\n   spaces.\n", ((ScalarToken)tokens.get(5)).getValue());
        assertEquals(6, rule.getRealEndLine(tokens.get(5)));

        tokens = getTokens("""
                           key: |
                               multi
                               line
                           key2: text""");
        assertEquals("multi\nline\n", ((ScalarToken)tokens.get(5)).getValue());
        assertEquals(3, rule.getRealEndLine(tokens.get(5)));

        tokens = getTokens("""
                           key:
                               |
                                 multi
                                 line
                           key2: text""");
        assertEquals("multi\nline\n", ((ScalarToken)tokens.get(5)).getValue());
        assertEquals(4, rule.getRealEndLine(tokens.get(5)));

        tokens = getTokens("""
                           - ? |
                                 multi-line
                                 key
                             : |
                                 multi-line
                                 value
                           key2: text""");
        assertEquals("multi-line\nkey\n", ((ScalarToken)tokens.get(5)).getValue());
        assertEquals(3, rule.getRealEndLine(tokens.get(5)));
        assertEquals("multi-line\nvalue\n", ((ScalarToken)tokens.get(7)).getValue());
        assertEquals(6, rule.getRealEndLine(tokens.get(7)));

        tokens = getTokens("""
                           - ?
                               |
                                 multi-line
                                 key
                             :
                               |
                                 multi-line
                                 value
                           """);
        assertEquals("multi-line\nkey\n", ((ScalarToken)tokens.get(5)).getValue());
        assertEquals(4, rule.getRealEndLine(tokens.get(5)));
        assertEquals("multi-line\nvalue\n", ((ScalarToken)tokens.get(7)).getValue());
        assertEquals(8, rule.getRealEndLine(tokens.get(7)));
    }

    @Test
    void testIsDigit() {
        Rule rule = getSimpleRule();
        assertFalse(rule.isDigit(null));
        assertFalse(rule.isDigit(""));
        assertTrue(rule.isDigit("123"));
        assertFalse(rule.isDigit("-123"));
        assertFalse(rule.isDigit("1.23"));
        assertFalse(rule.isDigit("a b c d"));
    }

    @Test
    void testCommentRule() {
        assertEquals(Rule.TYPE.COMMENT, new CommentRule() {
            @Override
            public List<LintProblem> check(Map conf, Parser.Comment comment) {
                return null;
            }
        }.getType());
    }

    @Test
    void testLineRule() {
        assertEquals(Rule.TYPE.LINE, new LineRule() {
            @Override
            public List<LintProblem> check(Map conf, Parser.Line line) {
                return null;
            }
        }.getType());
    }

    @Test
    void testTokenRule() {
        assertEquals(Rule.TYPE.TOKEN, new TokenRule() {
            @Override
            public List<LintProblem> check(Map<Object, Object> conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
                return null;
            }
        }.getType());
    }

    @Test
    void testParameters() {
        Rule rule = getSimpleRule();

        rule.addParameter("a name", "a value");
        assertEquals("a value", rule.getParameter("a name"));
        assertNull(rule.getParameter("another name"));
    }

    @Test
    void testDefault() {
        try {
            new Rule() {
                {
                    registerOption("opt1", Collections.emptyList());
                }

                @Override
                public TYPE getType() {
                    return TYPE.TOKEN;
                }
            };
            fail("Cannot get a default value from an empty list");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        Rule rule = new Rule() {
            {
                registerOption("opt1", "a value");
                registerOption("opt2", Arrays.asList(Boolean.class, "string", Integer.class), 128);
                registerOption("opt3", Arrays.asList("string", true, Integer.class));
            }

            @Override
            public TYPE getType() {
                return TYPE.TOKEN;
            }
        };
        assertEquals("a value", rule.getDefaultOptionValue("opt1"));
        assertEquals(128, rule.getDefaultOptionValue("opt2"));
        assertEquals("string", rule.getDefaultOptionValue("opt3"));
        try {
            rule.getDefaultOptionValue("foo");
            fail("Unknown option accepted");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    void testIsList() {
        Rule rule = new Rule() {
            {
                registerOption("opt1", "a value");
                registerOption("opt2", Arrays.asList(Boolean.class, "string", Integer.class));
                registerListOption("opt3", Collections.emptyList());
                registerOption("opt4", Arrays.asList(Boolean.class, "string", Integer.class), Collections.emptyList());
                registerListOption("opt5", Arrays.asList(Boolean.class, "string", Integer.class), Collections.emptyList());
            }

            @Override
            public TYPE getType() {
                return TYPE.TOKEN;
            }
        };
        assertFalse(rule.isListOption("opt1"));
        assertFalse(rule.isListOption("opt2"));
        assertTrue(rule.isListOption("opt3"));
        assertEquals(0, ((List<?>)rule.getDefaultOptionValue("opt3")).size());
        assertFalse(rule.isListOption("opt4"));
        assertTrue(rule.isListOption("opt5"));
        assertEquals(3, ((List<?>)rule.getOptions().get("opt5")).size());
        assertEquals(0, ((List<?>)rule.getDefaultOptionValue("opt5")).size());
        try {
            rule.getDefaultOptionValue("foo");
            fail("Unknown option accepted");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }


    /**
     * Returns an empty <code>TokenRule</code>
     *
     * @return an empty <code>TokenRule</code>
     */
    private Rule getSimpleRule() {
        return new Rule() {
            @Override
            public TYPE getType() {
                return TYPE.TOKEN;
            }
        };
    }

    /**
     * Returns the passed YAML string as a list of token
     *
     * @param yaml a YAML string
     * @return the tokens scanned in the passed YAML string
     */
    private List<Token> getTokens(String yaml) {
        List<Token> tokens = new ArrayList<>();
        Scanner scanner = new ScannerImpl(new StreamReader(yaml), new LoaderOptions());
        while (scanner.checkToken()) {
            tokens.add(scanner.getToken());
        }

        return tokens;
    }
}
