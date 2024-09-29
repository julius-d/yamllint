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
package com.github.sbaudoin.yamllint;

import com.github.sbaudoin.yamllint.rules.RuleTester;
import org.junit.jupiter.api.Test;

class YamlLintDirectivesTest extends RuleTester {
    @Test
    void testDisableDirective() throws YamlLintConfigException {
        YamlLintConfig conf = getDefaultConf();

        check("""
              ---
              - [valid , YAML]
              - trailing spaces   \s
              - bad   : colon
              - [valid , YAML]
              - bad  : colon and spaces  \s
              - [valid , YAML]
              """,
            conf,
            getLintProblem(3, 18, "trailing-spaces"),
            getLintProblem(4, 8, "colons"),
            getLintProblem(6, 7, "colons"),
            getLintProblem(6, 26, "trailing-spaces"));
        check("""
              ---
              - [valid , YAML]
              - trailing spaces   \s
              # yamllint disable
              - bad   : colon
              - [valid , YAML]
              - bad  : colon and spaces  \s
              - [valid , YAML]
              """,
            conf,
            getLintProblem(3, 18, "trailing-spaces"));
        check("""
              ---
              - [valid , YAML]
              # yamllint disable
              - trailing spaces   \s
              - bad   : colon
              - [valid , YAML]
              # yamllint enable
              - bad  : colon and spaces  \s
              - [valid , YAML]
              """,
            conf,
            getLintProblem(8, 7, "colons"),
            getLintProblem(8, 26, "trailing-spaces"));
    }

    @Test
    void testDisableDirectiveWithRules() throws YamlLintConfigException {
        YamlLintConfig conf = getDefaultConf();

        check("""
              ---
              - [valid , YAML]
              - trailing spaces   \s
              # yamllint disable rule:trailing-spaces
              - bad   : colon
              - [valid , YAML]
              - bad  : colon and spaces  \s
              - [valid , YAML]
              """,
            conf,
            getLintProblem(3, 18, "trailing-spaces"),
            getLintProblem(5, 8, "colons"),
            getLintProblem(7, 7, "colons"));
        check("""
              ---
              - [valid , YAML]
              # yamllint disable rule:trailing-spaces
              - trailing spaces   \s
              - bad   : colon
              - [valid , YAML]
              # yamllint enable rule:trailing-spaces
              - bad  : colon and spaces  \s
              - [valid , YAML]
              """,
            conf,
            getLintProblem(5, 8, "colons"),
            getLintProblem(8, 7, "colons"),
            getLintProblem(8, 26, "trailing-spaces"));
        check("""
              ---
              - [valid , YAML]
              # yamllint disable rule:trailing-spaces
              - trailing spaces   \s
              - bad   : colon
              - [valid , YAML]
              # yamllint enable
              - bad  : colon and spaces  \s
              - [valid , YAML]
              """,
            conf,
            getLintProblem(5, 8, "colons"),
            getLintProblem(8, 7, "colons"),
            getLintProblem(8, 26, "trailing-spaces"));
        check("""
              ---
              - [valid , YAML]
              # yamllint disable
              - trailing spaces   \s
              - bad   : colon
              - [valid , YAML]
              # yamllint enable rule:trailing-spaces
              - bad  : colon and spaces  \s
              - [valid , YAML]
              """,
            conf,
            getLintProblem(8, 26, "trailing-spaces"));
        check("""
              ---
              - [valid , YAML]
              # yamllint disable rule:colons
              - trailing spaces   \s
              # yamllint disable rule:trailing-spaces
              - bad   : colon
              - [valid , YAML]
              # yamllint enable rule:colons
              - bad  : colon and spaces  \s
              - [valid , YAML]
              """,
            conf,
            getLintProblem(4, 18, "trailing-spaces"),
            getLintProblem(9, 7, "colons"));
    }

    @Test
    void testDisableLineDirective() throws YamlLintConfigException {
        YamlLintConfig conf = getDefaultConf();

        check("""
              ---
              - [valid , YAML]
              - trailing spaces   \s
              # yamllint disable-line
              - bad   : colon
              - [valid , YAML]
              - bad  : colon and spaces  \s
              - [valid , YAML]
              """,
            conf,
            getLintProblem(3, 18, "trailing-spaces"),
            getLintProblem(7, 7, "colons"),
            getLintProblem(7, 26, "trailing-spaces"));
        check("""
              ---
              - [valid , YAML]
              - trailing spaces   \s
              - bad   : colon  # yamllint disable-line
              - [valid , YAML]
              - bad  : colon and spaces  \s
              - [valid , YAML]
              """,
            conf,
            getLintProblem(3, 18, "trailing-spaces"),
            getLintProblem(6, 7, "colons"),
            getLintProblem(6, 26, "trailing-spaces"));
        check("""
              ---
              - [valid , YAML]
              - trailing spaces   \s
              - bad   : colon
              - [valid , YAML]  # yamllint disable-line
              - bad  : colon and spaces  \s
              - [valid , YAML]
              """,
            conf,
            getLintProblem(3, 18, "trailing-spaces"),
            getLintProblem(4, 8, "colons"),
            getLintProblem(6, 7, "colons"),
            getLintProblem(6, 26, "trailing-spaces"));
    }

