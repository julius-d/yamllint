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
package com.github.sbaudoin.yamllint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExtendedYamlLintConfigTest {
  @Test
  void wrongExtend() {
    try {
      new YamlLintConfig("extends: null");
      fail("Invalid config not identified");
    } catch (YamlLintConfigException e) {
      assertThat(e.getMessage()).isEqualTo("invalid extends config: need to extend something");
    }

    try {
      new YamlLintConfig("extends:");
      fail("Invalid config not identified");
    } catch (YamlLintConfigException e) {
      assertThat(e.getMessage()).isEqualTo("invalid extends config: need to extend something");
    }

    try {
      new YamlLintConfig("extends:\n  - foo");
      fail("Invalid config not identified");
    } catch (YamlLintConfigException e) {
      assertThat(e.getMessage().startsWith("invalid extends config: unknown error: ")).isTrue();
    }

    try {
      new YamlLintConfig("extends: dummy");
      fail("Unknown ruleset should not be extended");
    } catch (YamlLintConfigException e) {
      assertThat(e.getMessage())
          .isEqualTo("invalid extends config: Bundled configuration file \"dummy\" not found");
    }

    try {
      new YamlLintConfig("extends: foo" + File.separator + "bar");
      fail("Unknown ruleset should not be extended");
    } catch (YamlLintConfigException e) {
      assertThat(e.getCause()).isInstanceOf(FileNotFoundException.class);
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void extendAddRule() throws Exception {
    YamlLintConfig oldConf =
        new YamlLintConfig(
            """
                 rules:
                   colons:
                     max-spaces-before: 0
                     max-spaces-after: 1
                 """);
    YamlLintConfig newConf =
        new YamlLintConfig(
            """
                rules:
                  hyphens:
                    max-spaces-after: 2
                """);
    newConf.extend(oldConf);

    assertThat(newConf.ruleConf.keySet())
        .isEqualTo(new HashSet(Arrays.asList("colons", "hyphens")));
    assertThat(newConf.getRuleConf("colons")).isInstanceOf(Map.class);
    assertThat(((Map) newConf.getRuleConf("colons")).get("max-spaces-before")).isEqualTo(0);
    assertThat(((Map) newConf.getRuleConf("colons")).get("max-spaces-after")).isEqualTo(1);
    assertThat(newConf.getRuleConf("hyphens")).isInstanceOf(Map.class);
    assertThat(((Map) newConf.getRuleConf("hyphens")).get("max-spaces-after")).isEqualTo(2);

    assertThat(newConf.getEnabledRules(null).size()).isEqualTo(2);
  }

  @Test
  @SuppressWarnings("unchecked")
  void extendRemoveRule() throws Exception {
    YamlLintConfig oldConf =
        new YamlLintConfig(
            """
                rules:
                  colons:
                    max-spaces-before: 0
                    max-spaces-after: 1
                  hyphens:
                    max-spaces-after: 2
                """);
    YamlLintConfig newConf =
        new YamlLintConfig(
            """
                rules:
                  colons: disable
                """);
    newConf.extend(oldConf);

    assertThat(newConf.ruleConf.keySet())
        .isEqualTo(new HashSet(Arrays.asList("colons", "hyphens")));
    assertThat(newConf.getRuleConf("colons")).isNull();
    assertThat(newConf.getRuleConf("hyphens")).isInstanceOf(Map.class);
    assertThat(((Map) newConf.getRuleConf("hyphens")).get("max-spaces-after")).isEqualTo(2);

    assertThat(newConf.getEnabledRules(null).size()).isEqualTo(1);
  }

  @Test
  @SuppressWarnings("unchecked")
  void extendEditRule() throws Exception {
    YamlLintConfig oldConf =
        new YamlLintConfig(
            """
                rules:
                  colons:
                    max-spaces-before: 0
                    max-spaces-after: 1
                  hyphens:
                    max-spaces-after: 2
                """);
    YamlLintConfig newConf =
        new YamlLintConfig(
            """
                rules:
                  colons:
                    max-spaces-before: 3
                    max-spaces-after: 4
                """);
    newConf.extend(oldConf);

    assertThat(newConf.ruleConf.keySet())
        .isEqualTo(new HashSet(Arrays.asList("colons", "hyphens")));
    assertThat(newConf.getRuleConf("colons")).isInstanceOf(Map.class);
    assertThat(newConf.getRuleConf("hyphens")).isInstanceOf(Map.class);
    assertThat(((Map) newConf.getRuleConf("colons")).get("max-spaces-before")).isEqualTo(3);
    assertThat(((Map) newConf.getRuleConf("colons")).get("max-spaces-after")).isEqualTo(4);
    assertThat(((Map) newConf.getRuleConf("hyphens")).get("max-spaces-after")).isEqualTo(2);

    assertThat(newConf.getEnabledRules(null).size()).isEqualTo(2);
  }

  @Test
  @SuppressWarnings("unchecked")
  void extendReenableRule() throws Exception {
    YamlLintConfig oldConf =
        new YamlLintConfig(
            """
                rules:
                  colons:
                    max-spaces-before: 0
                    max-spaces-after: 1
                  hyphens: disable
                """);
    YamlLintConfig newConf =
        new YamlLintConfig(
            """
                rules:
                  hyphens:
                    max-spaces-after: 2
                """);
    newConf.extend(oldConf);

    assertThat(newConf.ruleConf.keySet())
        .isEqualTo(new HashSet(Arrays.asList("colons", "hyphens")));
    assertThat(newConf.getRuleConf("colons")).isInstanceOf(Map.class);
    assertThat(newConf.getRuleConf("hyphens")).isInstanceOf(Map.class);
    assertThat(((Map) newConf.getRuleConf("colons")).get("max-spaces-before")).isEqualTo(0);
    assertThat(((Map) newConf.getRuleConf("colons")).get("max-spaces-after")).isEqualTo(1);
    assertThat(((Map) newConf.getRuleConf("hyphens")).get("max-spaces-after")).isEqualTo(2);

    assertThat(newConf.getEnabledRules(null).size()).isEqualTo(2);
  }

  @Test
  void extendWithIgnore() throws Exception {
    YamlLintConfig oldConf =
        new YamlLintConfig(
            """
                rules:
                  colons:
                    max-spaces-before: 0
                    max-spaces-after: 1
                ignore: foo.bar
                """);
    YamlLintConfig newConf =
        new YamlLintConfig(
            """
                rules:
                  hyphens:
                    max-spaces-after: 2
                """);

    assertThat(newConf.isFileIgnored("foo.bar")).isFalse();
    newConf.extend(oldConf);
    assertThat(newConf.isFileIgnored("foo.bar")).isTrue();
  }
}
