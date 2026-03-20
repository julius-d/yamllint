/*
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

class KeyOrderingTest extends RuleTester {
  @Test
  void disabled() throws Exception {
    YamlLintConfig conf = getConfig("key-ordering: disable");
    check(
        """
              ---
              block mapping:
                secondkey: a
                firstkey: b
              """,
        conf);
    check(
        """
              ---
              flow mapping:
                {secondkey: a, firstkey: b}
              """,
        conf);
    check(
        """
              ---
              second: before_first
              at: root
              """,
        conf);
    check(
        """
              ---
              nested but OK:
                second: {first: 1}
                third:
                  second: 2
              """,
        conf);
  }

  @Test
  void enabled() throws Exception {
    YamlLintConfig conf = getConfig("key-ordering: enable");
    check(
        """
              ---
              block mapping:
                secondkey: a
                firstkey: b
              """,
        conf,
        getLintProblem(4, 3));
    check(
        """
              ---
              flow mapping:
                {secondkey: a, firstkey: b}
              """,
        conf,
        getLintProblem(3, 18));
    check(
        """
              ---
              second: before_first
              at: root
              """,
        conf,
        getLintProblem(3, 1));
    check(
        """
              ---
              nested but OK:
                second: {first: 1}
                third:
                  second: 2
              """,
        conf);
  }

  @Test
  void wordLength() throws Exception {
    YamlLintConfig conf = getConfig("key-ordering: enable");
    check(
        """
              ---
              a: 1
              ab: 1
              abc: 1
              """,
        conf);
    check(
        """
              ---
              a: 1
              abc: 1
              ab: 1
              """,
        conf,
        getLintProblem(4, 1));
  }

  @Test
  void keyDuplicates() throws Exception {
    YamlLintConfig conf = getConfig("key-duplicates: disable", "key-ordering: enable");
    check("""
              ---
              key: 1
              key: 2
              """, conf);
  }

  @Test
  void testCase() throws Exception {
    YamlLintConfig conf = getConfig("key-ordering: enable");
    check(
        """
              ---
              T-shirt: 1
              T-shirts: 2
              t-shirt: 3
              t-shirts: 4
              """,
        conf);
    check(
        """
              ---
              T-shirt: 1
              t-shirt: 2
              T-shirts: 3
              t-shirts: 4
              """,
        conf,
        getLintProblem(4, 1));
  }

  @Test
  void accents() throws Exception {
    YamlLintConfig conf = getConfig("key-ordering: enable");
    check(
        """
              ---
              hair: true
              hais: true
              haïr: true
              haïssable: true
              """,
        conf);
    check(
        """
              ---
              haïr: true
              hais: true
              """,
        conf,
        getLintProblem(3, 1));
    check(
        """
              ---
              haïr: true
              hais: true
              """,
        conf,
        getLintProblem(3, 1));
  }

  @Test
  void keyTokensInFlowSequences() throws Exception {
    YamlLintConfig conf = getConfig("key-ordering: enable");
    check(
        """
              ---
              [
                key: value, mappings, in, flow: sequence
              ]
              """,
        conf);
  }
}