    @Test
    void testDisableLineDirectiveWithRules() throws YamlLintConfigException {
        YamlLintConfig conf = getDefaultConf();

        check("""
              ---
              - [valid , YAML]
              # yamllint disable-line rule:colons
              - trailing spaces   \s
              - bad   : colon
              - [valid , YAML]
              - bad  : colon and spaces  \s
              - [valid , YAML]
              """,
            conf,
            getLintProblem(4, 18, "trailing-spaces"),
            getLintProblem(5, 8, "colons"),
            getLintProblem(7, 7, "colons"),
            getLintProblem(7, 26, "trailing-spaces"));
        check("""
              ---
              - [valid , YAML]
              - trailing spaces  # yamllint disable-line rule:colons \s
              - bad   : colon
              - [valid , YAML]
              - bad  : colon and spaces  \s
              - [valid , YAML]
              """,
            conf,
            getLintProblem(3, 55, "trailing-spaces"),
            getLintProblem(4, 8, "colons"),
            getLintProblem(6, 7, "colons"),
            getLintProblem(6, 26, "trailing-spaces"));
        check("""
              ---
              - [valid , YAML]
              - trailing spaces   \s
              # yamllint disable-line rule:colons
              - bad   : colon
              - [valid , YAML]
              - bad  : colon and spaces  \s
              - [valid , YAML]
              """,
            conf,
            getLintProblem(3, 18, "trailing-spaces"),
            getLintProblem(7, 7, "colons"),
            getLintProblem(7, 26, "trailing-spaces"));
        check("""
              ---
              - [valid , YAML]
              - trailing spaces   \s
              - bad   : colon  # yamllint disable-line rule:colons
              - [valid , YAML]
              - bad  : colon and spaces  \s
              - [valid , YAML]
              """,
            conf,
            getLintProblem(3, 18, "trailing-spaces"),
            getLintProblem(6, 7, "colons"),
            getLintProblem(6, 26, "trailing-spaces"));
        check("""
              ---
              - [valid , YAML]
              - trailing spaces   \s
              - bad   : colon
              - [valid , YAML]
              # yamllint disable-line rule:colons
              - bad  : colon and spaces  \s
              - [valid , YAML]
              """,
            conf,
            getLintProblem(3, 18, "trailing-spaces"),
            getLintProblem(4, 8, "colons"),
            getLintProblem(7, 26, "trailing-spaces"));
        check("""
              ---
              - [valid , YAML]
              - trailing spaces   \s
              - bad   : colon
              - [valid , YAML]
              # yamllint disable-line rule:colons rule:trailing-spaces
              - bad  : colon and spaces  \s
              - [valid , YAML]
              """,
            conf,
            getLintProblem(3, 18, "trailing-spaces"),
            getLintProblem(4, 8, "colons"));
    }

    @Test
    void testDirectiveOnLastLine() throws YamlLintConfigException {
        YamlLintConfig conf = getConf("new-line-at-end-of-file: {}");

        check("---\n" +
                "no new line",
            conf,
            getLintProblem(2, 12, "new-line-at-end-of-file"));
        check("""
              ---
              # yamllint disable
              no new line""",
            conf);
        check("---\n" +
                "no new line  # yamllint disable",
            conf);
    }

    @Test
    void testIndentedDirective() throws YamlLintConfigException {
        YamlLintConfig conf = getConf("brackets: {min-spaces-inside: 0, max-spaces-inside: 0}");

        check("""
              ---
              - a: 1
                b:
                  c: [    x]
              """,
            conf,
            getLintProblem(4, 12, "brackets"));
        check("""
              ---
              - a: 1
                b:
                  # yamllint disable-line rule:brackets
                  c: [    x]
              """,
            conf);
    }

    @Test
    void testDirectiveOnItself() throws YamlLintConfigException {
        YamlLintConfig conf = getConf("comments: {min-spaces-from-content: 2}\n",
                "comments-indentation: {}\n");

        check("""
              ---
              - a: 1 # comment too close
                b:
               # wrong indentation
                  c: [x]
              """,
            conf,
            getLintProblem(2, 8, "comments"),
            getLintProblem(4, 2, "comments-indentation"));
        check("""
              ---
              # yamllint disable
              - a: 1 # comment too close
                b:
               # wrong indentation
                  c: [x]
              """,
            conf);
        check("""
              ---
              - a: 1 # yamllint disable-line
                b:
                  # yamllint disable-line
               # wrong indentation
                  c: [x]
              """,
            conf);
        check("""
              ---
              - a: 1 # yamllint disable-line rule:comments
                b:
                  # yamllint disable-line rule:comments-indentation
               # wrong indentation
                  c: [x]
              """,
            conf);
        check("""
              ---
              # yamllint disable
              - a: 1 # comment too close
                # yamllint enable rule:comments-indentation
                b:
               # wrong indentation
                  c: [x]
              """,
            conf,
            getLintProblem(6, 2, "comments-indentation"));
    }


    private YamlLintConfig getConf(String... rules) throws YamlLintConfigException {
        StringBuilder sb = new StringBuilder("---\nextends: default\nrules:\n");

        if (rules != null) {
            for (String rule : rules) {
                sb.append("  ").append(rule);
            }
        }

        return new YamlLintConfig(sb.toString());
    }

    private YamlLintConfig getDefaultConf() throws YamlLintConfigException {
        return getConf("commas: disable\n",
                "trailing-spaces: {}\n",
                "colons: {max-spaces-before: 1}\n");
    }
}
