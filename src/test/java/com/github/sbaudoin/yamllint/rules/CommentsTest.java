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

class CommentsTest extends RuleTester {
    @Test
    void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments: disable",
                "comments-indentation: disable");
        check("""
              ---
              #comment
              
              test: #    description
                - foo  # bar
                - hello #world
              
              # comment 2
              #comment 3
                #comment 3 bis
                #  comment 3 ter
              
              ################################
              ## comment 4
              ##comment 5
              
              string: "Une longue phrase." # this is French
              """, conf);
    }

    @Test
    void testStartingSpace() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments:",
                "  require-starting-space: true",
                "  min-spaces-from-content: -1",
                "comments-indentation: disable");
        check("""
              ---
              # comment
              
              test:  #     description
                - foo  #   bar
                - hello  # world
              
              # comment 2
              # comment 3
                #  comment 3 bis
                #  comment 3 ter
              
              ################################
              ## comment 4
              ##  comment 5
              """, conf);
        check("""
              ---
              #comment
              
              test:  #    description
                - foo  #  bar
                - hello  #world
              
              # comment 2
              #comment 3
                #comment 3 bis
                #  comment 3 ter
              
              ################################
              ## comment 4
              ##comment 5
              """, conf,
                getLintProblem(2, 2), getLintProblem(6, 13),
                getLintProblem(9, 2), getLintProblem(10, 4),
                getLintProblem(15, 3));
    }

    @Test
    void testShebang() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments:",
                "  require-starting-space: true",
                "  ignore-shebangs: false",
                "comments-indentation: disable",
                "document-start: disable");
        check("#!/bin/env my-interpreter\n", conf,
                getLintProblem(1, 2));
        check("""
              # comment
              #!/bin/env my-interpreter
              """, conf,
                getLintProblem(2, 2));
        check("""
              #!/bin/env my-interpreter
              ---
              #comment
              #!/bin/env my-interpreter
              """, conf,
                getLintProblem(1, 2),
                getLintProblem(3, 2),
                getLintProblem(4, 2));
        check("#! is a valid shebang too\n", conf, getLintProblem(1, 2));
        check("key:  #!/not/a/shebang\n", conf, getLintProblem(1, 8));
    }

    @Test
    void testIgnoreShebang() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments:",
                "  require-starting-space: true",
                "  ignore-shebangs: true",
                "comments-indentation: disable",
                "document-start: disable");
        check("#!/bin/env my-interpreter\n", conf);
        check("""
              # comment
              #!/bin/env my-interpreter
              """, conf,
                getLintProblem(2, 2));
        check("""
              #!/bin/env my-interpreter
              ---
              #comment
              #!/bin/env my-interpreter
              """, conf,
                getLintProblem(3, 2), getLintProblem(4, 2));
        check("#! is a valid shebang too\n", conf);
        check("key:  #!/not/a/shebang\n", conf, getLintProblem(1, 8));
    }

    @Test
    void testSpacesFromContent() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments:",
                "  require-starting-space: false",
                "  min-spaces-from-content: 2");
        check("""
              ---
              # comment
              
              test:  #    description
                - foo  #  bar
                - hello  #world
              
              string: "Une longue phrase."  # this is French
              """, conf);
        check("""
              ---
              # comment
              
              test: #    description
                - foo  # bar
                - hello #world
              
              string: "Une longue phrase." # this is French
              """, conf,
                getLintProblem(4, 7), getLintProblem(6, 11), getLintProblem(8, 30));
    }

    @Test
    void testBoth() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments:",
                "  require-starting-space: true",
                "  min-spaces-from-content: 2",
                "comments-indentation: disable");
        check("""
              ---
              #comment
              
              test: #    description
                - foo  # bar
                - hello #world
              
              # comment 2
              #comment 3
                #comment 3 bis
                #  comment 3 ter
              
              ################################
              ## comment 4
              ##comment 5
              
              string: "Une longue phrase." # this is French
              """, conf,
                getLintProblem(2, 2),
                getLintProblem(4, 7),
                getLintProblem(6, 11),
                getLintProblem(6, 12),
                getLintProblem(9, 2),
                getLintProblem(10, 4),
                getLintProblem(15, 3),
                getLintProblem(17, 30));
    }

    @Test
    void testEmptyComment() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments:",
                "  require-starting-space: true",
                "  min-spaces-from-content: 2");
        check("""
              ---
              # This is paragraph 1.
              #
              # This is paragraph 2.
              """, conf);
        check("""
              ---
              inline: comment  #
              foo: bar
              """, conf);
    }

    @Test
    void testFirstLine() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments:",
                    "  require-starting-space: true",
                    "  min-spaces-from-content: 2");
        check("# comment\n", conf);
    }

    @Test
    void testLastLine() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments:",
                "  require-starting-space: true",
                "  min-spaces-from-content: 2",
                "new-line-at-end-of-file: disable");
        check("# comment with no newline char:\n" +
                "#", conf);
    }

    @Test
    void testMultiLineScalar() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("comments:",
                "  require-starting-space: true",
                "  min-spaces-from-content: 2",
                "trailing-spaces: disable");
        check("""
              ---
              string: >
                this is plain text
              
              # comment
              """, conf);
        check("""
              ---
              - string: >
                  this is plain text
               \s
                # comment
              """, conf);
    }
}
