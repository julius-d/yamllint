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

class FloatValuesTest extends RuleTester {
    @Test
    void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("float-values: disable");
        check("""
              ---
              - 0.0
              - .NaN
              - .INF
              - .1
              - 10e-6
              """,
                conf);
    }

    @Test
    void testNumeralBeforeDecimal() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("float-values:",
                "  require-numeral-before-decimal: true",
                "  forbid-scientific-notation: false",
                "  forbid-nan: false",
                "  forbid-inf: false");
        check("""
              ---
              - 0.0
              - .1
              - '.1'
              - string.1
              - .1string
              - !custom_tag .2
              - &angle1 0.0
              - *angle1
              - &angle2 .3
              - *angle2
              """,
                conf,
                getLintProblem(3, 3), getLintProblem(10, 11));
    }

    @Test
    void testScientificNotation() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("float-values:",
                "  require-numeral-before-decimal: false",
                "  forbid-scientific-notation: true",
                "  forbid-nan: false",
                "  forbid-inf: false");
        check("""
              ---
              - 10e6
              - 10e-6
              - 0.00001
              - '10e-6'
              - string10e-6
              - 10e-6string
              - !custom_tag 10e-6
              - &angle1 0.000001
              - *angle1
              - &angle2 10e-6
              - *angle2
              - &angle3 10e6
              - *angle3
              """,
                conf,
                getLintProblem(2, 3),
                getLintProblem(3, 3),
                getLintProblem(11, 11),
                getLintProblem(13, 11));
    }

    @Test
    void testNan() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("float-values:",
                "  require-numeral-before-decimal: false",
                "  forbid-scientific-notation: false",
                "  forbid-nan: true",
                "  forbid-inf: false");
        check("""
              ---
              - .NaN
              - .NAN
              - '.NaN'
              - a.NaN
              - .NaNa
              - !custom_tag .NaN
              - &angle .nan
              - *angle
              """,
                conf,
                getLintProblem(2, 3),
                getLintProblem(3, 3),
                getLintProblem(8, 10));
    }

    @Test
    void testInf() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("float-values:",
                "  require-numeral-before-decimal: false",
                "  forbid-scientific-notation: false",
                "  forbid-nan: false",
                "  forbid-inf: true");
        check("""
              ---
              - .inf
              - .INF
              - -.inf
              - -.INF
              - '.inf'
              - ∞.infinity
              - .infinity∞
              - !custom_tag .inf
              - &angle .inf
              - *angle
              - &angle -.inf
              - *angle
              """,
                conf,
                getLintProblem(2, 3),
                getLintProblem(3, 3),
                getLintProblem(4, 3),
                getLintProblem(5, 3),
                getLintProblem(10, 10),
                getLintProblem(12, 10));
    }
}
