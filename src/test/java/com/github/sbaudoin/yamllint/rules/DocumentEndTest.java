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
package com.github.sbaudoin.yamllint.rules;

import com.github.sbaudoin.yamllint.YamlLintConfig;
import org.junit.jupiter.api.Test;

class DocumentEndTest extends RuleTester {
  @Test
  void disabled() throws Exception {
    YamlLintConfig conf = getConfig("document-end: disable");
    check(
        """
              ---
              with:
                document: end
              ...
              """,
        conf);
    check(
        """
              ---
              without:
                document: end
              """,
        conf);
  }

  @Test
  void required() throws Exception {
    YamlLintConfig conf = getConfig("document-end: {present: true}");
    check("", conf);
    check("\n", conf);
    check(
        """
              ---
              with:
                document: end
              ...
              """,
        conf);
    check(
        """
              ---
              without:
                document: end
              """,
        conf,
        getLintProblem(3, 1));
  }

  @Test
  void forbidden() throws Exception {
    YamlLintConfig conf = getConfig("document-end: {present: false}");
    check(
        """
              ---
              with:
                document: end
              ...
              """,
        conf,
        getLintProblem(4, 1));
    check(
        """
              ---
              without:
                document: end
              """,
        conf);
  }

  @Test
  void multipleDocuments() throws Exception {
    YamlLintConfig conf = getConfig("document-end: {present: true}", "document-start: disable");
    check(
        """
              ---
              first: document
              ...
              ---
              second: document
              ...
              ---
              third: document
              ...
              """,
        conf);
    check(
        """
              ---
              first: document
              ...
              ---
              second: document
              ---
              third: document
              ...
              """,
        conf,
        getLintProblem(6, 1));
  }

  @Test
  void directives() throws Exception {
    YamlLintConfig conf = getConfig("document-end: {present: true}");
    check(
        """
              %YAML 1.2
              ---
              document: end
              ...
              """,
        conf);
    check(
        """
              %YAML 1.2
              %TAG ! tag:clarkevans.com,2002:
              ---
              document: end
              ...
              """,
        conf);
    check(
        """
              ---
              first: document
              ...
              %YAML 1.2
              ---
              second: document
              ...
              """,
        conf);
  }
}
