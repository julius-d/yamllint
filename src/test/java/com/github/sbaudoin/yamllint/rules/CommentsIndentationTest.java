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

import com.github.sbaudoin.yamllint.YamlLintConfig;
import com.github.sbaudoin.yamllint.YamlLintConfigException;
import org.junit.jupiter.api.Test;

class CommentsIndentationTest extends RuleTester {
    @Test
    void testDisable() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments-indentation: disable");
        check("""
              ---
               # line 1
              # line 2
                # line 3
                # line 4
              
              obj:
               # these
                 # are
                # [good]
              # bad
                    # comments
                a: b
              
              obj1:
                a: 1
                # comments
              
              obj2:
                b: 2
              
              # empty
              #
              # comment
              ...
              """, conf);
    }

    @Test
    void testEnabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments-indentation: enable");
        check("""
              ---
              # line 1
              # line 2
              """, conf);
        check("""
              ---
               # line 1
              # line 2
              """, conf, getLintProblem(2, 2));
        check("""
              ---
                # line 1
                # line 2
              """, conf, getLintProblem(2, 3));
        check("""
              ---
              obj:
                # normal
                a: b
              """, conf);
        check("""
              ---
              obj:
               # bad
                a: b
              """, conf, getLintProblem(3, 2));
        check("""
              ---
              obj:
              # bad
                a: b
              """, conf, getLintProblem(3, 1));
        check("""
              ---
              obj:
                 # bad
                a: b
              """, conf, getLintProblem(3, 4));
        check("""
              ---
              obj:
               # these
                 # are
                # [good]
              # bad
                    # comments
                a: b
              """, conf,
                getLintProblem(3, 2), getLintProblem(4, 4),
                getLintProblem(6, 1), getLintProblem(7, 7));
        check("""
              ---
              obj1:
                a: 1
                # the following line is disabled
                # b: 2
              """, conf);
        check("""
              ---
              obj1:
                a: 1
                # b: 2
              
              obj2:
                b: 2
              """, conf);
        check("""
              ---
              obj1:
                a: 1
                # b: 2
              # this object is useless
              obj2: "no"
              """, conf);
        check("""
              ---
              obj1:
                a: 1
              # this object is useless
                # b: 2
              obj2: "no"
              """, conf, getLintProblem(5, 3));
        check("""
              ---
              obj1:
                a: 1
                # comments
                b: 2
              """, conf);
        check("""
              ---
              my list for today:
                - todo 1
                - todo 2
                # commented for now
                # - todo 3
              ...
              """, conf);
    }

    @Test
    void testFirstLine() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments-indentation: enable");
        check("# comment\n", conf);
        check("  # comment\n", conf, getLintProblem(1, 3));
    }

    @Test
    void testNoNewlineAtEnd() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments-indentation: enable",
                "new-line-at-end-of-file: disable");
        check("# comment", conf);
        check("  # comment", conf, getLintProblem(1, 3));
    }

    @Test
    void testEmptyComment() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments-indentation: enable");
        check("""
              ---
              # hey
              # normal
              #
              """, conf);
        check("""
              ---
              # hey
              # normal
               #
              """, conf, getLintProblem(4, 2));
    }

    @Test
    void testInlineComment() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments-indentation: enable");
        check("""
              ---
              - a  # inline
              # ok
              """, conf);
        check("""
              ---
              - a  # inline
               # not ok
              """, conf, getLintProblem(3, 2));
        check("""
              ---
               # not ok
              - a  # inline
              """, conf, getLintProblem(2, 2));
    }
}
