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

class BracesTest extends RuleTester {
    @Test
    void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("braces: disable");
        check("""
              ---
              dict1: {}
              dict2: { }
              dict3: {   a: 1, b}
              dict4: {a: 1, b, c: 3 }
              dict5: {a: 1, b, c: 3 }
              dict6: {  a: 1, b, c: 3 }
              dict7: {   a: 1, b, c: 3 }
              """, conf);
    }

    @Test
    void testForbid() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("braces:", "  forbid: false");
        check("""
              ---
              dict: {}
              """, conf);
        check("""
              ---
              dict: {a}
              """, conf);
        check("""
              ---
              dict: {a: 1}
              """, conf);
        check("""
              ---
              dict: {
                a: 1
              }
              """, conf);

        conf = getConfig("braces:", "  forbid: true");
        check("""
              ---
              dict:
                a: 1
              """, conf);
        check("""
              ---
              dict: {}
              """, conf, getLintProblem(2, 8));
        check("""
              ---
              dict: {a}
              """, conf, getLintProblem(2, 8));
        check("""
              ---
              dict: {a: 1}
              """, conf, getLintProblem(2, 8));
        check("""
              ---
              dict: {
                a: 1
              }
              """, conf, getLintProblem(2, 8));

        conf = getConfig("braces:", "  forbid: non-empty");
        check("""
              ---
              dict:
                a: 1
              """, conf);
        check("""
              ---
              dict: {}
              """, conf);
        check("""
              ---
              dict: {
              }
              """, conf);
        check("""
              ---
              dict: {
              # commented: value
              # another: value2
              }
              """, conf);
        check("""
              ---
              dict: {a}
              """, conf, getLintProblem(2, 8));
        check("""
              ---
              dict: {a: 1}
              """, conf, getLintProblem(2, 8));
        check("""
              ---
              dict: {
                a: 1
              }
              """, conf, getLintProblem(2, 8));
    }

    @Test
    void testMinSpaces() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("braces:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: 0",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("""
              ---
              dict: {}
              """, conf);

        conf = getConfig("braces:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: 1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("""
              ---
              dict: {}
              """, conf, getLintProblem(2, 8));
        check("""
              ---
              dict: { }
              """, conf);
        check("""
              ---
              dict: {a: 1, b}
              """, conf,
                getLintProblem(2, 8), getLintProblem(2, 15));
        check("""
              ---
              dict: { a: 1, b }
              """, conf);
        check("""
              ---
              dict: {
                a: 1,
                b
              }
              """, conf);

        conf = getConfig("braces:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: 3",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("""
              ---
              dict: { a: 1, b }
              """, conf,
                getLintProblem(2, 9), getLintProblem(2, 17));
        check("""
              ---
              dict: {   a: 1, b   }
              """, conf);
    }

    @Test
    void testMaxSpaces() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("braces:",
                "  max-spaces-inside: 0",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("""
              ---
              dict: {}
              """, conf);
        check("""
              ---
              dict: { }
              """, conf, getLintProblem(2, 8));
        check("""
              ---
              dict: {a: 1, b}
              """, conf);
        check("""
              ---
              dict: { a: 1, b }
              """, conf,
                getLintProblem(2, 8), getLintProblem(2, 16));
        check("""
              ---
              dict: {   a: 1, b   }
              """, conf,
                getLintProblem(2, 10), getLintProblem(2, 20));
        check("""
              ---
              dict: {
                a: 1,
                b
              }
              """, conf);

        conf = getConfig("braces:",
                "  max-spaces-inside: 3",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("""
              ---
              dict: {   a: 1, b   }
              """, conf);
        check("""
              ---
              dict: {    a: 1, b     }
              """, conf,
                getLintProblem(2, 11), getLintProblem(2, 23));
    }

    @Test
    void testMinAndMaxSpaces() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("braces:",
                "  max-spaces-inside: 0",
                "  min-spaces-inside: 0",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("""
              ---
              dict: {}
              """, conf);
        check("""
              ---
              dict: { }
              """, conf, getLintProblem(2, 8));
        check("""
              ---
              dict: {   a: 1, b}
              """, conf, getLintProblem(2, 10));

        conf = getConfig("braces:",
                "  max-spaces-inside: 1",
                "  min-spaces-inside: 1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("""
              ---
              dict: {a: 1, b, c: 3 }
              """, conf, getLintProblem(2, 8));

        conf = getConfig("braces:",
                "  max-spaces-inside: 2",
                "  min-spaces-inside: 0",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: -1");
        check("""
              ---
              dict: {a: 1, b, c: 3 }
              """, conf);
        check("""
              ---
              dict: {  a: 1, b, c: 3 }
              """, conf);
        check("""
              ---
              dict: {   a: 1, b, c: 3 }
              """, conf, getLintProblem(2, 10));
    }

    @Test
    void testMinSpacesEmpty() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("braces:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 0",
                "  min-spaces-inside-empty: 0");
        check("""
              ---
              array: {}
              """, conf);

        conf = getConfig("braces:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: 1");
        check("""
              ---
              array: {}
              """, conf, getLintProblem(2, 9));
        check("""
              ---
              array: { }
              """, conf);

        conf = getConfig("braces:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: -1",
                "  min-spaces-inside-empty: 3");
        check("""
              ---
              array: {}
              """, conf, getLintProblem(2, 9));
        check("""
              ---
              array: {   }
              """, conf);
    }

    @Test
    void testMaxSpacesEmpty() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("braces:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 0",
                "  min-spaces-inside-empty: -1");
        check("""
              ---
              array: {}
              """, conf);
        check("""
              ---
              array: { }
              """, conf, getLintProblem(2, 9));

        conf = getConfig("braces:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 1",
                "  min-spaces-inside-empty: -1");
        check("""
              ---
              array: {}
              """, conf);
        check("""
              ---
              array: { }
              """, conf);
        check("""
              ---
              array: {  }
              """, conf, getLintProblem(2, 10));

        conf = getConfig("braces:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 3",
                "  min-spaces-inside-empty: -1");
        check("""
              ---
              array: {}
              """, conf);
        check("""
              ---
              array: {   }
              """, conf);
        check("""
              ---
              array: {    }
              """, conf, getLintProblem(2, 12));
    }

    @Test
    void testMinAndMaxSpacesEmpty() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("braces:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 2",
                "  min-spaces-inside-empty: 1");
        check("""
              ---
              array: {}
              """, conf, getLintProblem(2, 9));
        check("""
              ---
              array: { }
              """, conf);
        check("""
              ---
              array: {  }
              """, conf);
        check("""
              ---
              array: {   }
              """, conf, getLintProblem(2, 11));
    }

    @Test
    void testMixedEmptyNonempty() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("braces:",
                "  max-spaces-inside: -1",
                "  min-spaces-inside: 1",
                "  max-spaces-inside-empty: 0",
                "  min-spaces-inside-empty: 0");
        check("""
              ---
              array: { a: 1, b }
              """, conf);
        check("""
              ---
              array: {a: 1, b}
              """, conf,
                getLintProblem(2, 9), getLintProblem(2, 16));
        check("""
              ---
              array: {}
              """, conf);
        check("""
              ---
              array: { }
              """, conf,
                getLintProblem(2, 9));

        conf = getConfig("braces:",
                "  max-spaces-inside: 0",
                "  min-spaces-inside: -1",
                "  max-spaces-inside-empty: 1",
                "  min-spaces-inside-empty: 1");
        check("""
              ---
              array: { a: 1, b }
              """, conf,
                getLintProblem(2, 9), getLintProblem(2, 17));
        check("""
              ---
              array: {a: 1, b}
              """, conf);
        check("""
              ---
              array: {}
              """, conf,
                getLintProblem(2, 9));
        check("""
              ---
              array: { }
              """, conf);

        conf = getConfig("braces:",
                "  max-spaces-inside: 2",
                "  min-spaces-inside: 1",
                "  max-spaces-inside-empty: 1",
                "  min-spaces-inside-empty: 1");
        check("""
              ---
              array: { a: 1, b  }
              """, conf);
        check("""
              ---
              array: {a: 1, b   }
              """, conf,
                getLintProblem(2, 9), getLintProblem(2, 18));
        check("""
              ---
              array: {}
              """, conf,
                getLintProblem(2, 9));
        check("""
              ---
              array: { }
              """, conf);
        check("""
              ---
              array: {   }
              """, conf,
                getLintProblem(2, 11));

        conf = getConfig("braces:",
                "  max-spaces-inside: 1",
                "  min-spaces-inside: 1",
                "  max-spaces-inside-empty: 1",
                "  min-spaces-inside-empty: 1");
        check("""
              ---
              array: { a: 1, b }
              """, conf);
        check("""
              ---
              array: {a: 1, b}
              """, conf,
                getLintProblem(2, 9), getLintProblem(2, 16));
        check("""
              ---
              array: {}
              """, conf,
                getLintProblem(2, 9));
        check("""
              ---
              array: { }
              """, conf);
    }
}
