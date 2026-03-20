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
import org.junit.jupiter.api.Test;

class BracketsTest extends RuleTester {
    @Test
    void disabled() throws Exception {
        YamlLintConfig conf = getConfig("brackets: disable");
        check("""
              ---
              array1: []
              array2: [ ]
              array3: [   a, b]
              array4: [a, b, c ]
              array5: [a, b, c ]
              array6: [  a, b, c ]
              array7: [   a, b, c ]
              """, conf);
    }

    @Test
    void forbid() throws Exception {
        YamlLintConfig conf = getConfig("brackets:", "  forbid: false");
        check("""
              ---
              array: []
              """, conf);
        check("""
              ---
              array: [a, b]
              """, conf);
        check("""
              ---
              array: [
                a,
                b
              ]
              """, conf);

        conf = getConfig("brackets:", "  forbid: true");
        check("""
              ---
              array:
                - a
                - b
              """, conf);
        check("""
              ---
              array: []
              """, conf, getLintProblem(2, 9));
        check("""
              ---
              array: [a, b]
              """, conf, getLintProblem(2, 9));
        check("""
              ---
              array: [
                a,
                b
              ]
              """, conf, getLintProblem(2, 9));

        conf = getConfig("brackets:", "  forbid: non-empty");
        check("""
              ---
              array:
                - a
                - b
              """, conf);
        check("""
              ---
              array: []
              """, conf);
        check("""
              ---
              array: [
              
              ]
              """, conf);
        check("""
              ---
              array: [
              # a comment
              ]
              """, conf);
        check("""
              ---
              array: [a, b]
              """, conf, getLintProblem(2, 9));
        check("""
              ---
              array: [
                a,
                b
              ]
              """, conf, getLintProblem(2, 9));
    }

