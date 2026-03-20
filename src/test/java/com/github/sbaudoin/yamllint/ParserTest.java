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
package com.github.sbaudoin.yamllint;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.tokens.BlockMappingStartToken;
import org.yaml.snakeyaml.tokens.DocumentStartToken;
import org.yaml.snakeyaml.tokens.KeyToken;
import org.yaml.snakeyaml.tokens.StreamStartToken;
import org.yaml.snakeyaml.tokens.ValueToken;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


class ParserTest {
    @Test
    void getLines() {
        List<Parser.Line> e = Parser.getLines("");
        assertThat(e.size()).isEqualTo(1);
        assertThat(e.get(0).getLineNo()).isEqualTo(1);
        assertThat(e.get(0).getStart()).isEqualTo(0);
        assertThat(e.get(0).getEnd()).isEqualTo(0);

        e = Parser.getLines("\n");
        assertThat(e.size()).isEqualTo(2);

        e = Parser.getLines(" \n");
        assertThat(e.size()).isEqualTo(2);
        assertThat(e.get(0).getLineNo()).isEqualTo(1);
        assertThat(e.get(0).getStart()).isEqualTo(0);
        assertThat(e.get(0).getEnd()).isEqualTo(1);

        e = Parser.getLines("\n\n");
        assertThat(e.size()).isEqualTo(3);

        e = Parser.getLines("""
                            ---
                            this is line 1
                            line 2
                            
                            3
                            """);
        assertThat(e.size()).isEqualTo(6);
        assertThat(e.get(0).getLineNo()).isEqualTo(1);
        assertThat(e.get(0).getContent()).isEqualTo("---");
        assertThat(e.get(2).getContent()).isEqualTo("line 2");
        assertThat(e.get(3).getContent()).isEqualTo("");
        assertThat(e.get(5).getLineNo()).isEqualTo(6);

        e = Parser.getLines("""
                            test with
                            no newline
                            at the end""");
        assertThat(e.size()).isEqualTo(3);
        assertThat(e.get(2).getLineNo()).isEqualTo(3);
        assertThat(e.get(2).getContent()).isEqualTo("at the end");
    }

