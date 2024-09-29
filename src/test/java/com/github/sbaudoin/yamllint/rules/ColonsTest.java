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

class ColonsTest extends RuleTester {
    @Test
    void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("colons: disable");
        check("""
              ---
              object:
                k1 : v1
              obj2:
                k2     :
                  - 8
                k3:
                  val
                property   : value
                prop2      : val2
                propriété  : [valeur]
                o:
                  k1: [v1, v2]
                p:
                  - k3: >
                      val
                  - o: {k1: v1}
                  - p: kdjf
                  - q: val0
                  - q2:
                      - val1
              ...
              """, conf);
        check("""
              ---
              object:
                k1:   v1
              obj2:
                k2:
                  - 8
                k3:
                  val
                property:     value
                prop2:        val2
                propriété:    [valeur]
                o:
                  k1:  [v1, v2]
              """, conf);
        check("""
              ---
              obj:
                p:
                  - k1: >
                      val
                  - k3:  >
                      val
                  - o: {k1: v1}
                  - o:  {k1: v1}
                  - q2:
                      - val1
              ...
              """, conf);
        check("""
              ---
              a: {b: {c:  d, e : f}}
              """, conf);
    }

    @Test
    void testBeforeEnabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("colons: {max-spaces-before: 0, max-spaces-after: -1}");
        check("""
              ---
              object:
                k1:
                  - a
                  - b
                k2: v2
              ...
              """, conf);
        check("""
              ---
              object:
                k1 :
                  - a
                  - b
                k2: v2
              ...
              """, conf, getLintProblem(3, 5));
        check("""
              ---
              lib :
                - var
              ...
              """, conf, getLintProblem(2, 4));
        check("""
              ---
              - lib :
                  - var
              ...
              """, conf, getLintProblem(2, 6));
        check("""
              ---
              a: {b: {c : d, e : f}}
              """, conf,
                getLintProblem(2, 10), getLintProblem(2, 17));
    }

    @Test
    void testBeforeMax() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("colons: {max-spaces-before: 3, max-spaces-after: -1}");
        check("""
              ---
              object :
                k1   :
                  - a
                  - b
                k2  : v2
              ...
              """, conf);
        check("""
              ---
              object :
                k1    :
                  - a
                  - b
                k2  : v2
              ...
              """, conf, getLintProblem(3, 8));
    }

    @Test
    void testBeforeWithExplicitBlockMappings() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("colons: {max-spaces-before: 0, max-spaces-after: 1}");
        check("""
              ---
              object:
                ? key
                : value
              ...
              """, conf);
        check("""
              ---
              object :
                ? key
                : value
              ...
              """, conf, getLintProblem(2, 7));
        check("""
              ---
              ? >
                  multi-line
                  key
              : >
                  multi-line
                  value
              ...
              """, conf);
        check("""
              ---
              - ? >
                    multi-line
                    key
                : >
                    multi-line
                    value
              ...
              """, conf);
        check("""
              ---
              - ? >
                    multi-line
                    key
                :  >
                     multi-line
                     value
              ...
              """, conf, getLintProblem(5, 5));
    }

    @Test
    void testAfterEnabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("colons: {max-spaces-before: -1, max-spaces-after: 1}");
        check("""
              ---
              key: value
              """, conf);
        check("""
              ---
              key:  value
              """, conf, getLintProblem(2, 6));
        check("""
              ---
              object:
                k1:  [a, b]
                k2: string
              """, conf, getLintProblem(3, 7));
        check("""
              ---
              object:
                k1: [a, b]
                k2:  string
              """, conf, getLintProblem(4, 7));
        check("""
              ---
              object:
                other: {key:  value}
              ...
              """, conf, getLintProblem(3, 16));
        check("""
              ---
              a: {b: {c:  d, e :  f}}
              """, conf,
                getLintProblem(2, 12), getLintProblem(2, 20));
    }

    @Test
    void testAfterEnabledQuestionMark() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("colons: {max-spaces-before: -1, max-spaces-after: 1}");
        check("""
              ---
              ? key
              : value
              """, conf);
        check("""
              ---
              ?  key
              : value
              """, conf, getLintProblem(2, 3));
        check("""
              ---
              ?  key
              :  value
              """, conf, getLintProblem(2, 3), getLintProblem(3, 3));
        check("""
              ---
              - ?  key
                :  value
              """, conf, getLintProblem(2, 5), getLintProblem(3, 5));
    }

    @Test
    void testAfterMax() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("colons: {max-spaces-before: -1, max-spaces-after: 3}");
        check("""
              ---
              object:
                k1:  [a, b]
              """, conf);
        check("""
              ---
              object:
                k1:    [a, b]
              """, conf, getLintProblem(3, 9));
        check("""
              ---
              object:
                k2:  string
              """, conf);
        check("""
              ---
              object:
                k2:    string
              """, conf, getLintProblem(3, 9));
        check("""
              ---
              object:
                other: {key:  value}
              ...
              """, conf);
        check("""
              ---
              object:
                other: {key:    value}
              ...
              """, conf, getLintProblem(3, 18));
    }

    @Test
    void testAfterWithExplicitBlockMappings() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("colons: {max-spaces-before: -1, max-spaces-after: 1}");
        check("""
              ---
              object:
                ? key
                : value
              ...
              """, conf);
        check("""
              ---
              object:
                ? key
                :  value
              ...
              """, conf, getLintProblem(4, 5));
    }

    @Test
    void testAfterDoNotConfoundWithTrailingSpace() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("colons: {max-spaces-before: 1, max-spaces-after: 1}",
                "trailing-spaces: disable");
        check("""
              ---
              trailing:    \s
                - spaces
              """, conf);
    }

    @Test
    void testBothBeforeAndAfter() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("colons: {max-spaces-before: 0, max-spaces-after: 1}");
        check("""
              ---
              obj:
                string: text
                k:
                  - 8
                k3:
                  val
                property: [value]
              """, conf);
        check("""
              ---
              object:
                k1 :  v1
              """, conf, getLintProblem(3, 5), getLintProblem(3, 8));
        check("""
              ---
              obj:
                string:  text
                k :
                  - 8
                k3:
                  val
                property: {a: 1, b:  2, c : 3}
              """, conf,
                getLintProblem(3, 11), getLintProblem(4, 4),
                getLintProblem(8, 23), getLintProblem(8, 28));
    }

    /**
     * Although accepted by PyYAML, `{*x: 4}` is not valid YAML: it should be
     * noted `{*x : 4}`. The reason is that a colon can be part of an anchor
     * name. See commit message for more details.
     */
    @Test
    void testWithAliasAsKey() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("colons: {max-spaces-before: 0, max-spaces-after: 1}");
        check("""
              ---
              - anchor: &a key
              - *a: 42
              - {*a: 42}
              - *a : 42
              - {*a : 42}
              - *a  : 42
              - {*a  : 42}
              """,
                conf,
                getLintProblem(7, 6), getLintProblem(8, 7));
    }
}
