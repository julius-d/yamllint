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

import static org.assertj.core.api.Assertions.*;

import com.github.sbaudoin.yamllint.rules.Rule;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SimpleYamlLintConfigTest {
  @Test
  void constructorWithNull() throws Exception {
    assertThatThrownBy(() -> new YamlLintConfig((String) null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new YamlLintConfig((URL) null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new YamlLintConfig((InputStream) null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void constructorInputStream() {
    File confFile = Path.of("src", "test", "resources", "config", "local", ".yamllint").toFile();
    try (InputStream in = new FileInputStream(confFile)) {
      new YamlLintConfig(in);
    } catch (Exception e) {
      fail("Should not fail: valid conf file passed");
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void parseConfig() throws Exception {
    YamlLintConfig conf =
        new YamlLintConfig(
            """
       rules:
         colons:
           max-spaces-before: 0
           max-spaces-after: 1
       """);

    assertThat(conf.ruleConf.keySet()).isEqualTo(new HashSet(Arrays.asList("colons")));
    assertThat(conf.getRuleConf("colons")).isInstanceOf(Map.class);
    assertThat(((Map) conf.getRuleConf("colons")).get("max-spaces-before")).isEqualTo(0);
    assertThat(((Map) conf.getRuleConf("colons")).get("max-spaces-after")).isEqualTo(1);

    assertThat(conf.getEnabledRules(null).size()).isEqualTo(1);
  }

  @Test
  void invalidConf() {
    assertThatThrownBy(() -> new YamlLintConfig("")).isInstanceOf(YamlLintConfigException.class);

    assertThatThrownBy(() -> new YamlLintConfig("not: valid: yaml"))
        .isInstanceOf(YamlLintConfigException.class);

    assertThatThrownBy(() -> new YamlLintConfig("ignore: 3"))
        .isInstanceOf(YamlLintConfigException.class);
  }

  @Test
  void unknownRule() {
    try {
      new YamlLintConfig(
          """
                         rules:
                           this-one-does-not-exist: enable
                         """);
      fail("Unknown rule not identified");
    } catch (YamlLintConfigException e) {
      assertThat(e.getMessage())
          .isEqualTo("invalid config: no such rule: \"this-one-does-not-exist\"");
    }
  }

  @Test
  void unknownOption() {
    try {
      new YamlLintConfig(
          """
                         rules:
                           colons:
                             max-spaces-before: 0
                             max-spaces-after: 1
                             abcdef: yes
                         """);
      fail("Unknown option not identified");
    } catch (YamlLintConfigException e) {
      assertThat(e.getMessage())
          .isEqualTo("invalid config: unknown option \"abcdef\" for rule \"colons\"");
    }
  }

  @Test
  void yesNoForBooleans() throws Exception {
    YamlLintConfig conf =
        new YamlLintConfig(
            """
       rules:
         indentation:
           spaces: 2
           indent-sequences: true
           check-multi-line-strings: false
       """);
    assertThat(((Map) conf.getRuleConf("indentation")).get("indent-sequences")).isEqualTo(true);
    assertThat(((Map) conf.getRuleConf("indentation")).get("check-multi-line-strings"))
        .isEqualTo(false);

    conf =
        new YamlLintConfig(
            """
                              rules:
                                indentation:
                                  spaces: 2
                                  indent-sequences: yes
                                  check-multi-line-strings: false
                              """);
    assertThat(((Map) conf.getRuleConf("indentation")).get("indent-sequences")).isEqualTo(true);
    assertThat(((Map) conf.getRuleConf("indentation")).get("check-multi-line-strings"))
        .isEqualTo(false);

    conf =
        new YamlLintConfig(
            """
                              rules:
                                indentation:
                                  spaces: 2
                                  indent-sequences: whatever
                                  check-multi-line-strings: false
                              """);
    assertThat(((Map) conf.getRuleConf("indentation")).get("indent-sequences"))
        .isEqualTo("whatever");
    assertThat(((Map) conf.getRuleConf("indentation")).get("check-multi-line-strings"))
        .isEqualTo(false);

    try {
      new YamlLintConfig(
          """
                         rules:
                           indentation:
                             spaces: 2
                             indent-sequences: YES!
                             check-multi-line-strings: false
                         """);
      fail("Invalid option value accepted");
    } catch (YamlLintConfigException e) {
      assertThat(
              e.getMessage()
                  .startsWith(
                      "invalid config: option \"indent-sequences\" of \"indentation\" should be in "))
          .isTrue();
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void validateRuleConf() throws Exception {
    final Rule rule = getDummyRule();

    assertThat(YamlLintConfig.validateRuleConf(rule, null)).isNull();
    assertThat(YamlLintConfig.validateRuleConf(rule, "disable")).isNull();

    assertThat(YamlLintConfig.validateRuleConf(rule, new HashMap()))
        .isEqualTo(toMap(new Object[][] {{"level", "error"}}));
    assertThat(YamlLintConfig.validateRuleConf(rule, "enable"))
        .isEqualTo(toMap(new Object[][] {{"level", "error"}}));

    try {
      YamlLintConfig.validateRuleConf(rule, "invalid conf");
      fail("Invalid configuration accepted");
    } catch (YamlLintConfigException e) {
      assertThat(e.getMessage())
          .isEqualTo(
              "invalid config: rule \"dummy-rule\": should be either \"enable\", \"disable\" or a dictionary");
    }

    // Ignore
    try {
      YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] {{"ignore", 3}}));
      fail("Invalid configuration accepted");
    } catch (YamlLintConfigException e) {
      assertThat(e.getMessage()).isEqualTo("invalid config: 'ignore' should contain file patterns");
    }
    Assertions.assertThatCode(
            () -> {
              YamlLintConfig.validateRuleConf(
                  rule, toMap(new Object[][] {{"ignore", Arrays.asList("foo", "bar")}}));
              assertThat(rule.ignores(new File("foo"))).isTrue();
              assertThat(rule.ignores(new File("bar"))).isTrue();
              YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] {{"ignore", "foo\nbar"}}));
              assertThat(rule.ignores(new File("foo"))).isTrue();
              assertThat(rule.ignores(new File("bar"))).isTrue();
            })
        .withFailMessage("Unexpected error thrown: ")
        .doesNotThrowAnyException();

    Assertions.assertThatCode(
            () -> {
              YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] {{"level", "error"}}));
              YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] {{"level", "warning"}}));
              YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] {{"level", "info"}}));
            })
        .withFailMessage("Error level not recognized")
        .doesNotThrowAnyException();
    try {
      YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] {{"level", "warn"}}));
      fail("Unsupported error level accepted");
    } catch (YamlLintConfigException e) {
      assertThat(e.getMessage())
          .isEqualTo("invalid config: level should be \"error\", \"warning\" or \"info\"");
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void validateRuleConf2() throws Exception {
    final var rule = getDummyRule(toMap(new Object[][] {{"length", 0}}));
    Assertions.assertThatCode(
            () -> {
              YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] {{"length", 8}}));
            })
        .withFailMessage("Supported option value not accepted: ")
        .doesNotThrowAnyException();
    try {
      YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] {{"height", 8}}));
      fail("Unknown option accepted");
    } catch (YamlLintConfigException e) {
      assertThat(e.getMessage())
          .isEqualTo("invalid config: unknown option \"height\" for rule \"dummy-rule\"");
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void validateRuleConf3() throws Exception {
    final var rule = getDummyRule(toMap(new Object[][] {{"a", false}, {"b", 44}}));
    Assertions.assertThatCode(
            () -> {
              YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] {{"a", true}, {"b", 0}}));
            })
        .withFailMessage("Supported option value not accepted")
        .doesNotThrowAnyException();
    try {
      YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] {{"a", 1}, {"b", 0}}));
      fail("Unsupported option value accepted");
    } catch (YamlLintConfigException e) {
      assertThat(e.getMessage())
          .isEqualTo("invalid config: option \"a\" of \"dummy-rule\" should be of type boolean");
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void validateRuleConf4() throws Exception {
    final var rule =
        getDummyRule(toMap(new Object[][] {{"choice", Arrays.asList(true, 88, "str")}}));
    Assertions.assertThatCode(
            () -> {
              YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] {{"choice", true}}));
              YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] {{"choice", 88}}));
              YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] {{"choice", "str"}}));
            })
        .withFailMessage("Supported option value not accepted")
        .doesNotThrowAnyException();
    try {
      YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] {{"choice", false}}));
      fail("Unsupported option value accepted");
    } catch (YamlLintConfigException e) {
      assertThat(e.getMessage())
          .isEqualTo(
              "invalid config: option \"choice\" of \"dummy-rule\" should be in [true, 88, 'str']");
    }
    try {
      YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] {{"choice", 99}}));
      fail("Unsupported option value accepted");
    } catch (YamlLintConfigException e) {
      assertThat(e.getMessage())
          .isEqualTo(
              "invalid config: option \"choice\" of \"dummy-rule\" should be in [true, 88, 'str']");
    }
    try {
      YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] {{"choice", "abc"}}));
      fail("Unsupported option value accepted");
    } catch (YamlLintConfigException e) {
      assertThat(e.getMessage())
          .isEqualTo(
              "invalid config: option \"choice\" of \"dummy-rule\" should be in [true, 88, 'str']");
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void validateRuleConf5() throws Exception {
    final var rule =
        getDummyRule(toMap(new Object[][] {{"choice", Arrays.asList(Integer.class, "hardcoded")}}));
    Assertions.assertThatCode(
            () -> {
              YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] {{"choice", 42}}));
              YamlLintConfig.validateRuleConf(
                  rule, toMap(new Object[][] {{"choice", "hardcoded"}}));
            })
        .withFailMessage("Supported option value not accepted")
        .doesNotThrowAnyException();
    try {
      YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] {{"choice", false}}));
      fail("Unsupported option value accepted");
    } catch (YamlLintConfigException e) {
      assertThat(e.getMessage())
          .isEqualTo(
              "invalid config: option \"choice\" of \"dummy-rule\" should be in [integer, 'hardcoded']");
    }
    try {
      YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] {{"choice", "abc"}}));
      fail("Unsupported option value accepted");
    } catch (YamlLintConfigException e) {
      assertThat(e.getMessage())
          .isEqualTo(
              "invalid config: option \"choice\" of \"dummy-rule\" should be in [integer, 'hardcoded']");
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void validateRuleConf6() throws Exception {
    final var rule = getDummyRule(toMap(new Object[][] {{"errored", true}}));
    try {
      YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] {{"errored", true}}));
      fail("Invalid conf accepted");
    } catch (YamlLintConfigException e) {
      assertThat(e.getMessage())
          .isEqualTo("invalid config: dummy-rule: the conf says to return an error message");
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void validateRuleConf7() throws Exception {
    // Test list options
    final var rule =
        getDummyRule(toMap(new Object[][] {{"alist", Collections.<String>emptyList()}}), true);
    try {
      YamlLintConfig.validateRuleConf(rule, toMap(new Object[][] {{"alist", "not a list"}}));
      fail("Invalid conf accepted");
    } catch (YamlLintConfigException e) {
      assertThat(e.getMessage())
          .isEqualTo("invalid config: option \"alist\" of \"dummy-rule\" should be a list");
    }
    Assertions.assertThatCode(
            () -> {
              Map<String, Object> conf =
                  YamlLintConfig.validateRuleConf(
                      rule, toMap(new Object[][] {{"alist", Arrays.asList("value1", "value2")}}));
            })
        .withFailMessage("Valid list conf failed: ")
        .doesNotThrowAnyException();
  }

  @Test
  void mutuallyExclusiveIgnoreKeys() {
    try {
      new YamlLintConfig(
          """
                         extends: default
                         ignore-from-file: .gitignore
                         ignore: |
                           *.dont-lint-me.yaml
                           /bin/
                         """);
      fail("Invalid conf accepted");
    } catch (YamlLintConfigException e) {
      assertThat(e.getMessage())
          .isEqualTo("invalid config: ignore and ignore-from-file keys cannot be used together");
    }
  }

  @Test
  void ignore() throws Exception {
    YamlLintConfig conf =
        new YamlLintConfig(
            """
       rules:
         indentation:
           spaces: 2
           indent-sequences: true
           check-multi-line-strings: false
       ignore: |
         .*\\.txt$
         foo.bar
       """);
    assertThat(conf.isFileIgnored("/my/file.txt")).isTrue();
    assertThat(conf.isFileIgnored("foo.bar")).isTrue();
    assertThat(conf.isFileIgnored("/anything/that/matches/nothing.doc")).isFalse();
    assertThat(conf.isFileIgnored("/foo.bar")).isFalse();

    assertThatThrownBy(
            () ->
                new YamlLintConfig(
                    """
                                                rules:
                                                  indentation:
                                                    spaces: 2
                                                    indent-sequences: true
                                                    check-multi-line-strings: false
                                                ignore:
                                                  - ".*\\.txt$"
                                                  - foo.bar
                                                """))
        .isInstanceOf(YamlLintConfigException.class);

    assertThatThrownBy(
            () ->
                new YamlLintConfig(
                    """
                                                rules:
                                                  indentation:
                                                    spaces: 2
                                                    indent-sequences: true
                                                    check-multi-line-strings: false
                                                ignore: 3
                                                """))
        .isInstanceOf(YamlLintConfigException.class);
  }

  @Test
  void ignoreFromFileDoesNotExist() {
    try {
      new YamlLintConfig(
          """
                         extends: default
                         ignore-from-file: not_found_file
                         """);
      fail("Invalid ignore-from-file configuration accepted");
    } catch (YamlLintConfigException e) {
      assertThat(e.getMessage())
          .isEqualTo("invalid config: ignore-from-file contains an invalid file path");
    }
  }

  @Test
  void ignoreFromFileIncorrectType() {
    try {
      new YamlLintConfig(
          """
                         extends: default
                         ignore-from-file: 0
                         """);
      fail("Invalid ignore-from-file syntax accepted");
    } catch (YamlLintConfigException e) {
      assertThat(e.getMessage())
          .isEqualTo(
              "invalid config: ignore-from-file should contain filename(s), either as a list or string");
    }

    try {
      new YamlLintConfig(
          """
                         extends: default
                         ignore-from-file: [0]
                         """);
      fail("Invalid ignore-from-file syntax accepted");
    } catch (YamlLintConfigException e) {
      assertThat(e.getMessage())
          .isEqualTo(
              "invalid config: ignore-from-file should contain filename(s), either as a list or string");
    }
  }

  @Test
  void ignoreFromFileValidConf() {
    Rule rule = getDummyRule();

    Assertions.assertThatCode(
            () -> {
              YamlLintConfig.validateRuleConf(
                  rule,
                  toMap(
                      new Object[][] {
                        {
                          "ignore-from-file",
                          "src"
                              + File.separator
                              + "test"
                              + File.separator
                              + "resources"
                              + File.separator
                              + "config"
                              + File.separator
                              + "ignore"
                        }
                      }));
              assertThat(rule.ignores(new File("foo"))).isTrue();
              assertThat(rule.ignores(new File("bar"))).isTrue();
              YamlLintConfig.validateRuleConf(
                  rule,
                  toMap(
                      new Object[][] {
                        {
                          "ignore-from-file",
                          Arrays.asList(
                              "src"
                                  + File.separator
                                  + "test"
                                  + File.separator
                                  + "resources"
                                  + File.separator
                                  + "config"
                                  + File.separator
                                  + "ignore")
                        }
                      }));
              assertThat(rule.ignores(new File("foo"))).isTrue();
              assertThat(rule.ignores(new File("bar"))).isTrue();
            })
        .withFailMessage("Unknown error: ")
        .doesNotThrowAnyException();
  }

  @Test
  void isYamlFile() throws Exception {
    assertThatThrownBy(
            () ->
                new YamlLintConfig(
                    """
                                                yaml-files:
                                                  indentation:
                                                    spaces: 2
                                                    indent-sequences: true
                                                    check-multi-line-strings: false
                                                """))
        .isInstanceOf(YamlLintConfigException.class);

    YamlLintConfig conf = new YamlLintConfig("extends: default\n");
    assertThat(conf.isYamlFile("/my/file.yaml")).isTrue();
    assertThat(conf.isYamlFile("foo.yml")).isTrue();
    assertThat(conf.isYamlFile("/anything/that/a.yaml/donot.match")).isFalse();
    assertThat(conf.isYamlFile("/foo.Yaml")).isFalse();

    conf =
        new YamlLintConfig(
            """
                              rules:
                                colons:
                                  max-spaces-before: 0
                                  max-spaces-after: 1
                              """);
    assertThat(conf.isYamlFile("/my/file.yaml")).isTrue();
    assertThat(conf.isYamlFile("foo.yml")).isTrue();
    assertThat(conf.isYamlFile("/anything/that/a.yaml/donot.match")).isFalse();
    assertThat(conf.isYamlFile("/foo.Yaml")).isFalse();

    conf =
        new YamlLintConfig(
            """
                              yaml-files:
                                - .*\\.match$
                              """);
    assertThat(conf.isYamlFile("/my/file.yaml")).isFalse();
    assertThat(conf.isYamlFile("foo.yml")).isFalse();
    assertThat(conf.isYamlFile("/anything/that/a.yaml/donot.match")).isTrue();
    assertThat(conf.isYamlFile("/foo.Yaml")).isFalse();
  }

  @SuppressWarnings("unchecked")
  private Map toMap(Object[][] o) {
    Map map = new HashMap();
    for (Object[] objects : o) {
      map.put(objects[0], objects[1]);
    }
    return map;
  }

  private Rule getDummyRule() {
    return getDummyRule(new HashMap<>());
  }

  private Rule getDummyRule(final Map<String, Object> o) {
    return getDummyRule(o, false);
  }

  private Rule getDummyRule(final Map<String, Object> o, final boolean isList) {
    return new Rule() {
      {
        for (Map.Entry<String, Object> e : o.entrySet()) {
          if (isList) {
            registerListOption(e.getKey(), (List<?>) e.getValue());
          } else {
            registerOption(e.getKey(), e.getValue());
          }
        }
      }

      @Override
      public TYPE getType() {
        return null;
      }

      @Override
      public String getId() {
        return "dummy-rule";
      }

      @Override
      public String validate(Map<String, Object> conf) {
        if (conf.containsKey("errored")) {
          return "the conf says to return an error message";
        }
        return null;
      }
    };
  }
}
