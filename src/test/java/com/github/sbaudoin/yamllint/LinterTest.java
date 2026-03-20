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

import static com.github.sbaudoin.yamllint.rules.RuleTester.getFakeConfig;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class LinterTest {
  @Test
  void runOnString() throws Exception {
    assertThat(Linter.run("test: document", getFakeConfig()).size()).isEqualTo(2);
    assertThat(Linter.run("test: document", getFakeConfig(), new File("file.yml")).size())
        .isEqualTo(2);
  }

  @Test
  void reader() throws Exception {
    assertThat(Linter.run(new StringReader("---\n"), getFakeConfig()).size()).isEqualTo(0);
    assertThat(
            Linter.run(new StringReader("test: document"), getFakeConfig(), new File("file.yml"))
                .size())
        .isEqualTo(2);
  }

  @Test
  void empty() throws Exception {
    assertThat(Linter.run("---\n", getFakeConfig()).size()).isEqualTo(0);
  }

  @Test
  void runOnNonAsciiChars() throws Exception {
    String s =
        """
                   ---
                   - hétérogénéité
                   # 19.99
                   """;
    assertThat(Linter.run(s, getFakeConfig()).size()).isEqualTo(0);
    assertThat(
            Linter.run(
                    new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)), getFakeConfig())
                .size())
        .isEqualTo(0);
    assertThat(
            Linter.run(
                    new String(s.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1),
                    getFakeConfig())
                .size())
        .isEqualTo(0);

    s =
        """
            ---
            - お早う御座います。
            # الأَبْجَدِيَّة العَرَبِيَّة
            """;
    assertThat(Linter.run(s, getFakeConfig()).size()).isEqualTo(0);
    assertThat(
            Linter.run(
                    new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)), getFakeConfig())
                .size())
        .isEqualTo(0);
  }

  @Test
  void runWithIgnore() throws Exception {
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
    assertThat(Linter.run(conf, new File("/my/file.txt")).size()).isEqualTo(0);
    assertThat(Linter.run(conf, new File("foo.bar")).size()).isEqualTo(0);
  }

  @Test
  void getProblemLevel() {
    assertThat(Linter.getProblemLevel(0)).isEqualTo(Linter.NONE_LEVEL);
    assertThat(Linter.getProblemLevel(1)).isEqualTo(Linter.INFO_LEVEL);
    assertThat(Linter.getProblemLevel(2)).isEqualTo(Linter.WARNING_LEVEL);
    assertThat(Linter.getProblemLevel(3)).isEqualTo(Linter.ERROR_LEVEL);
    assertThat(Linter.getProblemLevel(Linter.NONE_LEVEL)).isEqualTo(0);
    assertThat(Linter.getProblemLevel(Linter.INFO_LEVEL)).isEqualTo(1);
    assertThat(Linter.getProblemLevel(Linter.WARNING_LEVEL)).isEqualTo(2);
    assertThat(Linter.getProblemLevel(Linter.ERROR_LEVEL)).isEqualTo(3);
  }
}
