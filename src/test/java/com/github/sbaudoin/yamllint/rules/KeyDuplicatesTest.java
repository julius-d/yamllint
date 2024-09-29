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

class KeyDuplicatesTest extends RuleTester {
    @Test
    void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("key-duplicates: disable");
        check("""
              ---
              block mapping:
                key: a
                otherkey: b
                key: c
              """, conf);
        check("""
              ---
              flow mapping:
                {key: a, otherkey: b, key: c}
              """, conf);
        check("""
              ---
              duplicated twice:
                - k: a
                  ok: b
                  k: c
                  k: d
              """, conf);
        check("""
              ---
              duplicated twice:
                - {k: a, ok: b, k: c, k: d}
              """, conf);
        check("""
              ---
              multiple duplicates:
                a: 1
                b: 2
                c: 3
                d: 4
                d: 5
                b: 6
              """, conf);
        check("""
              ---
              multiple duplicates:
                {a: 1, b: 2, c: 3, d: 4, d: 5, b: 6}
              """, conf);
        check("""
              ---
              at: root
              multiple: times
              at: root
              """, conf);
        check("""
              ---
              nested but OK:
                a: {a: {a: 1}}
                b:
                  b: 2
                  c: 3
              """, conf);
        check("""
              ---
              nested duplicates:
                a: {a: 1, a: 1}
                b:
                  c: 3
                  d: 4
                  d: 4
                b: 2
              """, conf);
        check("""
              ---
              duplicates with many styles: 1
              "duplicates with many styles": 1
              'duplicates with many styles': 1
              ? duplicates with many styles
              : 1
              ? >-
                  duplicates with
                  many styles
              : 1
              """, conf);
        check("""
              ---
              Merge Keys are OK:
              anchor_one: &anchor_one
                one: one
              anchor_two: &anchor_two
                two: two
              anchor_reference:
                <<: *anchor_one
                <<: *anchor_two
              """, conf);
        check("""
              ---
              {a: 1, b: 2}}
              """, conf, getSyntaxError(2, 13));
        check("""
              ---
              [a, b, c]]
              """, conf, getSyntaxError(2, 10));
    }

    @Test
    void testEnabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("key-duplicates: enable");
        check("""
              ---
              block mapping:
                key: a
                otherkey: b
                key: c
              """, conf,
                getLintProblem(5, 3));
        check("""
              ---
              flow mapping:
                {key: a, otherkey: b, key: c}
              """, conf,
                getLintProblem(3, 25));
        check("""
              ---
              duplicated twice:
                - k: a
                  ok: b
                  k: c
                  k: d
              """, conf,
                getLintProblem(5, 5), getLintProblem(6, 5));
        check("""
              ---
              duplicated twice:
                - {k: a, ok: b, k: c, k: d}
              """, conf,
                getLintProblem(3, 19), getLintProblem(3, 25));
        check("""
              ---
              multiple duplicates:
                a: 1
                b: 2
                c: 3
                d: 4
                d: 5
                b: 6
              """, conf,
                getLintProblem(7, 3), getLintProblem(8, 3));
        check("""
              ---
              multiple duplicates:
                {a: 1, b: 2, c: 3, d: 4, d: 5, b: 6}
              """, conf,
                getLintProblem(3, 28), getLintProblem(3, 34));
        check("""
              ---
              at: root
              multiple: times
              at: root
              """, conf,
                getLintProblem(4, 1));
        check("""
              ---
              nested but OK:
                a: {a: {a: 1}}
                b:
                  b: 2
                  c: 3
              """, conf);
        check("""
              ---
              nested duplicates:
                a: {a: 1, a: 1}
                b:
                  c: 3
                  d: 4
                  d: 4
                b: 2
              """, conf,
                getLintProblem(3, 13), getLintProblem(7, 5), getLintProblem(8, 3));
        check("""
              ---
              duplicates with many styles: 1
              "duplicates with many styles": 1
              'duplicates with many styles': 1
              ? duplicates with many styles
              : 1
              ? >-
                  duplicates with
                  many styles
              : 1
              """, conf,
                getLintProblem(3, 1), getLintProblem(4, 1), getLintProblem(5, 3),
                getLintProblem(7, 3));
        check("""
              ---
              Merge Keys are OK:
              anchor_one: &anchor_one
                one: one
              anchor_two: &anchor_two
                two: two
              anchor_reference:
                <<: *anchor_one
                <<: *anchor_two
              """, conf);
        check("""
              ---
              {a: 1, b: 2}}
              """, conf, getSyntaxError(2, 13));
        check("""
              ---
              [a, b, c]]
              """, conf, getSyntaxError(2, 10));
    }

    @Test
    void testKeyTokensInFlowSequences() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("key-duplicates: enable");
        check("""
              ---
              [
                flow: sequence, with, key: value, mappings
              ]
              """, conf);
    }
}
