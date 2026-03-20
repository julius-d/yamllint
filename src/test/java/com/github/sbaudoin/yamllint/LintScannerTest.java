/**
 * Copyright (c) 2018-2023, Sylvain Baudoin
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.sbaudoin.yamllint;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.reader.StreamReader;

class LintScannerTest {
  @Test
  void getToken() {
    LintScanner scanner = new LintScanner(new StreamReader("key1: value\nkey2: value"));
    Assertions.assertThatCode(
            () -> {
              while (scanner.getToken() != null) {
                // Do nothing: we just want to go through all token
              }
            })
        .withFailMessage("IndexOutOfBoundsException raised while reading tokens")
        .doesNotThrowAnyException();
  }

  @Test
  void peekToken() {
    LintScanner scanner = new LintScanner(new StreamReader("key1: value\nkey2: value"));
    Assertions.assertThatCode(
            () -> {
              while (scanner.peekToken() != null) {
                // Looks like this is redundant compared to testGetToken()...
                scanner.getToken();
              }
            })
        .withFailMessage("IndexOutOfBoundsException raised while reading tokens")
        .doesNotThrowAnyException();
  }

  @Test
  void hasMoreTokens() {
    LintScanner scanner = new LintScanner(new StreamReader("key1: value\nkey2: value"));
    assertThat(scanner.hasMoreTokens()).isTrue(); // StreamStartToken
    for (int i = 0; i < 11; i++) {
      scanner.getToken();
    }
    assertThat(scanner.hasMoreTokens()).isTrue();
    scanner.getToken(); // StreamEndToken
    assertThat(scanner.hasMoreTokens()).isFalse();
  }
}
