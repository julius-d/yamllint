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

class ScalarIndentationTest extends RuleTester {
    @Override
    public String getRuleId() {
        return "indentation";
    }

    @Test
    void testBasicsPlain() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: false}",
                "document-start: disable");
        check("""
              multi
              line
              """, conf);
        check("""
              multi
               line
              """, conf);
        check("""
              - multi
                line
              """, conf);
        check("""
              - multi
                 line
              """, conf);
        check("""
              a key: multi
                     line
              """, conf);
        check("""
              a key: multi
                line
              """, conf);
        check("""
              a key: multi
                      line
              """, conf);
        check("""
              a key:
                multi
                line
              """, conf);
        check("""
              - C code: void main() {
                            printf("foo");
                        }
              """, conf);
        check("""
              - C code:
                  void main() {
                      printf("foo");
                  }
              """, conf);
    }

    @Test
    void testCheckMultiLinePlain() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: true}",
                "document-start: disable");
        check("""
              multi
               line
              """, conf, getLintProblem(2, 2));
        check("""
              - multi
                 line
              """, conf, getLintProblem(2, 4));
        check("""
              a key: multi
                line
              """, conf, getLintProblem(2, 3));
        check("""
              a key: multi
                      line
              """, conf, getLintProblem(2, 9));
        check("""
              a key:
                multi
                 line
              """, conf, getLintProblem(3, 4));
        check("""
              - C code: void main() {
                            printf("foo");
                        }
              """, conf, getLintProblem(2, 15));
        check("""
              - C code:
                  void main() {
                      printf("foo");
                  }
              """, conf, getLintProblem(3, 9));
    }

    @Test
    void testBasicsQuoted() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: false}",
                "document-start: disable");
        check("""
              "multi
               line"
              """, conf);
        check("""
              - "multi
                 line"
              """, conf);
        check("""
              a key: "multi
                      line"
              """, conf);
        check("""
              a key:
                "multi
                 line"
              """, conf);
        check("""
              - jinja2: "{% if ansible is defined %}
                           {{ ansible }}
                         {% else %}
                           {{ chef }}
                         {% endif %}"
              """, conf);
        check("""
              - jinja2:
                  "{% if ansible is defined %}
                     {{ ansible }}
                   {% else %}
                     {{ chef }}
                   {% endif %}"
              """, conf);
        check("""
              ["this is a very long line
                that needs to be split",
               "other line"]
              """, conf);
        check("""
              ["multi
                line 1", "multi
                          line 2"]
              """, conf);
    }

    @Test
    void testCheckMultiLineQuoted() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: true}",
                "document-start: disable");
        check("""
              "multi
              line"
              """, conf, getLintProblem(2, 1));
        check("""
              "multi
                line"
              """, conf, getLintProblem(2, 3));
        check("""
              - "multi
                line"
              """, conf, getLintProblem(2, 3));
        check("""
              - "multi
                  line"
              """, conf, getLintProblem(2, 5));
        check("""
              a key: "multi
                line"
              """, conf, getLintProblem(2, 3));
        check("""
              a key: "multi
                     line"
              """, conf, getLintProblem(2, 8));
        check("""
              a key: "multi
                       line"
              """, conf, getLintProblem(2, 10));
        check("""
              a key:
                "multi
                line"
              """, conf, getLintProblem(3, 3));
        check("""
              a key:
                "multi
                  line"
              """, conf, getLintProblem(3, 5));
        check("""
              - jinja2: "{% if ansible is defined %}
                           {{ ansible }}
                         {% else %}
                           {{ chef }}
                         {% endif %}"
              """, conf,
                getLintProblem(2, 14), getLintProblem(4, 14));
        check("""
              - jinja2:
                  "{% if ansible is defined %}
                     {{ ansible }}
                   {% else %}
                     {{ chef }}
                   {% endif %}"
              """, conf,
                getLintProblem(3, 8), getLintProblem(5, 8));
        check("""
              ["this is a very long line
                that needs to be split",
               "other line"]
              """, conf);
        check("""
              ["this is a very long line
               that needs to be split",
               "other line"]
              """, conf, getLintProblem(2, 2));
        check("""
              ["this is a very long line
                 that needs to be split",
               "other line"]
              """, conf, getLintProblem(2, 4));
        check("""
              ["multi
                line 1", "multi
                          line 2"]
              """, conf);
        check("""
              ["multi
                line 1", "multi
                         line 2"]
              """, conf, getLintProblem(3, 12));
        check("""
              ["multi
                line 1", "multi
                           line 2"]
              """, conf, getLintProblem(3, 14));
    }

    @Test
    void testBasicsFoldedStyle() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: false}",
                "document-start: disable");
        check("""
              >
                multi
                line
              """, conf);
        check("""
              - >
                  multi
                  line
              """, conf);
        check("""
              - key: >
                  multi
                  line
              """, conf);
        check("""
              - key:
                  >
                    multi
                    line
              """, conf);
        check("""
              - ? >
                    multi-line
                    key
                : >
                    multi-line
                    value
              """, conf);
        check("""
              - ?
                  >
                    multi-line
                    key
                :
                  >
                    multi-line
                    value
              """, conf);
        check("""
              - jinja2: >
                  {% if ansible is defined %}
                    {{ ansible }}
                  {% else %}
                    {{ chef }}
                  {% endif %}
              """, conf);
    }

    @Test
    void testCheckMultiLineFoldedStyle() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: true}",
                "document-start: disable");
        check("""
              >
                multi
                 line
              """, conf, getLintProblem(3, 4));
        check("""
              - >
                  multi
                   line
              """, conf, getLintProblem(3, 6));
        check("""
              - key: >
                  multi
                   line
              """, conf, getLintProblem(3, 6));
        check("""
              - key:
                  >
                    multi
                     line
              """, conf, getLintProblem(4, 8));
        check("""
              - ? >
                    multi-line
                     key
                : >
                    multi-line
                     value
              """, conf,
                getLintProblem(3, 8), getLintProblem(6, 8));
        check("""
              - ?
                  >
                    multi-line
                     key
                :
                  >
                    multi-line
                     value
              """, conf,
                getLintProblem(4, 8), getLintProblem(8, 8));
        check("""
              - jinja2: >
                  {% if ansible is defined %}
                    {{ ansible }}
                  {% else %}
                    {{ chef }}
                  {% endif %}
              """, conf,
                getLintProblem(3, 7), getLintProblem(5, 7));
    }

    @Test
    void testBasicsLiteralStyle() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: false}",
                "document-start: disable");
        check("""
              |
                multi
                line
              """, conf);
        check("""
              - |
                  multi
                  line
              """, conf);
        check("""
              - key: |
                  multi
                  line
              """, conf);
        check("""
              - key:
                  |
                    multi
                    line
              """, conf);
        check("""
              - ? |
                    multi-line
                    key
                : |
                    multi-line
                    value
              """, conf);
        check("""
              - ?
                  |
                    multi-line
                    key
                :
                  |
                    multi-line
                    value
              """, conf);
        check("""
              - jinja2: |
                  {% if ansible is defined %}
                   {{ ansible }}
                  {% else %}
                    {{ chef }}
                  {% endif %}
              """, conf);
    }

    @Test
    void testCheckMultiLineLiteralStyle() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: true}",
                "document-start: disable");
        check("""
              |
                multi
                 line
              """, conf, getLintProblem(3, 4));
        check("""
              - |
                  multi
                   line
              """, conf, getLintProblem(3, 6));
        check("""
              - key: |
                  multi
                   line
              """, conf, getLintProblem(3, 6));
        check("""
              - key:
                  |
                    multi
                     line
              """, conf, getLintProblem(4, 8));
        check("""
              - ? |
                    multi-line
                     key
                : |
                    multi-line
                     value
              """, conf,
                getLintProblem(3, 8), getLintProblem(6, 8));
        check("""
              - ?
                  |
                    multi-line
                     key
                :
                  |
                    multi-line
                     value
              """, conf,
                getLintProblem(4, 8), getLintProblem(8, 8));
        check("""
              - jinja2: |
                  {% if ansible is defined %}
                    {{ ansible }}
                  {% else %}
                    {{ chef }}
                  {% endif %}
              """, conf,
                getLintProblem(3, 7), getLintProblem(5, 7));
    }

    // The following "paragraph" examples are inspired from
    // http://stackoverflow.com/questions/3790454/in-yaml-how-do-i-break-a-string-over-multiple-lines

    @Test
    void testParagraphPlain() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: true}",
                "document-start: disable");
        check("""
              - long text: very "long"
                           'string' with
              
                           paragraph gap, \\n and
                           spaces.
              """, conf);
        check("""
              - long text: very "long"
                  'string' with
              
                  paragraph gap, \\n and
                  spaces.
              """, conf,
                getLintProblem(2, 5), getLintProblem(4, 5), getLintProblem(5, 5));
        check("""
              - long text:
                  very "long"
                  'string' with
              
                  paragraph gap, \\n and
                  spaces.
              """, conf);
    }

    @Test
    void testParagraphDoubleQuoted() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: true}",
                "document-start: disable");
        check("""
              - long text: "very \\"long\\"
                            'string' with
              
                            paragraph gap, \\n and
                            spaces."
              """, conf);
        check("""
              - long text: "very \\"long\\"
                  'string' with
              
                  paragraph gap, \\n and
                  spaces."
              """, conf,
                getLintProblem(2, 5), getLintProblem(4, 5), getLintProblem(5, 5));
        check("""
              - long text: "very \\"long\\"
              'string' with
              
              paragraph gap, \\n and
              spaces."
              """, conf,
                getLintProblem(2, 1), getLintProblem(4, 1), getLintProblem(5, 1));
        check("""
              - long text:
                  "very \\"long\\"
                   \'string\' with
              
                   paragraph gap, \\n and
                   spaces."
              """, conf);
    }

    @Test
    void testParagraphSingleQuoted() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: true}",
                "document-start: disable");
        check("""
              - long text: 'very "long"
                            ''string'' with
              
                            paragraph gap, \\n and
                            spaces.'
              """, conf);
        check("""
              - long text: 'very "long"
                  ''string'' with
              
                  paragraph gap, \\n and
                  spaces.'
              """, conf,
                getLintProblem(2, 5), getLintProblem(4, 5), getLintProblem(5, 5));
        check("""
              - long text: 'very "long"
              ''string'' with
              
              paragraph gap, \\n and
              spaces.'
              """, conf,
                getLintProblem(2, 1), getLintProblem(4, 1), getLintProblem(5, 1));
        check("""
              - long text:
                  'very "long"
                   ''string'' with
              
                   paragraph gap, \\n and
                   spaces.'
              """, conf);
    }

    @Test
    void testParagraphFolded() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: true}",
                "document-start: disable");
        check("""
              - long text: >
                  very "long"
                  'string' with
              
                  paragraph gap, \\n and
                  spaces.
              """, conf);
        check("""
              - long text: >
                  very "long"
                   'string' with
              
                    paragraph gap, \\n and
                     spaces.
              """, conf,
                getLintProblem(3, 6), getLintProblem(5, 7), getLintProblem(6, 8));
    }

    @Test
    void testParagraphLiteral() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: true}",
                "document-start: disable");
        check("""
              - long text: |
                  very "long"
                  'string' with
              
                  paragraph gap, \\n and
                  spaces.
              """, conf);
        check("""
              - long text: |
                  very "long"
                   'string' with
              
                    paragraph gap, \\n and
                     spaces.
              """, conf,
                getLintProblem(3, 6), getLintProblem(5, 7), getLintProblem(6, 8));
    }

    @Test
    void testConsistent() throws YamlLintConfigException {
        YamlLintConfig conf = getConfig("indentation: {spaces: consistent,",
                "              indent-sequences: true,",
                "              check-multi-line-strings: true}",
                "document-start: disable");
        check("""
              multi
              line
              """, conf);
        check("""
              multi
               line
              """, conf, getLintProblem(2, 2));
        check("""
              - multi
                line
              """, conf);
        check("""
              - multi
                 line
              """, conf, getLintProblem(2, 4));
        check("""
              a key: multi
                line
              """, conf, getLintProblem(2, 3));
        check("""
              a key: multi
                      line
              """, conf, getLintProblem(2, 9));
        check("""
              a key:
                multi
                 line
              """, conf, getLintProblem(3, 4));
        check("""
              - C code: void main() {
                            printf("foo");
                        }
              """, conf, getLintProblem(2, 15));
        check("""
              - C code:
                  void main() {
                      printf("foo");
                  }
              """, conf, getLintProblem(3, 9));
        check("""
              >
                multi
                line
              """, conf);
        check("""
              >
                   multi
                   line
              """, conf);
        check("""
              >
                   multi
                    line
              """, conf, getLintProblem(3, 7));
    }
}