    @Test
    void getTokensOrComments() {
        List<Parser.Lined> e = Parser.getTokensOrComments("");
        assertThat(e.size()).isEqualTo(2);
        assertThat(e.get(0)).isInstanceOf(Parser.Token.class);
        assertThat(((Parser.Token)e.get(0)).getPrev()).isNull();
        assertThat(((Parser.Token)e.get(0)).getCurr()).isNotNull();
        assertThat(((Parser.Token)e.get(0)).getNext()).isNotNull();
        assertThat(e.get(1)).isInstanceOf(Parser.Token.class);
        assertThat(((Parser.Token)e.get(0)).getCurr()).isEqualTo(((Parser.Token)e.get(1)).getPrev());
        assertThat(((Parser.Token)e.get(0)).getNext()).isEqualTo(((Parser.Token)e.get(1)).getCurr());
        assertThat(((Parser.Token)e.get(1)).getNext()).isNull();

        e = Parser.getTokensOrComments("""
                                       ---
                                       k: v
                                       """);
        assertThat(e.size()).isEqualTo(9);
        assertThat(((Parser.Token)e.get(3)).getCurr()).isInstanceOf(KeyToken.class);
        assertThat(((Parser.Token)e.get(5)).getCurr()).isInstanceOf(ValueToken.class);

        e = Parser.getTokensOrComments("""
                                       # start comment
                                       - a
                                       - key: val  # key=val
                                       # this is
                                       # a block    \s
                                       # comment
                                       - c
                                       # end comment
                                       """);
        assertThat(e.size()).isEqualTo(21);
        assertThat(e.get(1)).isInstanceOf(Parser.Comment.class);
        assertThat(e.get(1)).isEqualTo(new Parser.Comment(1, 1, "# start comment", 0, null, null, null));
        assertThat(e.get(11)).isEqualTo(new Parser.Comment(3, 13, "# key=val", 0, null, null, null));
        assertThat(e.get(12)).isEqualTo(new Parser.Comment(4, 1, "# this is", 0, null, null, null));
        assertThat(e.get(13)).isEqualTo(new Parser.Comment(5, 1, "# a block     ", 0, null, null, null));
        assertThat(e.get(14)).isEqualTo(new Parser.Comment(6, 1, "# comment", 0, null, null, null));
        assertThat(e.get(18)).isEqualTo(new Parser.Comment(8, 1, "# end comment", 0, null, null, null));

        e = Parser.getTokensOrComments("""
                ---
                # no newline char""");
        assertThat(e.get(2)).isEqualTo(new Parser.Comment(2, 1, "# no newline char", 0, null, null, null));

        e = Parser.getTokensOrComments("# just comment");
        assertThat(e.get(1)).isEqualTo(new Parser.Comment(1, 1, "# just comment", 0, null, null, null));

        e = Parser.getTokensOrComments("""
                                       
                                          # indented comment
                                       """);
        assertThat(e.get(1)).isEqualTo(new Parser.Comment(2, 4, "# indented comment", 0, null, null, null));

        e = Parser.getTokensOrComments("""
                                       
                                       # trailing spaces   \s
                                       """);
        assertThat(e.get(1)).isEqualTo(new Parser.Comment(2, 1, "# trailing spaces    ", 0, null, null, null));

        e = Parser.getTokensOrComments("""
                                       # block
                                       # comment
                                       - data   # inline comment
                                       # block
                                       # comment
                                       - k: v   # inline comment
                                       - [ l, ist
                                       ]   # inline comment
                                       - { m: ap
                                       }   # inline comment
                                       # block comment
                                       - data   # inline comment
                                       """).stream().filter(c -> c instanceof Parser.Comment).collect(Collectors.toList());
        assertThat(e.size()).isEqualTo(10);
        assertThat(((Parser.Comment)e.get(0)).isInline()).isFalse();
        assertThat(((Parser.Comment)e.get(1)).isInline()).isFalse();
        assertThat(((Parser.Comment)e.get(2)).isInline()).isTrue();
        assertThat(((Parser.Comment)e.get(3)).isInline()).isFalse();
        assertThat(((Parser.Comment)e.get(4)).isInline()).isFalse();
        assertThat(((Parser.Comment)e.get(5)).isInline()).isTrue();
        assertThat(((Parser.Comment)e.get(6)).isInline()).isTrue();
        assertThat(((Parser.Comment)e.get(7)).isInline()).isTrue();
        assertThat(((Parser.Comment)e.get(8)).isInline()).isFalse();
        assertThat(((Parser.Comment)e.get(9)).isInline()).isTrue();
    }

    @Test
    void getTokensOrCommentsOrLines() {
        List<Parser.Lined> e = Parser.getTokensOrCommentsOrLines("""
                                                                 ---
                                                                 k: v  # k=v
                                                                 """);
        assertThat(e.size()).isEqualTo(13);
        assertThat(e.get(0)).isInstanceOf(Parser.Token.class);
        assertThat(((Parser.Token)e.get(0)).getCurr()).isInstanceOf(StreamStartToken.class);
        assertThat(e.get(1)).isInstanceOf(Parser.Token.class);
        assertThat(((Parser.Token)e.get(1)).getCurr()).isInstanceOf(DocumentStartToken.class);
        assertThat(e.get(2)).isInstanceOf(Parser.Line.class);
        assertThat(e.get(3)).isInstanceOf(Parser.Token.class);
        assertThat(((Parser.Token)e.get(3)).getCurr()).isInstanceOf(BlockMappingStartToken.class);
        assertThat(e.get(4)).isInstanceOf(Parser.Token.class);
        assertThat(((Parser.Token)e.get(4)).getCurr()).isInstanceOf(KeyToken.class);
        assertThat(e.get(6)).isInstanceOf(Parser.Token.class);
        assertThat(((Parser.Token)e.get(6)).getCurr()).isInstanceOf(ValueToken.class);
        assertThat(e.get(8)).isInstanceOf(Parser.Comment.class);
        assertThat(e.get(9)).isInstanceOf(Parser.Line.class);
        assertThat(e.get(12)).isInstanceOf(Parser.Line.class);
    }

    @Test
    void commentEquals() {
        String buffer = """
                        ---
                        k: v  # k=v
                        """;
        List<Parser.Lined> e = Parser.getTokensOrCommentsOrLines(buffer);
        assertThat(e.get(8)).isInstanceOf(Parser.Comment.class);
        assertThat(e.get(4)).isNotEqualTo(e.get(8));
        assertThat(e.get(8)).isEqualTo(new Parser.Comment(2, 7, buffer, 10, null, null, null));
    }
}
