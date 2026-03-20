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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.util.logging.*;
import org.junit.jupiter.api.Test;

class RuleFactoryTest {
  @Test
  void getRule() {
    // Temporarily remove console handler
    Logger logger = Logger.getLogger(RuleFactory.class.getName()).getParent();
    ConsoleHandler ch = (ConsoleHandler) logger.getHandlers()[0];
    logger.removeHandler(ch);

    // Add memory handler to check logs
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    StreamHandler sh =
        new StreamHandler(
            bos,
            new Formatter() {
              @Override
              public String format(LogRecord logRecord) {
                return logRecord.getMessage() + System.lineSeparator();
              }
            });
    sh.setLevel(Level.WARNING);
    logger.addHandler(sh);

    // All known rules
    assertThat(RuleFactory.instance.getRule("braces")).isNotNull();
    assertThat(RuleFactory.instance.getRule("brackets")).isNotNull();
    assertThat(RuleFactory.instance.getRule("colons")).isNotNull();
    assertThat(RuleFactory.instance.getRule("commas")).isNotNull();
    assertThat(RuleFactory.instance.getRule("comments")).isNotNull();
    assertThat(RuleFactory.instance.getRule("comments-indentation")).isNotNull();
    assertThat(RuleFactory.instance.getRule("document-end")).isNotNull();
    assertThat(RuleFactory.instance.getRule("document-start")).isNotNull();
    assertThat(RuleFactory.instance.getRule("empty-lines")).isNotNull();
    assertThat(RuleFactory.instance.getRule("empty-values")).isNotNull();
    assertThat(RuleFactory.instance.getRule("hyphens")).isNotNull();
    assertThat(RuleFactory.instance.getRule("indentation")).isNotNull();
    assertThat(RuleFactory.instance.getRule("key-duplicates")).isNotNull();
    assertThat(RuleFactory.instance.getRule("key-ordering")).isNotNull();
    assertThat(RuleFactory.instance.getRule("line-length")).isNotNull();
    assertThat(RuleFactory.instance.getRule("new-line-at-end-of-file")).isNotNull();
    assertThat(RuleFactory.instance.getRule("new-lines")).isNotNull();
    assertThat(RuleFactory.instance.getRule("octal-values")).isNotNull();
    assertThat(RuleFactory.instance.getRule("trailing-spaces")).isNotNull();
    assertThat(RuleFactory.instance.getRule("truthy")).isNotNull();

    // Unknown rule
    assertThat(RuleFactory.instance.getRule("this-rule-does-not-exist")).isNull();

    // Set back console handler
    logger.removeHandler(sh);
    sh.close();
    logger.addHandler(ch);
  }
}
