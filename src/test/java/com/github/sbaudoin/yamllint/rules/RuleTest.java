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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuleTest {
    @Test
    void getId() {
        assertThat(new Rule() {
            @Override
            public TYPE getType() {
                return null;
            }
        }.getId()).isEqualTo("rule-test$1");
    }

    @Test
    void ignore() {
        Rule rule = getSimpleRule();
        rule.setIgnore(Arrays.asList(".*\\.txt$", "foo.bar"));

        assertThat(rule.ignores(null)).isFalse();
        assertThat(rule.ignores(new File("foo.txt"))).isTrue();
        assertThat(rule.ignores(new File("/foo/x.txt/bar"))).isFalse();
        assertThat(rule.ignores(new File("foo.bar"))).isTrue();
        assertThat(rule.ignores(new File("fooxbar"))).isTrue();
    }

    @Test
    void level() {
        Rule rule = getSimpleRule();
        rule.setLevel(Linter.ERROR_LEVEL);
        assertThat(rule.getLevel()).isEqualTo(Linter.ERROR_LEVEL);
    }

    @Test
    void getOptions() {
        Rule rule = new Rule() {
            {
                registerOption("option_name", Boolean.class);
            }

            @Override
            public TYPE getType() {
                return null;
            }
        };
        assertThat(rule.getOptions().size()).isEqualTo(1);
        assertThat(rule.getOptions().get("option_name")).isEqualTo(Boolean.class);
    }

    @Test
    void getType() {
        assertThat(getSimpleRule().getType()).isEqualTo(Rule.TYPE.TOKEN);
    }

    @Test
    void spacesAfter() {
        Rule rule = getSimpleRule();

        List<Token> tokens = getTokens("-    4SpaceKey");
        assertThat(rule.spacesAfter(tokens.get(2), tokens.get(3), null, null, "", "")).isNull(); // No min or max
        assertThat(rule.spacesAfter(tokens.get(2), tokens.get(3), 2, null, "", "")).isNull(); // No max
        assertThat(rule.spacesAfter(tokens.get(2), tokens.get(3), null, 2, "", "")).isEqualTo(new LintProblem(1, 5, null));
        assertThat(rule.spacesAfter(tokens.get(2), tokens.get(3), 4, null, "", "")).isNull(); // No max
        assertThat(rule.spacesAfter(tokens.get(2), tokens.get(3), 5, null, "", "")).isEqualTo(new LintProblem(1, 6, null));
        assertThat(rule.spacesAfter(tokens.get(2), tokens.get(3), 4, 4, "", "")).isNull();
        assertThat(rule.spacesAfter(tokens.get(2), tokens.get(3), 4, 2, "", "")).isEqualTo(new LintProblem(1, 5, null));
    }

    @Test
    void spacesBefore() {
        Rule rule = getSimpleRule();

        List<Token> tokens = getTokens("-    4SpaceKey");
        assertThat(rule.spacesBefore(tokens.get(3), tokens.get(2), null, null, "", "")).isNull(); // No min or max
        assertThat(rule.spacesBefore(tokens.get(3), tokens.get(2), 2, null, "", "")).isNull(); // No max
        assertThat(rule.spacesBefore(tokens.get(3), tokens.get(2), null, 2, "", "")).isEqualTo(new LintProblem(1, 5, null));
        assertThat(rule.spacesBefore(tokens.get(3), tokens.get(2), 4, null, "", "")).isNull(); // No max
        assertThat(rule.spacesBefore(tokens.get(3), tokens.get(2), 5, null, "", "")).isEqualTo(new LintProblem(1, 6, null));
        assertThat(rule.spacesBefore(tokens.get(3), tokens.get(2), 4, 4, "", "")).isNull();
        assertThat(rule.spacesBefore(tokens.get(3), tokens.get(2), 4, 2, "", "")).isEqualTo(new LintProblem(1, 5, null));
    }

    @Test
    void isExplicitKey() {
        Rule rule = getSimpleRule();

        List<Token> tokens = getTokens("key: value");
        assertThat(tokens.get(2)).isInstanceOf(KeyToken.class);
        assertThat(rule.isExplicitKey(tokens.get(2))).isFalse();

        tokens = getTokens("? key\n  : v");
        assertThat(tokens.get(2)).isInstanceOf(KeyToken.class);
        assertThat(rule.isExplicitKey(tokens.get(2))).isTrue();

        tokens = getTokens("?\n  key\n  : v");
        assertThat(tokens.get(2)).isInstanceOf(KeyToken.class);
        assertThat(rule.isExplicitKey(tokens.get(2))).isTrue();
    }

    @Test
    void getLineIndent() {
        Rule rule = getSimpleRule();

        List<Token> tokens = getTokens("""
                                       a: 1
                                       b:
                                         - c: [2, 3, {d: 4}]
                                       """);

        assertThat(((ScalarToken)tokens.get(3)).getValue()).isEqualTo("a");
        assertThat(rule.getLineIndent(tokens.get(3))).isEqualTo(0);
        assertThat(((ScalarToken)tokens.get(5)).getValue()).isEqualTo("1");
        assertThat(rule.getLineIndent(tokens.get(5))).isEqualTo(0);
        assertThat(((ScalarToken)tokens.get(7)).getValue()).isEqualTo("b");
        assertThat(rule.getLineIndent(tokens.get(7))).isEqualTo(0);
        assertThat(((ScalarToken)tokens.get(13)).getValue()).isEqualTo("c");
        assertThat(rule.getLineIndent(tokens.get(13))).isEqualTo(2);
        assertThat(((ScalarToken)tokens.get(16)).getValue()).isEqualTo("2");
        assertThat(rule.getLineIndent(tokens.get(16))).isEqualTo(2);
        assertThat(((ScalarToken)tokens.get(18)).getValue()).isEqualTo("3");
        assertThat(rule.getLineIndent(tokens.get(18))).isEqualTo(2);
        assertThat(((ScalarToken)tokens.get(22)).getValue()).isEqualTo("d");
        assertThat(rule.getLineIndent(tokens.get(22))).isEqualTo(2);
        assertThat(((ScalarToken)tokens.get(24)).getValue()).isEqualTo("4");
        assertThat(rule.getLineIndent(tokens.get(24))).isEqualTo(2);
    }

    @Test
    void isWhitespace() {
        Rule rule = getSimpleRule();
        assertThat(rule.isWhitespace('\t')).isTrue(); // tab (ASCII 9)
        assertThat(rule.isWhitespace('\n')).isTrue(); // line feed (ASCII 10)
        assertThat(rule.isWhitespace(0x000b)).isTrue(); // vertical tab (ASCII 11)
        assertThat(rule.isWhitespace('\f')).isTrue(); // form feed (ASCII 12)
        assertThat(rule.isWhitespace('\r')).isTrue(); // carriage return (ASCII 13)
        assertThat(rule.isWhitespace(' ')).isTrue(); // space (ASCII 32)
        assertThat(rule.isWhitespace('x')).isFalse();
    }

    @Test
    void find() {
        Rule rule = getSimpleRule();
        int[] haystack = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

        // Standard cases
        assertThat(rule.find(haystack, 4, 2, 18)).isEqualTo(4);
        assertThat(rule.find(haystack, 10, 5, 18)).isEqualTo(-1);
        assertThat(rule.find(haystack, 4, 16, 18)).isEqualTo(-1);
        assertThat(rule.find(haystack, 9, 2, 8)).isEqualTo(-1);
        assertThat(rule.find(haystack, 4, 4, 4)).isEqualTo(-1);
        // Boundary tests
        assertThatThrownBy(() -> rule.find(haystack, 4, -1, 8)).isInstanceOf(ArrayIndexOutOfBoundsException.class);
        assertThatThrownBy(() -> rule.find(haystack, 4, 20, 8)).isInstanceOf(ArrayIndexOutOfBoundsException.class);
        assertThatThrownBy(() -> rule.find(haystack, 4, 20, 40)).isInstanceOf(ArrayIndexOutOfBoundsException.class);
        assertThat(rule.find(haystack, 4, 2, 10)).isEqualTo(4);
        assertThatThrownBy(() -> rule.find(haystack, 4, 2, 21)).isInstanceOf(ArrayIndexOutOfBoundsException.class);
        assertThatThrownBy(() -> rule.find(haystack, 4, 2, -1)).isInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

    @Test
    void rfind() {
        Rule rule = getSimpleRule();
        int[] haystack = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

        // Standard cases
        assertThat(rule.rfind(haystack, 4, 2, 18)).isEqualTo(14);
        assertThat(rule.rfind(haystack, 10, 5, 8)).isEqualTo(-1);
        assertThat(rule.rfind(haystack, 4, 6, 8)).isEqualTo(-1);
        assertThat(rule.rfind(haystack, 9, 2, 8)).isEqualTo(-1);
        assertThat(rule.rfind(haystack, 4, 4, 4)).isEqualTo(-1);
        // Boundary tests
        assertThatThrownBy(() -> rule.rfind(haystack, 4, -1, 8)).isInstanceOf(ArrayIndexOutOfBoundsException.class);
        assertThatThrownBy(() -> rule.rfind(haystack, 4, 20, 8)).isInstanceOf(ArrayIndexOutOfBoundsException.class);
        assertThatThrownBy(() -> rule.rfind(haystack, 4, 20, 40)).isInstanceOf(ArrayIndexOutOfBoundsException.class);
        assertThat(rule.find(haystack, 4, 2, 10)).isEqualTo(4);
        assertThatThrownBy(() -> rule.rfind(haystack, 4, 2, 21)).isInstanceOf(ArrayIndexOutOfBoundsException.class);
        assertThatThrownBy(() -> rule.rfind(haystack, 4, 2, -1)).isInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

    @Test
    void getRealEndLine() {
        Rule rule = getSimpleRule();

        List<Token> tokens = getTokens("key: value\n\n");
        assertThat(((ScalarToken)tokens.get(5)).getValue()).isEqualTo("value");
        assertThat(rule.getRealEndLine(tokens.get(5))).isEqualTo(1);

        tokens = getTokens("""
                           long text:
                               'very "long"
                                ''string'' with
                           
                                paragraph gap, \\n and
                                spaces.'
                           other text: 'much shorter'""");
        assertThat(((ScalarToken)tokens.get(5)).getValue()).isEqualTo("very \"long\" 'string' with\nparagraph gap, \\n and spaces.");
        assertThat(rule.getRealEndLine(tokens.get(5))).isEqualTo(6);

        tokens = getTokens("""
                           long text: >
                               very "long"
                                'string' with
                           
                                 paragraph gap, \\n and
                                  spaces.
                           other text: 'much shorter'""");
        assertThat(((ScalarToken)tokens.get(5)).getValue()).isEqualTo("very \"long\"\n 'string' with\n\n  paragraph gap, \\n and\n   spaces.\n");
        assertThat(rule.getRealEndLine(tokens.get(5))).isEqualTo(6);

        tokens = getTokens("""
                           key: |
                               multi
                               line
                           key2: text""");
        assertThat(((ScalarToken)tokens.get(5)).getValue()).isEqualTo("multi\nline\n");
        assertThat(rule.getRealEndLine(tokens.get(5))).isEqualTo(3);

        tokens = getTokens("""
                           key:
                               |
                                 multi
                                 line
                           key2: text""");
        assertThat(((ScalarToken)tokens.get(5)).getValue()).isEqualTo("multi\nline\n");
        assertThat(rule.getRealEndLine(tokens.get(5))).isEqualTo(4);

        tokens = getTokens("""
                           - ? |
                                 multi-line
                                 key
                             : |
                                 multi-line
                                 value
                           key2: text""");
        assertThat(((ScalarToken)tokens.get(5)).getValue()).isEqualTo("multi-line\nkey\n");
        assertThat(rule.getRealEndLine(tokens.get(5))).isEqualTo(3);
        assertThat(((ScalarToken)tokens.get(7)).getValue()).isEqualTo("multi-line\nvalue\n");
        assertThat(rule.getRealEndLine(tokens.get(7))).isEqualTo(6);

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
        assertThat(((ScalarToken)tokens.get(5)).getValue()).isEqualTo("multi-line\nkey\n");
        assertThat(rule.getRealEndLine(tokens.get(5))).isEqualTo(4);
        assertThat(((ScalarToken)tokens.get(7)).getValue()).isEqualTo("multi-line\nvalue\n");
        assertThat(rule.getRealEndLine(tokens.get(7))).isEqualTo(8);
    }

    @Test
    void isDigit() {
        Rule rule = getSimpleRule();
        assertThat(rule.isDigit(null)).isFalse();
        assertThat(rule.isDigit("")).isFalse();
        assertThat(rule.isDigit("123")).isTrue();
        assertThat(rule.isDigit("-123")).isFalse();
        assertThat(rule.isDigit("1.23")).isFalse();
        assertThat(rule.isDigit("a b c d")).isFalse();
    }

    @Test
    void commentRule() {
        assertThat(new CommentRule() {
            @Override
            public List<LintProblem> check(Map conf, Parser.Comment comment) {
                return null;
            }
        }.getType()).isEqualTo(Rule.TYPE.COMMENT);
    }

    @Test
    void lineRule() {
        assertThat(new LineRule() {
            @Override
            public List<LintProblem> check(Map conf, Parser.Line line) {
                return null;
            }
        }.getType()).isEqualTo(Rule.TYPE.LINE);
    }

    @Test
    void tokenRule() {
        assertThat(new TokenRule() {
            @Override
            public List<LintProblem> check(Map<Object, Object> conf, Token token, Token prev, Token next, Token nextnext, Map<String, Object> context) {
                return null;
            }
        }.getType()).isEqualTo(Rule.TYPE.TOKEN);
    }

    @Test
    void parameters() {
        Rule rule = getSimpleRule();

        rule.addParameter("a name", "a value");
        assertThat(rule.getParameter("a name")).isEqualTo("a value");
        assertThat(rule.getParameter("another name")).isNull();
    }

    @Test
    void testDefault() {
        assertThatThrownBy(() -> new Rule() {
            {
                registerOption("opt1", Collections.emptyList());
            }

            @Override
            public TYPE getType() {
                return TYPE.TOKEN;
            }
        }).isInstanceOf(IllegalArgumentException.class);

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
        assertThat(rule.getDefaultOptionValue("opt1")).isEqualTo("a value");
        assertThat(rule.getDefaultOptionValue("opt2")).isEqualTo(128);
        assertThat(rule.getDefaultOptionValue("opt3")).isEqualTo("string");
        assertThatThrownBy(() -> rule.getDefaultOptionValue("foo")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isList() {
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
        assertThat(rule.isListOption("opt1")).isFalse();
        assertThat(rule.isListOption("opt2")).isFalse();
        assertThat(rule.isListOption("opt3")).isTrue();
        assertThat(((List<?>)rule.getDefaultOptionValue("opt3")).size()).isEqualTo(0);
        assertThat(rule.isListOption("opt4")).isFalse();
        assertThat(rule.isListOption("opt5")).isTrue();
        assertThat(((List<?>)rule.getOptions().get("opt5")).size()).isEqualTo(3);
        assertThat(((List<?>)rule.getDefaultOptionValue("opt5")).size()).isEqualTo(0);
        assertThatThrownBy(() -> rule.getDefaultOptionValue("foo")).isInstanceOf(IllegalArgumentException.class);
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
