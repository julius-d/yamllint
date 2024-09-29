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

class TruthyTest extends RuleTester {
    @Test
    void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("truthy: disable");
        check("""
              ---
              1: True
              """, conf);
        check("""
              ---
              True: 1
              """, conf);
    }

    @Test
    void testEnabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("truthy: enable");
        check("""
              ---
              1: True
              True: 1
              """,
                conf, getLintProblem(2, 4), getLintProblem(3, 1));
        check("""
              ---
              1: "True"
              "True": 1
              """, conf);
        check("""
              ---
              [
                true, false,
                "false", "FALSE",
                "true", "True",
                True, FALSE,
                on, OFF,
                NO, Yes
              ]
              """, conf,
                getLintProblem(6, 3), getLintProblem(6, 9),
                getLintProblem(7, 3), getLintProblem(7, 7),
                getLintProblem(8, 3), getLintProblem(8, 7));
    }

    @Test
    void testDifferentAllowedValues() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("truthy:",
                "  allowed-values: [\"yes\", \"no\"]");
        check("""
              ---
              key1: foo
              key2: yes
              key3: bar
              key4: no
              """, conf);
        check("""
              ---
              key1: true
              key2: Yes
              key3: false
              key4: no
              key5: yes
              """,
                conf,
                getLintProblem(2, 7), getLintProblem(3, 7),
                getLintProblem(4, 7));
    }

    @Test
    void testCombinedAllowedValues() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("truthy:",
                "  allowed-values: [\"yes\", \"no\", \"true\", \"false\"]");
        check("""
              ---
              key1: foo
              key2: yes
              key3: bar
              key4: no
              """, conf);
        check("""
              ---
              key1: true
              key2: Yes
              key3: false
              key4: no
              key5: yes
              """,
                conf, getLintProblem(3, 7));
    }

    @Test
    void testNoAllowedValues() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("truthy:",
                "  allowed-values: []");
        check("""
              ---
              key1: foo
              key2: bar
              """, conf);
        check("""
              ---
              key1: true
              key2: yes
              key3: false
              key4: no
              """, conf,
                getLintProblem(2, 7), getLintProblem(3, 7),
                getLintProblem(4, 7), getLintProblem(5, 7));
    }

    @Test
    void testExplicitTypes() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("truthy: enable");
        check("---\n" +
                "string1: !!str True\n" +
                "string2: !!str yes\n" +
                "string3: !!str off\n" +
                "encoded: !!binary |\n" +
                "           True\n" +
                "           OFF\n" +
                "           pad==\n" +  // this decodes as "N\xbb\x9e8Qii"
                "boolean1: !!bool true\n" +
                "boolean2: !!bool \"false\"\n" +
                "boolean3: !!bool FALSE\n" +
                "boolean4: !!bool True\n" +
                "boolean5: !!bool off\n" +
                "boolean6: !!bool NO\n",
                conf);
    }

    @Test
    void testCheckKeysDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("truthy:",
                "  allowed-values: []",
                "  check-keys: false",
                "key-duplicates: disable");
        check("""
              ---
              YES: 0
              Yes: 0
              yes: 0
              No: 0
              No: 0
              no: 0
              TRUE: 0
              True: 0
              true: 0
              FALSE: 0
              False: 0
              false: 0
              ON: 0
              On: 0
              on: 0
              OFF: 0
              Off: 0
              off: 0
              YES:
                Yes:
                  yes:
                    on: 0
              """,
                conf);
    }
}
