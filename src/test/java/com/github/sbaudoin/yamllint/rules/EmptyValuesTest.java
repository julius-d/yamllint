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

class EmptyValuesTest extends RuleTester {
    @Test
    void testDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: disable",
                "braces: disable",
                "commas: disable");
        check("""
              ---
              foo:
              """, conf);
        check("""
              ---
              foo:
               bar:
              """, conf);
        check("""
              ---
              {a:}
              """, conf);
        check("""
              ---
              foo: {a:}
              """, conf);
        check("""
              ---
              - {a:}
              - {a:, b: 2}
              - {a: 1, b:}
              - {a: 1, b: , }
              """, conf);
        check("""
              ---
              {a: {b: , c: {d: 4, e:}}, f:}
              """, conf);
    }

    @Test
    void testInBlockMappingsDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: false,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}");
        check("""
              ---
              foo:
              """, conf);
        check("""
              ---
              foo:
              bar: aaa
              """, conf);
    }

    @Test
    void testInBlockMappingsSingleLine() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: true,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}");
        check("""
              ---
              implicitly-null:
              """, conf, getLintProblem(2, 17));
        check("""
              ---
              implicitly-null:with-colons:in-key:
              """, conf,
                getLintProblem(2, 36));
        check("""
              ---
              implicitly-null:with-colons:in-key2:
              """, conf,
                getLintProblem(2, 37));
    }

    @Test
    void testInBlockMappingsAllLines() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: true,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}");
        check("""
              ---
              foo:
              bar:
              foobar:
              """, conf,
                getLintProblem(2, 5),
                getLintProblem(3, 5),
                getLintProblem(4, 8));
    }

    @Test
    void testInBlockMappingsExplicitEndOfDocument() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: true,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}");
        check("""
              ---
              foo:
              ...
              """, conf, getLintProblem(2, 5));
    }

    @Test
    void testInBlockMappingsNotEndOfDocument() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: true,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}");
        check("""
              ---
              foo:
              bar:
               aaa
              """, conf, getLintProblem(2, 5));
    }

    @Test
    void testInBlockMappingsDifferentLevel() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: true,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}");
        check("""
              ---
              foo:
               bar:
              aaa: bbb
              """, conf, getLintProblem(3, 6));
    }

    @Test
    void testInBlockMappingsEmptyFlowMapping() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: true,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}",
                "braces: disable",
                "commas: disable");
        check("""
              ---
              foo: {a:}
              """, conf);
        check("""
              ---
              - {a:, b: 2}
              - {a: 1, b:}
              - {a: 1, b: , }
              """, conf);
    }

    @Test
    void testInBlockMappingsEmptyBlockSequence() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: true,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}");
        check("""
              ---
              foo:
                -
              """, conf);
    }

    @Test
    void testInBlockMappingsNotEmptyOrExplicitNull() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: true,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}");
        check("""
              ---
              foo:
               bar:
                aaa
              """, conf);
        check("""
              ---
              explicitly-null: null
              """, conf);
        check("""
              ---
              explicitly-null:with-colons:in-key: null
              """, conf);
        check("""
              ---
              false-null: nulL
              """, conf);
        check("""
              ---
              empty-string: ""
              """, conf);
        check("""
              ---
              nullable-boolean: false
              """, conf);
        check("""
              ---
              nullable-int: 0
              """, conf);
        check("""
              ---
              First occurrence: &anchor Foo
              Second occurrence: *anchor
              """, conf);
    }

    @Test
    void testInBlockMappingsVariousExplicitNull() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: true,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}");
        check("""
              ---
              null-alias: ~
              """, conf);
        check("""
              ---
              null-key1: {?: val}
              """, conf);
        check("""
              ---
              null-key2: {? !!null "": val}
              """, conf);
    }

    @Test
    void testInBlockMappingsComments() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: true,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}",
                "comments: disable");
        check("""
              ---
              empty:  # comment
              foo:
                bar: # comment
              """, conf,
                getLintProblem(2, 7),
                getLintProblem(4, 7));
    }

    @Test
    void testInFlowMappingsDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: false,",
                "               forbid-in-flow-mappings: false," +
                "               forbid-in-block-sequences: false}",
                "braces: disable",
                "commas: disable");
        check("""
              ---
              {a:}
              """, conf);
        check("""
              ---
              foo: {a:}
              """, conf);
        check("""
              ---
              - {a:}
              - {a:, b: 2}
              - {a: 1, b:}
              - {a: 1, b: , }
              """, conf);
        check("""
              ---
              {a: {b: , c: {d: 4, e:}}, f:}
              """, conf);
    }

    @Test
    void testInFlowMappingsSingleLine() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: false,",
                "               forbid-in-flow-mappings: true," +
                "               forbid-in-block-sequences: false}",
                "braces: disable",
                "commas: disable");
        check("""
              ---
              {a:}
              """, conf,
                getLintProblem(2, 4));
        check("""
              ---
              foo: {a:}
              """, conf,
                getLintProblem(2, 9));
        check("""
              ---
              - {a:}
              - {a:, b: 2}
              - {a: 1, b:}
              - {a: 1, b: , }
              """, conf,
                getLintProblem(2, 6),
                getLintProblem(3, 6),
                getLintProblem(4, 12),
                getLintProblem(5, 12));
        check("""
              ---
              {a: {b: , c: {d: 4, e:}}, f:}
              """, conf,
                getLintProblem(2, 8),
                getLintProblem(2, 23),
                getLintProblem(2, 29));
    }

    @Test
    void testInFlowMappingsMultiLine() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: false,",
                "               forbid-in-flow-mappings: true," +
                "               forbid-in-block-sequences: false}",
                "braces: disable",
                "commas: disable");
        check("""
              ---
              foo: {
                a:
              }
              """, conf,
                getLintProblem(3, 5));
        check("""
              ---
              {
                a: {
                  b: ,
                  c: {
                    d: 4,
                    e:
                  }
                },
                f:
              }
              """, conf,
                getLintProblem(4, 7),
                getLintProblem(7, 9),
                getLintProblem(10, 5));
    }

    @Test
    void testInFlowMappingsVariousExplicitNull() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: false,",
                "               forbid-in-flow-mappings: true," +
                "               forbid-in-block-sequences: false}",
                "braces: disable",
                "commas: disable");
        check("""
              ---
              {explicit-null: null}
              """, conf);
        check("""
              ---
              {null-alias: ~}
              """, conf);
        check("""
              ---
              null-key1: {?: val}
              """, conf);
        check("""
              ---
              null-key2: {? !!null "": val}
              """, conf);
    }

    @Test
    void testInFlowMappingsComments() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: false,",
                "               forbid-in-flow-mappings: true," +
                "               forbid-in-block-sequences: false}",
                "braces: disable",
                "commas: disable",
                "comments: disable");
        check("""
              ---
              {
                a: {
                  b: ,  # comment
                  c: {
                    d: 4,  # comment
                    e:  # comment
                  }
                },
                f:  # comment
              }
              """, conf,
                getLintProblem(4, 7),
                getLintProblem(7, 9),
                getLintProblem(10, 5));
    }

    @Test
    void testInBlockSequencesDisabled() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("empty-values: {forbid-in-block-mappings: false," +
                "               forbid-in-flow-mappings: false,\n" +
                "               forbid-in-block-sequences: false}");
        check("""
              ---
              foo:
                - bar
                -
              """, conf);
        check("""
              ---
              foo:
                -
              """, conf);
    }

    @Test
    void testInBlockSequencesPrimativeItem() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("""
                                        empty-values: {forbid-in-block-mappings: false,
                                                       forbid-in-flow-mappings: false,
                                                       forbid-in-block-sequences: true}""");
        check("""
              ---
              foo:
                -
              """, conf,
                getLintProblem(3, 4));
        check("""
              ---
              foo:
                - bar
                -
              """, conf,
                getLintProblem(4, 4));
        check("""
              ---
              foo:
                - 1
                - 2
                -
              """, conf,
                getLintProblem(5, 4));
        check("""
              ---
              foo:
                - true
              """, conf);
    }

    @Test
    void test_in_block_sequences_complex_objects() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("""
                                        empty-values: {forbid-in-block-mappings: false,
                                                       forbid-in-flow-mappings: false,
                                                       forbid-in-block-sequences: true}""");
        check("""
              ---
              foo:
                - a: 1
              """, conf);
        check("""
              ---
              foo:
                - a: 1
                -
              """, conf,
                getLintProblem(4, 4));
        check("""
              ---
              foo:
                - a: 1
                  b: 2
                -
              """, conf,
                getLintProblem(5, 4));
        check("""
              ---
              foo:
                - a: 1
                - b: 2
                -
              """, conf,
                getLintProblem(5, 4));
        check("""
              ---
              foo:
                - - a
                  - b: 2
                  -
              """, conf,
                getLintProblem(5, 6));
        check("""
              ---
              foo:
                - - a
                  - b: 2
                -
              """, conf,
                getLintProblem(5, 4));
    }

    @Test
    void testInBlockSequencesVariousExplicitNull() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("""
                                        empty-values: {forbid-in-block-mappings: false,
                                                       forbid-in-flow-mappings: false,
                                                       forbid-in-block-sequences: true}""");
        check("""
              ---
              foo:
                - null
              """, conf);
        check("""
              ---
              - null
              """, conf);
        check("""
              ---
              foo:
                - bar: null
                - null
              """, conf);
        check("""
              ---
              - null
              - null
              """, conf);
        check("""
              ---
              - - null
                - null
              """, conf);
    }
}
