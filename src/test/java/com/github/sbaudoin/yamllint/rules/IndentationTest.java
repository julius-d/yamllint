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

class IndentationTest extends RuleTester {
    @Test
    void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: disable");
        check("""
              ---
              object:
                 k1: v1
              obj2:
               k2:
                   - 8
               k3:
                         val
              ...
              """, conf);
        check("""
              ---
                o:
                  k1: v1
                p:
                 k3:
                     val
              ...
              """, conf);
        check("""
              ---
                   - o:
                       k1: v1
                   - p: kdjf
                   - q:
                      k3:
                            - val
              ...
              """, conf);
    }

    @Test
    void testOneSpace() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: 1, indent-sequences: false, check-multi-line-strings: false}");
        check("""
              ---
              object:
               k1:
               - a
               - b
               k2: v2
               k3:
               - name: Unix
                 date: 1969
               - name: Linux
                 date: 1991
              ...
              """, conf);

        conf = getConfig("indentation: {spaces: 1, indent-sequences: true, check-multi-line-strings: false}");
        check("""
              ---
              object:
               k1:
                - a
                - b
               k2: v2
               k3:
                - name: Unix
                  date: 1969
                - name: Linux
                  date: 1991
              ...
              """, conf);
    }

    @Test
    void testTwoSpaces() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: 2, indent-sequences: false, check-multi-line-strings: false}");
        check("""
              ---
              object:
                k1:
                - a
                - b
                k2: v2
                k3:
                - name: Unix
                  date: 1969
                - name: Linux
                  date: 1991
                k4:
                -
                k5: v3
              ...
              """, conf);

        conf = getConfig("indentation: {spaces: 2, indent-sequences: true, check-multi-line-strings: false}");
        check("""
              ---
              object:
                k1:
                  - a
                  - b
                k2: v2
                k3:
                  - name: Unix
                    date: 1969
                  - name: Linux
                    date: 1991
              ...
              """, conf);
    }

    @Test
    void testThreeSpaces() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: 3, indent-sequences: false, check-multi-line-strings: false}");
        check("""
              ---
              object:
                 k1:
                 - a
                 - b
                 k2: v2
                 k3:
                 - name: Unix
                   date: 1969
                 - name: Linux
                   date: 1991
              ...
              """, conf);

        conf = getConfig("indentation: {spaces: 3, indent-sequences: true, check-multi-line-strings: false}");
        check("""
              ---
              object:
                 k1:
                    - a
                    - b
                 k2: v2
                 k3:
                    - name: Unix
                      date: 1969
                    - name: Linux
                      date: 1991
              ...
              """, conf);
    }

    @Test
    void testConsistentSpaces() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: whatever,",
                "              check-multi-line-strings: false}",
                "document-start: disable");
        check("""
              ---
              object:
               k1:
                - a
                - b
               k2: v2
               k3:
                - name: Unix
                  date: 1969
                - name: Linux
                  date: 1991
              ...
              """, conf);
        check("""
              ---
              object:
                k1:
                - a
                - b
                k2: v2
                k3:
                - name: Unix
                  date: 1969
                - name: Linux
                  date: 1991
              ...
              """, conf);
        check("""
              ---
              object:
                 k1:
                    - a
                    - b
                 k2: v2
                 k3:
                    - name: Unix
                      date: 1969
                    - name: Linux
                      date: 1991
              ...
              """, conf);
        check("""
              first is not indented:
                value is indented
              """, conf);
        check("""
              first is not indented:
                   value:
                        is indented
              """, conf);
        check("""
              - first is already indented:
                  value is indented too
              """, conf);
        check("""
              - first is already indented:
                     value:
                          is indented too
              """, conf);
        check("""
              - first is already indented:
                     value:
                           is indented too
              """, conf, getLintProblem(3, 14));
        check("""
              ---
              list one:
                - 1
                - 2
                - 3
              list two:
                  - a
                  - b
                  - c
              """, conf, getLintProblem(7, 5));
        check("""
              ---
              list one:
              - 1
              - 2
              - 3
              list two:
                - a
                - b
                - c
              """, conf);
        check("""
              ---
              list one:
               - 1
               - 2
               - 3
              list two:
              - a
              - b
              - c
              """, conf);
    }

    @Test
    void testConsistentSpacesAndIndentSequences() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}");
        check("""
              ---
              list one:
              - 1
              - 2
              - 3
              list two:
                  - a
                  - b
                  - c
              """, conf, getLintProblem(3, 1));
        check("""
              ---
              list one:
                - 1
                - 2
                - 3
              list two:
                  - a
                  - b
                  - c
              """, conf, getLintProblem(7, 5));
        check("""
              ---
              list one:
                - 1
                - 2
                - 3
              list two:
              - a
              - b
              - c
              """, conf, getLintProblem(7, 1));

        conf = getConfig("indentation: {spaces: consistent, indent-sequences: false, check-multi-line-strings: false}");
        check("""
              ---
              list one:
              - 1
              - 2
              - 3
              list two:
                  - a
                  - b
                  - c
              """, conf, getLintProblem(7, 5));
        check("""
              ---
              list one:
              - 1
              - 2
              - 3
              list two:
                - a
                - b
                - c
              """, conf, getLintProblem(7, 3));
        check("""
              ---
              list one:
                - 1
                - 2
                - 3
              list two:
              - a
              - b
              - c
              """, conf, getLintProblem(3, 3));

        conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: consistent,",
                "              check-multi-line-strings: false}");
        check("""
              ---
              list one:
              - 1
              - 2
              - 3
              list two:
                  - a
                  - b
                  - c
              """, conf, getLintProblem(7, 5));
        check("""
              ---
              list one:
                  - 1
                  - 2
                  - 3
              list two:
              - a
              - b
              - c
              """, conf, getLintProblem(7, 1));
        check("""
              ---
              list one:
              - 1
              - 2
              - 3
              list two:
              - a
              - b
              - c
              """, conf);
        check("""
              ---
              list one:
                - 1
                - 2
                - 3
              list two:
                  - a
                  - b
                  - c
              """, conf, getLintProblem(7, 5));

        conf = getConfig("indentation: {spaces: consistent, indent-sequences: whatever, check-multi-line-strings: false}");
        check("""
              ---
              list one:
              - 1
              - 2
              - 3
              list two:
                  - a
                  - b
                  - c
              """, conf);
        check("""
              ---
              list one:
                  - 1
                  - 2
                  - 3
              list two:
              - a
              - b
              - c
              """, conf);
        check("""
              ---
              list one:
              - 1
              - 2
              - 3
              list two:
              - a
              - b
              - c
              """, conf);
        check("""
              ---
              list one:
                - 1
                - 2
                - 3
              list two:
                  - a
                  - b
                  - c
              """, conf, getLintProblem(7, 5));
    }

    @Test
    void testIndentSequencesWhatever() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: 4, indent-sequences: whatever, check-multi-line-strings: false}");
        check("""
              ---
              list one:
              - 1
              - 2
              - 3
              list two:
                  - a
                  - b
                  - c
              """, conf);
        check("""
              ---
              list one:
                - 1
                - 2
                - 3
              list two:
                  - a
                  - b
                  - c
              """, conf, getLintProblem(3, 3));
        check("""
              ---
              list one:
              - 1
              - 2
              - 3
              list two:
                - a
                - b
                - c
              """, conf, getLintProblem(7, 3));
        check("""
              ---
              list:
                  - 1
                  - 2
                  - 3
              - a
              - b
              - c
              """, conf, getSyntaxError(6, 1));
    }

    @Test
    void testIndentSequencesConsistent() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: 4, indent-sequences: consistent, check-multi-line-strings: false}");
        check("""
              ---
              list one:
              - 1
              - 2
              - 3
              list:
                  two:
                  - a
                  - b
                  - c
              """, conf);
        check("""
              ---
              list one:
                  - 1
                  - 2
                  - 3
              list:
                  two:
                      - a
                      - b
                      - c
              """, conf);
        check("""
              ---
              list one:
              - 1
              - 2
              - 3
              list two:
                  - a
                  - b
                  - c
              """, conf, getLintProblem(7, 5));
        check("""
              ---
              list one:
                  - 1
                  - 2
                  - 3
              list two:
              - a
              - b
              - c
              """, conf, getLintProblem(7, 1));
        check("""
              ---
              list one:
               - 1
               - 2
               - 3
              list two:
              - a
              - b
              - c
              """, conf, getLintProblem(3, 2), getLintProblem(7, 1));
    }

    @Test
    void testDirectFlows() throws YamlLintConfigException {
        // flow: [ ...
        // ]
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}");
        check("""
              ---
              a: {x: 1,
                  y,
                  z: 1}
              """, conf);
        check("""
              ---
              a: {x: 1,
                 y,
                  z: 1}
              """, conf, getLintProblem(3, 4));
        check("""
              ---
              a: {x: 1,
                   y,
                  z: 1}
              """, conf, getLintProblem(3, 6));
        check("""
              ---
              a: {x: 1,
                y, z: 1}
              """, conf, getLintProblem(3, 3));
        check("""
              ---
              a: {x: 1,
                  y, z: 1
              }
              """, conf);
        check("""
              ---
              a: {x: 1,
                y, z: 1
              }
              """, conf, getLintProblem(3, 3));
        check("""
              ---
              a: [x,
                  y,
                  z]
              """, conf);
        check("""
              ---
              a: [x,
                 y,
                  z]
              """, conf, getLintProblem(3, 4));
        check("""
              ---
              a: [x,
                   y,
                  z]
              """, conf, getLintProblem(3, 6));
        check("""
              ---
              a: [x,
                y, z]
              """, conf, getLintProblem(3, 3));
        check("""
              ---
              a: [x,
                  y, z
              ]
              """, conf);
        check("""
              ---
              a: [x,
                y, z
              ]
              """, conf, getLintProblem(3, 3));
    }

    @Test
    void testBrokenFlows() throws YamlLintConfigException {
        // flow: [
        //   ...
        // ]
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}");
        check("""
              ---
              a: {
                x: 1,
                y, z: 1
              }
              """, conf);
        check("""
              ---
              a: {
                x: 1,
                y, z: 1}
              """, conf);
        check("""
              ---
              a: {
                 x: 1,
                y, z: 1
              }
              """, conf, getLintProblem(4, 3));
        check("""
              ---
              a: {
                x: 1,
                y, z: 1
                }
              """, conf, getLintProblem(5, 3));
        check("""
              ---
              a: [
                x,
                y, z
              ]
              """, conf);
        check("""
              ---
              a: [
                x,
                y, z]
              """, conf);
        check("""
              ---
              a: [
                 x,
                y, z
              ]
              """, conf, getLintProblem(4, 3));
        check("""
              ---
              a: [
                x,
                y, z
                ]
              """, conf, getLintProblem(5, 3));
        check("""
              ---
              obj: {
                a: 1,
                 b: 2,
               c: 3
              }
              """, conf, getLintProblem(4, 4), getLintProblem(5, 2));
        check("""
              ---
              list: [
                1,
                 2,
               3
              ]
              """, conf, getLintProblem(4, 4), getLintProblem(5, 2));
        check("""
              ---
              top:
                rules: [
                  1, 2,
                ]
              """, conf);
        check("""
              ---
              top:
                rules: [
                  1, 2,
              ]
                rulez: [
                  1, 2,
                  ]
              """, conf, getLintProblem(5, 1), getLintProblem(8, 5));
        check("""
              ---
              top:
                rules:
                  here: {
                    foo: 1,
                    bar: 2
                  }
              """, conf);
        check("""
              ---
              top:
                rules:
                  here: {
                    foo: 1,
                    bar: 2
                    }
                  there: {
                    foo: 1,
                    bar: 2
                }
              """, conf, getLintProblem(7, 7), getLintProblem(11, 3));

        conf = getConfig("indentation: {spaces: 2, indent-sequences: true, check-multi-line-strings: false}");
        check("""
              ---
              a: {
                 x: 1,
                y, z: 1
              }
              """, conf, getLintProblem(3, 4));
        check("""
              ---
              a: [
                 x,
                y, z
              ]
              """, conf, getLintProblem(3, 4));
    }

    @Test
    void testClearedFlows() throws YamlLintConfigException {
        // flow:
        //   [
        //     ...
        //   ]
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}");
        check("""
              ---
              top:
                rules:
                  {
                    foo: 1,
                    bar: 2
                  }
              """, conf);
        check("""
              ---
              top:
                rules:
                  {
                     foo: 1,
                    bar: 2
                  }
              """, conf, getLintProblem(5, 8));
        check("""
              ---
              top:
                rules:
                 {
                   foo: 1,
                   bar: 2
                 }
              """, conf, getLintProblem(4, 4));
        check("""
              ---
              top:
                rules:
                  {
                    foo: 1,
                    bar: 2
                 }
              """, conf, getLintProblem(7, 4));
        check("""
              ---
              top:
                rules:
                  {
                    foo: 1,
                    bar: 2
                   }
              """, conf, getLintProblem(7, 6));
        check("""
              ---
              top:
                [
                  a, b, c
                ]
              """, conf);
        check("""
              ---
              top:
                [
                   a, b, c
                ]
              """, conf, getLintProblem(4, 6));
        check("""
              ---
              top:
                 [
                   a, b, c
                 ]
              """, conf, getLintProblem(4, 6));
        check("""
              ---
              top:
                [
                  a, b, c
                 ]
              """, conf, getLintProblem(5, 4));
        check("""
              ---
              top:
                rules: [
                  {
                    foo: 1
                  },
                  {
                    foo: 2,
                    bar: [
                      a, b, c
                    ],
                  },
                ]
              """, conf);
        check("""
              ---
              top:
                rules: [
                  {
                   foo: 1
                   },
                  {
                    foo: 2,
                      bar: [
                        a, b, c
                    ],
                  },
              ]
              """, conf, getLintProblem(5, 6), getLintProblem(6, 6),
                getLintProblem(9, 9), getLintProblem(11, 7), getLintProblem(13, 1));
    }

    @Test
    void testUnderIndented() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: 2, indent-sequences: consistent, check-multi-line-strings: false}");
        check("""
              ---
              object:
               val: 1
              ...
              """, conf, getLintProblem(3, 2));
        check("""
              ---
              object:
                k1:
                 - a
              ...
              """, conf, getLintProblem(4, 4));
        check("""
              ---
              object:
                k3:
                  - name: Unix
                   date: 1969
              ...
              """, conf, getSyntaxError(5, 6));

        conf = getConfig("indentation: {spaces: 4, indent-sequences: consistent, check-multi-line-strings: false}");
        check("""
              ---
              object:
                 val: 1
              ...
              """, conf, getLintProblem(3, 4));
        check("""
              ---
              - el1
              - el2:
                 - subel
              ...
              """, conf, getLintProblem(4, 4));
        check("""
              ---
              object:
                  k3:
                      - name: Linux
                       date: 1991
              ...
              """, conf, getSyntaxError(5, 10));

        conf = getConfig("indentation: {spaces: 2, indent-sequences: true, check-multi-line-strings: false}");
        check("---\n" +
                "a:\n" +
                "-\n" +  // empty list
               "b: c\n" +
               "...\n", conf, getLintProblem(3, 1));

        conf = getConfig("indentation: {spaces: 2, indent-sequences: consistent, check-multi-line-strings: false}");
        check("---\n" +
                "a:\n" +
                "  -\n" +  // empty list
               "b:\n" +
               "-\n" +
               "c: d\n" +
               "...\n", conf, getLintProblem(5, 1));
    }

    @Test
    void testOverIndented() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: 2, indent-sequences: consistent, check-multi-line-strings: false}");
        check("""
              ---
              object:
                 val: 1
              ...
              """, conf, getLintProblem(3, 4));
        check("""
              ---
              object:
                k1:
                   - a
              ...
              """, conf, getLintProblem(4, 6));
        check("""
              ---
              object:
                k3:
                  - name: Unix
                     date: 1969
              ...
              """, conf, getSyntaxError(5, 12));

        conf = getConfig("indentation: {spaces: 4, indent-sequences: consistent, check-multi-line-strings: false}");
        check("""
              ---
              object:
                   val: 1
              ...
              """, conf, getLintProblem(3, 6));
        check("""
              ---
               object:
                   val: 1
              ...
              """, conf, getLintProblem(2, 2));
        check("""
              ---
              - el1
              - el2:
                   - subel
              ...
              """, conf, getLintProblem(4, 6));
        check("""
              ---
              - el1
              - el2:
                            - subel
              ...
              """, conf, getLintProblem(4, 15));
        check("""
              ---
                - el1
                - el2:
                      - subel
              ...
              """, conf,
                getLintProblem(2, 3));
        check("""
              ---
              object:
                  k3:
                      - name: Linux
                         date: 1991
              ...
              """, conf, getSyntaxError(5, 16));

        conf = getConfig("indentation: {spaces: 4, indent-sequences: whatever, check-multi-line-strings: false}");
        check("""
              ---
                - el1
                - el2:
                  - subel
              ...
              """, conf,
                getLintProblem(2, 3));

        conf = getConfig("indentation: {spaces: 2, indent-sequences: false, check-multi-line-strings: false}");
        check("---\n" +
                "a:\n" +
                "  -\n" +  // empty list
               "b: c\n" +
               "...\n", conf, getLintProblem(3, 3));

        conf = getConfig("indentation: {spaces: 2, indent-sequences: consistent, check-multi-line-strings: false}");
        check("---\n" +
                "a:\n" +
                "-\n" +  // empty list
               "b:\n" +
               "  -\n" +
               "c: d\n" +
               "...\n", conf, getLintProblem(5, 3));
    }

    @Test
    void testMultiLines() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}");
        check("""
              ---
              long_string: >
                bla bla blah
                blah bla bla
              ...
              """, conf);
        check("""
              ---
              - long_string: >
                  bla bla blah
                  blah bla bla
              ...
              """, conf);
        check("""
              ---
              obj:
                - long_string: >
                    bla bla blah
                    blah bla bla
              ...
              """, conf);
    }

    @Test
    void testEmptyValue() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}");
        check("""
              ---
              key1:
              key2: not empty
              key3:
              ...
              """, conf);
        check("""
              ---
              -
              - item 2
              -
              ...
              """, conf);
    }

    @Test
    void testNestedCollections() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: 2, indent-sequences: true, check-multi-line-strings: false}");
        check("""
              ---
              - o:
                k1: v1
              ...
              """, conf);
        check("""
              ---
              - o:
               k1: v1
              ...
              """, conf, getSyntaxError(3, 2));
        check("""
              ---
              - o:
                 k1: v1
              ...
              """, conf, getLintProblem(3, 4));

        conf = getConfig("indentation: {spaces: 4, indent-sequences: true, check-multi-line-strings: false}");
        check("""
              ---
              - o:
                    k1: v1
              ...
              """, conf);
        check("""
              ---
              - o:
                   k1: v1
              ...
              """, conf, getLintProblem(3, 6));
        check("""
              ---
              - o:
                     k1: v1
              ...
              """, conf, getLintProblem(3, 8));
        check("""
              ---
              - - - - item
                  - elem 1
                  - elem 2
                  - - - - - very nested: a
                            key: value
              ...
              """, conf);
        check("""
              ---
               - - - - item
                   - elem 1
                   - elem 2
                   - - - - - very nested: a
                             key: value
              ...
              """, conf, getLintProblem(2, 2));
    }

    @Test
    void testNestedCollectionsWithSpacesConsistent() throws YamlLintConfigException {
        // Tests behavior of {spaces: consistent} in nested collections to
        // ensure wrong-indentation is properly caught--especially when the
        // expected indent value is initially unkown. For details, see
        // https://github.com/adrienverge/yamllint/issues/485.
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true}");
        check("""
              ---
              - item:
                - elem
              - item:
                  - elem
              ...
              """, conf, getLintProblem(3, 3));
        conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: false}");
        check("""
              ---
              - item:
                - elem
              - item:
                  - elem
              ...
              """, conf, getLintProblem(5, 5));
        conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: consistent}");
        check("""
              ---
              - item:
                - elem
              - item:
                  - elem
              ...
              """, conf, getLintProblem(5, 5));
        conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: whatever}");
        check("""
              ---
              - item:
                - elem
              - item:
                  - elem
              ...
              """, conf);
    }

    @Test
    void testReturn() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}");
        check("""
              ---
              a:
                b:
                  c:
                d:
                  e:
                    f:
              g:
              ...
              """, conf);
        check("""
              ---
              a:
                b:
                  c:
                 d:
              ...
              """, conf, getSyntaxError(5, 4));
        check("""
              ---
              a:
                b:
                  c:
               d:
              ...
              """, conf, getSyntaxError(5, 2));
    }

    @Test
    void testFirstLine() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}",
                "document-start: disable");
        check("  a: 1\n", conf, getLintProblem(1, 3));
    }

    @Test
    void testExplicitBlockMappings() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}");
        check("""
              ---
              object:
                  ? key
                  : value
              """, conf);
        check("""
              ---
              object:
                  ? key
                  :
                      value
              ...
              """, conf);
        check("""
              ---
              object:
                  ?
                      key
                  : value
              """, conf);
        check("""
              ---
              object:
                  ?
                      key
                  :
                      value
              ...
              """, conf);
        check("""
              ---
              - ? key
                : value
              """, conf);
        check("""
              ---
              - ? key
                :
                    value
              ...
              """, conf);
        check("""
              ---
              - ?
                    key
                : value
              """, conf);
        check("""
              ---
              - ?
                    key
                :
                    value
              ...
              """, conf);
        check("""
              ---
              object:
                  ? key
                  :
                     value
              ...
              """, conf, getLintProblem(5, 8));
        check("""
              ---
              - - ?
                     key
                  :
                    value
              ...
              """, conf, getLintProblem(5, 7));
        check("""
              ---
              object:
                  ?
                     key
                  :
                       value
              ...
              """, conf, getLintProblem(4, 8), getLintProblem(6, 10));
        check("""
              ---
              object:
                  ?
                       key
                  :
                     value
              ...
              """, conf, getLintProblem(4, 10), getLintProblem(6, 8));
    }

    @Test
    void testClearSequenceItem() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}");
        check("""
              ---
              -
                string
              -
                map: ping
              -
                - sequence
                -
                  nested
                -
                  >
                    multi
                    line
              ...
              """, conf);
        check("""
              ---
              -
               string
              -
                 string
              """, conf, getLintProblem(5, 4));
        check("""
              ---
              -
               map: ping
              -
                 map: ping
              """, conf, getLintProblem(5, 4));
        check("""
              ---
              -
               - sequence
              -
                 - sequence
              """, conf, getLintProblem(5, 4));
        check("""
              ---
              -
                -
                 nested
                -
                   nested
              """, conf, getLintProblem(4, 4), getLintProblem(6, 6));
        check("""
              ---
              -
                -
                   >
                    multi
                    line
              ...
              """, conf, getLintProblem(4, 6));

        conf = getConfig("indentation: {spaces: 2, indent-sequences: true, check-multi-line-strings: false}");
        check("""
              ---
              -
               string
              -
                 string
              """, conf, getLintProblem(3, 2), getLintProblem(5, 4));
        check("""
              ---
              -
               map: ping
              -
                 map: ping
              """, conf, getLintProblem(3, 2), getLintProblem(5, 4));
        check("""
              ---
              -
               - sequence
              -
                 - sequence
              """, conf, getLintProblem(3, 2), getLintProblem(5, 4));
        check("""
              ---
              -
                -
                 nested
                -
                   nested
              """, conf, getLintProblem(4, 4), getLintProblem(6, 6));
    }

    @Test
    void testAnchors() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}");
        check("""
              ---
              key: &anchor value
              """, conf);
        check("""
              ---
              key: &anchor
                value
              """, conf);
        check("""
              ---
              - &anchor value
              """, conf);
        check("""
              ---
              - &anchor
                value
              """, conf);
        check("""
              ---
              key: &anchor [1,
                            2]
              """, conf);
        check("""
              ---
              key: &anchor
                [1,
                 2]
              """, conf);
        check("""
              ---
              key: &anchor
                - 1
                - 2
              """, conf);
        check("""
              ---
              - &anchor [1,
                         2]
              """, conf);
        check("""
              ---
              - &anchor
                [1,
                 2]
              """, conf);
        check("""
              ---
              - &anchor
                - 1
                - 2
              """, conf);
        check("""
              ---
              key:
                &anchor1
                value
              """, conf);
        check("""
              ---
              pre:
                &anchor1 0
              &anchor2 key:
                value
              """, conf);
        check("""
              ---
              machine0:
                /etc/hosts: &ref-etc-hosts
                  content:
                    - 127.0.0.1: localhost
                    - ::1: localhost
                  mode: 0644
              machine1:
                /etc/hosts: *ref-etc-hosts
              """, conf);
        check("""
              ---
              list:
                - k: v
                - &a truc
                - &b
                  truc
                - k: *a
              """, conf);
    }

    @Test
    void testTags() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: true, check-multi-line-strings: false}");
        check("""
              ---
              -
                "flow in block"
              - >
                  Block scalar
              - !!map  # Block collection
                foo: bar
              """, conf);

        conf = getConfig("indentation: {spaces: consistent, indent-sequences: false, check-multi-line-strings: false}");
        check("""
              ---
              sequence: !!seq
              - entry
              - !!seq
                - nested
              """, conf);
        check("""
              ---
              mapping: !!map
                foo: bar
              Block style: !!map
                Clark: Evans
                Ingy: döt Net
                Oren: Ben-Kiki
              """, conf);
        check("""
              ---
              Flow style: !!map {Clark: Evans, Ingy: döt Net}
              Block style: !!seq
              - Clark Evans
              - Ingy döt Net
              """, conf);
    }

    @Test
    void testFlowsImbrication() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent, indent-sequences: false, check-multi-line-strings: false}");
        check("""
              ---
              [val]: value
              """, conf);
        check("""
              ---
              {key}: value
              """, conf);
        check("""
              ---
              {key: val}: value
              """, conf);
        check("""
              ---
              [[val]]: value
              """, conf);
        check("""
              ---
              {{key}}: value
              """, conf);
        check("""
              ---
              {{key: val1}: val2}: value
              """, conf);
        check("""
              ---
              - [val, {{key: val}: val}]: value
              - {[val,
                  {{key: val}: val}]}
              - {[val,
                  {{key: val,
                    key2}}]}
              - {{{{{moustaches}}}}}
              - {{{{{moustache,
                     moustache},
                    moustache}},
                  moustache}}
              """, conf);
        check("""
              ---
              - {[val,
                   {{key: val}: val}]}
              """,
                conf, getLintProblem(3, 6));
        check("""
              ---
              - {[val,
                  {{key: val,
                   key2}}]}
              """,
                conf, getLintProblem(4, 6));
        check("""
              ---
              - {{{{{moustache,
                     moustache},
                     moustache}},
                 moustache}}
              """,
                conf, getLintProblem(4, 8), getLintProblem(5, 4));
    }
}