    @Test
    void minSpaces() throws Exception {
        YamlLintConfig conf = getConfig("brackets:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: 0",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("""
              ---
              array: []
              """, conf);

        conf = getConfig("brackets:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: 1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("""
              ---
              array: []
              """, conf, getLintProblem(2, 9));
        check("""
              ---
              array: [ ]
              """, conf);
        check("""
              ---
              array: [a, b]
              """, conf, getLintProblem(2, 9), getLintProblem(2, 13));
        check("""
              ---
              array: [ a, b ]
              """, conf);
        check("""
              ---
              array: [
                a,
                b
              ]
              """, conf);

        conf = getConfig("brackets:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: 3",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("""
              ---
              array: [ a, b ]
              """, conf,
                getLintProblem(2, 10), getLintProblem(2, 15));
        check("""
              ---
              array: [   a, b   ]
              """, conf);
    }

    @Test
    void maxSpaces() throws Exception {
        YamlLintConfig conf = getConfig("brackets:",
                "  max-spaces-inside: 0",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("""
              ---
              array: []
              """, conf);
        check("""
              ---
              array: [ ]
              """, conf, getLintProblem(2, 9));
        check("""
              ---
              array: [a, b]
              """, conf);
        check("""
              ---
              array: [ a, b ]
              """, conf,
                getLintProblem(2, 9), getLintProblem(2, 14));
        check("""
              ---
              array: [   a, b   ]
              """, conf,
                getLintProblem(2, 11), getLintProblem(2, 18));
        check("""
              ---
              array: [
                a,
                b
              ]
              """, conf);

        conf = getConfig("brackets:",
                "  max-spaces-inside: 3",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("""
              ---
              array: [   a, b   ]
              """, conf);
        check("""
              ---
              array: [    a, b     ]
              """, conf,
                getLintProblem(2, 12), getLintProblem(2, 21));
    }

    @Test
    void minAndMaxSpaces() throws Exception {
        YamlLintConfig conf = getConfig("brackets:",
                "  max-spaces-inside: 0",
                "  min-spaces-inside: 0",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("""
              ---
              array: []
              """, conf);
        check("""
              ---
              array: [ ]
              """, conf, getLintProblem(2, 9));
        check("""
              ---
              array: [   a, b]
              """, conf, getLintProblem(2, 11));

        conf = getConfig("brackets:",
                "  max-spaces-inside: 1",
                "  min-spaces-inside: 1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("""
              ---
              array: [a, b, c ]
              """, conf, getLintProblem(2, 9));

        conf = getConfig("brackets:",
                "  max-spaces-inside: 2",
                "  min-spaces-inside: 0",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("""
              ---
              array: [a, b, c ]
              """, conf);
        check("""
              ---
              array: [  a, b, c ]
              """, conf);
        check("""
              ---
              array: [   a, b, c ]
              """, conf, getLintProblem(2, 11));
    }

    @Test
    void minSpacesEmpty() throws Exception {
        YamlLintConfig conf = getConfig("brackets:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 0",
                "  min-spaces-inside-empty: 0");
        check("""
              ---
              array: []
              """, conf);

        conf = getConfig("brackets:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: 1");
        check("""
              ---
              array: []
              """, conf, getLintProblem(2, 9));
        check("""
              ---
              array: [ ]
              """, conf);

        conf = getConfig("brackets:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: 3");
        check("""
              ---
              array: []
              """, conf, getLintProblem(2, 9));
        check("""
              ---
              array: [   ]
              """, conf);
    }

    @Test
    void maxSpacesEmpty() throws Exception {
        YamlLintConfig conf = getConfig("brackets:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 0",
                "  min-spaces-inside-empty: -1");
        check("""
              ---
              array: []
              """, conf);
        check("""
              ---
              array: [ ]
              """, conf, getLintProblem(2, 9));

        conf = getConfig("brackets:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 1",
                "  min-spaces-inside-empty: -1");
        check("""
              ---
              array: []
              """, conf);
        check("""
              ---
              array: [ ]
              """, conf);
        check("""
              ---
              array: [  ]
              """, conf, getLintProblem(2, 10));

        conf = getConfig("brackets:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 3",
                "  min-spaces-inside-empty: -1");
        check("""
              ---
              array: []
              """, conf);
        check("""
              ---
              array: [   ]
              """, conf);
        check("""
              ---
              array: [    ]
              """, conf, getLintProblem(2, 12));
    }

    @Test
    void minAndMaxSpacesEmpty() throws Exception {
        YamlLintConfig conf = getConfig("brackets:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 2",
                "  min-spaces-inside-empty: 1");
        check("""
              ---
              array: []
              """, conf, getLintProblem(2, 9));
        check("""
              ---
              array: [ ]
              """, conf);
        check("""
              ---
              array: [  ]
              """, conf);
        check("""
              ---
              array: [   ]
              """, conf, getLintProblem(2, 11));
    }

    @Test
    void mixedEmptyNonempty() throws Exception {
        YamlLintConfig conf = getConfig("brackets:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: 1",
                "  max-spaces-inside-empty: 0",
                "  min-spaces-inside-empty: 0");
        check("""
              ---
              array: [ a, b ]
              """, conf);
        check("""
              ---
              array: [a, b]
              """, conf,
                getLintProblem(2, 9), getLintProblem(2, 13));
        check("""
              ---
              array: []
              """, conf);
        check("""
              ---
              array: [ ]
              """, conf,
                getLintProblem(2, 9));

        conf = getConfig("brackets:",
                "  max-spaces-inside: 0",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 1",
                "  min-spaces-inside-empty: 1");
        check("""
              ---
              array: [ a, b ]
              """, conf,
                getLintProblem(2, 9), getLintProblem(2, 14));
        check("""
              ---
              array: [a, b]
              """, conf);
        check("""
              ---
              array: []
              """, conf,
                getLintProblem(2, 9));
        check("""
              ---
              array: [ ]
              """, conf);

        conf = getConfig("brackets:",
                "  max-spaces-inside: 2",
                "  min-spaces-inside: 1",
                "  max-spaces-inside-empty: 1",
                "  min-spaces-inside-empty: 1");
        check("""
              ---
              array: [ a, b  ]
              """, conf);
        check("""
              ---
              array: [a, b   ]
              """, conf,
                getLintProblem(2, 9), getLintProblem(2, 15));
        check("""
              ---
              array: []
              """, conf,
                getLintProblem(2, 9));
        check("""
              ---
              array: [ ]
              """, conf);
        check("""
              ---
              array: [   ]
              """, conf,
                getLintProblem(2, 11));

        conf = getConfig("brackets:",
                "  max-spaces-inside: 1",
                "  min-spaces-inside: 1",
                "  max-spaces-inside-empty: 1",
                "  min-spaces-inside-empty: 1");
        check("""
              ---
              array: [ a, b ]
              """, conf);
        check("""
              ---
              array: [a, b]
              """, conf,
                getLintProblem(2, 9), getLintProblem(2, 13));
        check("""
                ---
                array: []""", conf,
                getLintProblem(2, 9));
        check("""
              ---
              array: [ ]
              """, conf);
    }
}
