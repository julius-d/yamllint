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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.PipedReader;
import java.io.StringReader;
import org.junit.jupiter.api.Test;

class LintStreamReaderTest {
  @Test
  void constructors() {
    assertThat(new LintStreamReader("").getMark().getName()).isEqualTo("'string'");
    assertThat(new LintStreamReader(new StringReader("")).getMark().getName())
        .isEqualTo("'reader'");

    PipedReader reader = new PipedReader();
    assertThatThrownBy(() -> new LintStreamReader(reader))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void forward() {
    LintStreamReader reader = new LintStreamReader("test");
    while (reader.peek() != '\u0000') {
      reader.forward(1);
    }
    reader = new LintStreamReader("test");
    assertThat(reader.peek()).isEqualTo('t');
    reader.forward(1);
    assertThat(reader.peek()).isEqualTo('e');
    reader.forward(1);
    assertThat(reader.peek()).isEqualTo('s');
    reader.forward(1);
    assertThat(reader.peek()).isEqualTo('t');
    reader.forward(1);
    assertThat(reader.peek()).isEqualTo('\u0000');
  }

  @Test
  void peekInt() {
    LintStreamReader reader = new LintStreamReader("test");
    assertThat(reader.peek(0)).isEqualTo('t');
    assertThat(reader.peek(1)).isEqualTo('e');
    assertThat(reader.peek(2)).isEqualTo('s');
    assertThat(reader.peek(3)).isEqualTo('t');
    reader.forward(1);
    assertThat(reader.peek(0)).isEqualTo('e');
    assertThat(reader.peek(1)).isEqualTo('s');
    assertThat(reader.peek(2)).isEqualTo('t');
  }
}
