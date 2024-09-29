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
package com.github.sbaudoin.yamllint;

import com.github.sbaudoin.yamllint.rules.RuleTester;
import org.junit.jupiter.api.Test;

class SyntaxErrorTest extends RuleTester {
    @Override
    public String getRuleId() {
        // By convention syntax errors have the 'null' id
        return null;
    }

    @Test
    void testSyntaxErrors() throws YamlLintConfigException {
        check("""
              ---
              this is not: valid: YAML
              """, null, getLintProblem(2, 19));
        check("""
              ---
              this is: valid YAML
              
              this is an error: [
              
              ...
              """, null, getLintProblem(6, 1));
        check("""
              %YAML 1.2
              %TAG ! tag:clarkevans.com,2002:
              doc: ument
              ...
              """, null, getLintProblem(3, 1));
    }

    @Test
    void testEmptyFlows() throws YamlLintConfigException {
        check("""
              ---
              - []
              - {}
              - [
              ]
              - {
              }
              ...
              """, null);
    }

    @Test
    void testExplicitMapping() throws YamlLintConfigException {
        check("""
              ---
              ? key
              : - value 1
                - value 2
              ...
              """, null);
        check("""
              ---
              ?
                key
              : {a: 1}
              ...
              """, null);
        check("""
              ---
              ?
                key
              :
                val
              ...
              """, null);
    }

    @Test
    void testMappingBetweenSequences() throws YamlLintConfigException {
        // This is valid YAML.See http://www.yaml.org/spec/1.2/spec.html,
        // example 2.11
        check("""
              ---
              ? - Detroit Tigers
                - Chicago cubs
              :
                - 2001-07-23
              
              ? [New York Yankees,
                 Atlanta Braves]
              : [2001-07-02, 2001-08-12,
                 2001-08-14]
              """, null);
    }

    @Test
    void testSets() throws YamlLintConfigException {
        check("""
              ---
              ? key one
              ? key two
              ? [non, scalar, key]
              ? key with value
              : value
              ...
              """, null);
        check("""
              ---
              ? - multi
                - line
                - keys
              ? in:
                  a:
                    set
              ...
              """, null);
    }

    @Test
    void testMultipleDocs() throws YamlLintConfigException {
        check("""
              ---
              a: b
              ...
              ---
              ,
              ...
              """, null, getLintProblem(5, 1));
    }

    @Test
    void testCustomTag() throws YamlLintConfigException {
        // See https://github.com/sbaudoin/sonar-yaml/issues/15
        check("""
              ---
              appli_password: !vault |
                        $ANSIBLE_VAULT;1.1;AES256
                        42424242424242424242424242424242424242424242424242424242424242424242424242424242
                        42424242424242424242424242424242424242424242424242424242424242424242424242424242
                        42424242424242424242424242424242424242424242424242424242424242424242424242424242
                        42424242424242424242424242424242424242424242424242424242424242424242424242424242
                        4242
              """, null);
    }
}
