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

class CommasTest extends RuleTester {
    @Test
    void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("commas: disable");
        check("""
              ---
              dict: {a: b ,   c: "1 2 3",    d: e , f: [g,      h]}
              array: [
                elem  ,
                key: val ,
              ]
              map: {
                key1: val1 ,
                key2: val2,
              }
              ...
              """, conf);
        check("""
              ---
              - [one, two , three,four]
              - {five,six , seven, eight}
              - [
                nine,  ten
                , eleven
                ,twelve
              ]
              - {
                thirteen: 13,  fourteen
                , fifteen: 15
                ,sixteen: 16
              }
              """, conf);
    }

    @Test
    void testBeforeMax() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("commas:",
                "  max-spaces-before: 0",
                "  min-spaces-after: 0",
                "  max-spaces-after: -1");
        check("""
              ---
              array: [1, 2,  3, 4]
              ...
              """, conf);
        check("""
              ---
              array: [1, 2 ,  3, 4]
              ...
              """, conf, getLintProblem(2, 13));
        check("""
              ---
              array: [1 , 2,  3      , 4]
              ...
              """, conf, getLintProblem(2, 10), getLintProblem(2, 23));
        check("""
              ---
              dict: {a: b, c: "1 2 3", d: e,  f: [g, h]}
              ...
              """, conf);
        check("""
              ---
              dict: {a: b, c: "1 2 3" , d: e,  f: [g, h]}
              ...
              """, conf, getLintProblem(2, 24));
        check("""
              ---
              dict: {a: b , c: "1 2 3", d: e,  f: [g    , h]}
              ...
              """, conf, getLintProblem(2, 12), getLintProblem(2, 42));
        check("""
              ---
              array: [
                elem,
                key: val,
              ]
              """, conf);
        check("""
              ---
              array: [
                elem ,
                key: val,
              ]
              """, conf, getLintProblem(3, 7));
        check("""
              ---
              map: {
                key1: val1,
                key2: val2,
              }
              """, conf);
        check("""
              ---
              map: {
                key1: val1,
                key2: val2 ,
              }
              """, conf, getLintProblem(4, 13));
    }

    @Test
    void testBeforeMaxWithCommaOnNewLine() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("commas:",
                "  max-spaces-before: 0",
                "  min-spaces-after: 0",
                "  max-spaces-after: -1");
        check("""
              ---
              flow-seq: [1, 2, 3
                         , 4, 5, 6]
              ...
              """, conf, getLintProblem(3, 11));
        check("""
              ---
              flow-map: {a: 1, b: 2
                         , c: 3}
              ...
              """, conf, getLintProblem(3, 11));

        conf = getConfig("commas:",
                "  max-spaces-before: 0",
                "  min-spaces-after: 0",
                "  max-spaces-after: -1",
                "indentation: disable");
        check("""
              ---
              flow-seq: [1, 2, 3
                       , 4, 5, 6]
              ...
              """, conf, getLintProblem(3, 9));
        check("""
              ---
              flow-map: {a: 1, b: 2
                       , c: 3}
              ...
              """, conf, getLintProblem(3, 9));
        check("""
              ---
              [
              1,
              2
              , 3
              ]
              """, conf, getLintProblem(5, 1));
        check("""
              ---
              {
              a: 1,
              b: 2
              , c: 3
              }
              """, conf, getLintProblem(5, 1));
    }

    @Test
    void testBeforeMax3() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("commas:",
                "  max-spaces-before: 3",
                "  min-spaces-after: 0",
                "  max-spaces-after: -1");
        check("""
              ---
              array: [1 , 2, 3   , 4]
              ...
              """, conf);
        check("""
              ---
              array: [1 , 2, 3    , 4]
              ...
              """, conf, getLintProblem(2, 20));
        check("""
              ---
              array: [
                elem1   ,
                elem2    ,
                key: val,
              ]
              """, conf, getLintProblem(4, 11));
    }

    @Test
    void testAfterMin() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("commas:",
                "  max-spaces-before: -1",
                "  min-spaces-after: 1",
                "  max-spaces-after: -1");
        check("""
              ---
              - [one, two , three,four]
              - {five,six , seven, eight}
              - [
                nine,  ten
                , eleven
                ,twelve
              ]
              - {
                thirteen: 13,  fourteen
                , fifteen: 15
                ,sixteen: 16
              }
              """, conf,
                getLintProblem(2, 21), getLintProblem(3, 9),
                getLintProblem(7, 4), getLintProblem(12, 4));
    }

    @Test
    void testAfterMax() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("commas:",
                "  max-spaces-before: -1",
                "  min-spaces-after: 0",
                "  max-spaces-after: 1");
        check("""
              ---
              array: [1, 2, 3, 4]
              ...
              """, conf);
        check("""
              ---
              array: [1, 2,  3, 4]
              ...
              """, conf, getLintProblem(2, 15));
        check("""
              ---
              array: [1,  2, 3,     4]
              ...
              """, conf, getLintProblem(2, 12), getLintProblem(2, 22));
        check("""
              ---
              dict: {a: b , c: "1 2 3", d: e, f: [g, h]}
              ...
              """, conf);
        check("""
              ---
              dict: {a: b , c: "1 2 3",  d: e, f: [g, h]}
              ...
              """, conf, getLintProblem(2, 27));
        check("""
              ---
              dict: {a: b ,  c: "1 2 3", d: e, f: [g,     h]}
              ...
              """, conf, getLintProblem(2, 15), getLintProblem(2, 44));
        check("""
              ---
              array: [
                elem,
                key: val,
              ]
              """, conf);
        check("""
              ---
              array: [
                elem,  key: val,
              ]
              """, conf, getLintProblem(3, 9));
        check("""
              ---
              map: {
                key1: val1,   key2: [val2,  val3]
              }
              """, conf, getLintProblem(3, 16), getLintProblem(3, 30));
    }

    @Test
    void testAfterMax3() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("commas:",
                "  max-spaces-before: -1",
                "  min-spaces-after: 1",
                "  max-spaces-after: 3");
        check("""
              ---
              array: [1,  2, 3,   4]
              ...
              """, conf);
        check("""
              ---
              array: [1,  2, 3,    4]
              ...
              """, conf, getLintProblem(2, 21));
        check("""
              ---
              dict: {a: b ,   c: "1 2 3",    d: e, f: [g,      h]}
              ...
              """, conf, getLintProblem(2, 31), getLintProblem(2, 49));
    }

    @Test
    void testBothBeforeAndAfter() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("commas:",
                "  max-spaces-before: 0",
                "  min-spaces-after: 1",
                "  max-spaces-after: 1");
        check("""
              ---
              dict: {a: b ,   c: "1 2 3",    d: e , f: [g,      h]}
              array: [
                elem  ,
                key: val ,
              ]
              map: {
                key1: val1 ,
                key2: val2,
              }
              ...
              """, conf,
                getLintProblem(2, 12), getLintProblem(2, 16), getLintProblem(2, 31),
                getLintProblem(2, 36), getLintProblem(2, 50), getLintProblem(4, 8),
                getLintProblem(5, 11), getLintProblem(8, 13));
        conf = getConfig("commas:",
                "  max-spaces-before: 0",
                "  min-spaces-after: 1",
                "  max-spaces-after: 1",
                "indentation: disable");
        check("""
              ---
              - [one, two , three,four]
              - {five,six , seven, eight}
              - [
                nine,  ten
                , eleven
                ,twelve
              ]
              - {
                thirteen: 13,  fourteen
                , fifteen: 15
                ,sixteen: 16
              }
              """, conf,
                getLintProblem(2, 12), getLintProblem(2, 21), getLintProblem(3, 9),
                getLintProblem(3, 12), getLintProblem(5, 9), getLintProblem(6, 2),
                getLintProblem(7, 2), getLintProblem(7, 4), getLintProblem(10, 17),
                getLintProblem(11, 2), getLintProblem(12, 2), getLintProblem(12, 4));
    }
}
