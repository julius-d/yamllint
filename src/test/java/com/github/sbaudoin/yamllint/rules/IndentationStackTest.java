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

import com.github.sbaudoin.yamllint.Parser;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IndentationStackTest {
    /**
     * Transform the stack at a given moment into a printable string like:
     * <pre>B_MAP:0 KEY:0 VAL:5</pre>
     */
    public String formatStack(List<?> stack) {
        return stack.stream().filter(e -> stack.indexOf(e) > 0).map(Object::toString).collect(Collectors.joining(" "));
    }

    public String fullStack(String source) {
        Map<Object, Object> conf = new HashMap<Object, Object>() {
            {
                put("spaces", 2);
                put("indent-sequences", true);
                put("check-multi-line-strings", false);
            }
        };

        Map<String, Object> context = new HashMap<>();
        StringBuilder output = new StringBuilder();
        for (Parser.Lined elem : Parser.getTokensOrComments(source).stream().filter(t -> !(t instanceof Parser.Comment)).collect(Collectors.toList())) {
            // Get the context
            new Indentation().check(conf, ((Parser.Token)elem).getCurr(), ((Parser.Token)elem).getPrev(), ((Parser.Token)elem).getNext(), ((Parser.Token)elem).getNextNext(), context);

            String tokenType = ((Parser.Token)elem).getCurr().getClass().getSimpleName()
                    .replaceAll("Token", "")
                    .replaceAll("Block", "B").replaceAll("Flow", "F")
                    .replaceAll("Sequence", "Seq")
                    .replaceAll("Mapping", "Map");
            if ("StreamStart".equals(tokenType) || "StreamEnd".equals(tokenType)) {
                continue;
            }
            output.append(String.format("%9s %s\n", tokenType, formatStack((List<?>)context.get("stack"))));
        }

        return output.toString();
    }

    @Test
    void testSimpleMapping() {
        assertEquals(
                """
                BMapStart B_MAP:0
                      Key B_MAP:0 KEY:0
                   Scalar B_MAP:0 KEY:0
                    Value B_MAP:0 KEY:0 VAL:5
                   Scalar B_MAP:0
                     BEnd\s
                """,
                fullStack("key: val\n"));

        assertEquals(
                """
                BMapStart B_MAP:5
                      Key B_MAP:5 KEY:5
                   Scalar B_MAP:5 KEY:5
                    Value B_MAP:5 KEY:5 VAL:10
                   Scalar B_MAP:5
                     BEnd\s
                """,
                fullStack("     key: val\n"));
    }

    @Test
    void testSimpleSequence() {
        assertEquals(
                """
                BSeqStart B_SEQ:0
                   BEntry B_SEQ:0 B_ENT:2
                   Scalar B_SEQ:0
                   BEntry B_SEQ:0 B_ENT:2
                   Scalar B_SEQ:0
                   BEntry B_SEQ:0 B_ENT:2
                   Scalar B_SEQ:0
                     BEnd\s
                """,
                fullStack("""
                          - 1
                          - 2
                          - 3
                          """));

        assertEquals(
                """
                BMapStart B_MAP:0
                      Key B_MAP:0 KEY:0
                   Scalar B_MAP:0 KEY:0
                    Value B_MAP:0 KEY:0 VAL:2
                BSeqStart B_MAP:0 KEY:0 VAL:2 B_SEQ:2
                   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:2 B_ENT:4
                   Scalar B_MAP:0 KEY:0 VAL:2 B_SEQ:2
                   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:2 B_ENT:4
                   Scalar B_MAP:0 KEY:0 VAL:2 B_SEQ:2
                     BEnd B_MAP:0
                     BEnd\s
                """,
                fullStack("""
                          key:
                            - 1
                            - 2
                          """));
    }

    @Test
    void testNonIndentedSequences() {
            /* There seems to be a bug in snakeyaml: depending on the indentation, a
               sequence does not produce the same tokens. More precisely, the
               following YAML:
                   usr:
                     - lib
               produces a BlockSequenceStartToken and a BlockEndToken around the
               "lib" sequence, whereas the following:
                   usr:
                     - lib
               does not (both two tokens are omitted).
               So, yamllint must create fake 'B_SEQ'. This test makes sure it does. */

                assertEquals(
                        """
                        BMapStart B_MAP:0
                              Key B_MAP:0 KEY:0
                           Scalar B_MAP:0 KEY:0
                            Value B_MAP:0 KEY:0 VAL:2
                        BSeqStart B_MAP:0 KEY:0 VAL:2 B_SEQ:2
                           BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:2 B_ENT:4
                           Scalar B_MAP:0 KEY:0 VAL:2 B_SEQ:2
                             BEnd B_MAP:0
                              Key B_MAP:0 KEY:0
                           Scalar B_MAP:0 KEY:0
                            Value B_MAP:0 KEY:0 VAL:5
                           Scalar B_MAP:0
                             BEnd\s
                        """,
                        fullStack("""
                                  usr:
                                    - lib
                                  var: cache
                                  """));

        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:2\n" +
                // missing BSeqStart here
                "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2\n" +
                "   Scalar B_MAP:0\n" +
                // missing BEnd here
                "     BEnd \n",
                fullStack("""
                          usr:
                          - lib
                          """));

        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:2\n" +
                // missing BSeqStart here
                "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2\n" +
                "   Scalar B_MAP:0\n" +
                // missing BEnd here
                "      Key B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:5\n" +
                "   Scalar B_MAP:0\n" +
                "     BEnd \n",
                fullStack("""
                          usr:
                          - lib
                          var: cache
                          """));

        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:2\n" +
                // missing BSeqStart here
                "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2\n" +
                "FSeqStart B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 F_SEQ:3\n" +
                "  FSeqEnd B_MAP:0\n" +
                // missing BEnd here
                "     BEnd \n",
                fullStack("""
                          usr:
                          - []
                          """));

        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:2\n" +
                // missing BSeqStart here
                "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2\n" +
                "BMapStart B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 B_MAP:2\n" +
                "      Key B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 B_MAP:2 KEY:2\n" +
                "   Scalar B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 B_MAP:2 KEY:2\n" +
                "    Value B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 B_MAP:2 KEY:2 VAL:4\n" +  // noqa
                "   Scalar B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 B_MAP:2\n" +
                "     BEnd B_MAP:0\n" +
                // missing BEnd here
                "     BEnd \n",
                fullStack("""
                          usr:
                          - k:
                              v
                          """));
    }

    @Test
    void testFlows() {
        assertEquals(
                """
                BMapStart B_MAP:0
                      Key B_MAP:0 KEY:0
                   Scalar B_MAP:0 KEY:0
                    Value B_MAP:0 KEY:0 VAL:5
                FSeqStart B_MAP:0 KEY:0 VAL:5 F_SEQ:2
                FMapStart B_MAP:0 KEY:0 VAL:5 F_SEQ:2 F_MAP:3
                      Key B_MAP:0 KEY:0 VAL:5 F_SEQ:2 F_MAP:3 KEY:3
                   Scalar B_MAP:0 KEY:0 VAL:5 F_SEQ:2 F_MAP:3 KEY:3
                    Value B_MAP:0 KEY:0 VAL:5 F_SEQ:2 F_MAP:3 KEY:3 VAL:5
                   Scalar B_MAP:0 KEY:0 VAL:5 F_SEQ:2 F_MAP:3
                  FMapEnd B_MAP:0 KEY:0 VAL:5 F_SEQ:2
                  FSeqEnd B_MAP:0
                     BEnd\s
                """,
                fullStack("""
                          usr: [
                            {k:
                              v}
                            ]
                          """));
    }

    @Test
    void testAnchors() {
        assertEquals(
                """
                BMapStart B_MAP:0
                      Key B_MAP:0 KEY:0
                   Scalar B_MAP:0 KEY:0
                    Value B_MAP:0 KEY:0 VAL:5
                   Anchor B_MAP:0 KEY:0 VAL:5
                   Scalar B_MAP:0
                     BEnd\s
                """,
                fullStack("key: &anchor value\n"));

        assertEquals(
                """
                BMapStart B_MAP:0
                      Key B_MAP:0 KEY:0
                   Scalar B_MAP:0 KEY:0
                    Value B_MAP:0 KEY:0 VAL:2
                   Anchor B_MAP:0 KEY:0 VAL:2
                   Scalar B_MAP:0
                     BEnd\s
                """,
                fullStack("""
                          key: &anchor
                            value
                          """));

        assertEquals(
                """
                BSeqStart B_SEQ:0
                   BEntry B_SEQ:0 B_ENT:2
                   Anchor B_SEQ:0 B_ENT:2
                   Scalar B_SEQ:0
                     BEnd\s
                """,
                fullStack("- &anchor value\n"));

        assertEquals(
                """
                BSeqStart B_SEQ:0
                   BEntry B_SEQ:0 B_ENT:2
                   Anchor B_SEQ:0 B_ENT:2
                   Scalar B_SEQ:0
                     BEnd\s
                """,
                fullStack("""
                          - &anchor
                            value
                          """));

        assertEquals(
                """
                BSeqStart B_SEQ:0
                   BEntry B_SEQ:0 B_ENT:2
                   Anchor B_SEQ:0 B_ENT:2
                BSeqStart B_SEQ:0 B_ENT:2 B_SEQ:2
                   BEntry B_SEQ:0 B_ENT:2 B_SEQ:2 B_ENT:4
                   Scalar B_SEQ:0 B_ENT:2 B_SEQ:2
                   BEntry B_SEQ:0 B_ENT:2 B_SEQ:2 B_ENT:4
                   Scalar B_SEQ:0 B_ENT:2 B_SEQ:2
                     BEnd B_SEQ:0
                     BEnd\s
                """,
                fullStack("""
                          - &anchor
                            - 1
                            - 2
                          """));

        assertEquals(
                """
                BMapStart B_MAP:0
                      Key B_MAP:0 KEY:0
                   Anchor B_MAP:0 KEY:0
                   Scalar B_MAP:0 KEY:0
                    Value B_MAP:0 KEY:0 VAL:2
                   Scalar B_MAP:0
                     BEnd\s
                """,
                fullStack("""
                          &anchor key:
                            value
                          """));

        assertEquals(
                """
                BMapStart B_MAP:0
                      Key B_MAP:0 KEY:0
                   Scalar B_MAP:0 KEY:0
                    Value B_MAP:0 KEY:0 VAL:2
                   Anchor B_MAP:0 KEY:0 VAL:2
                   Scalar B_MAP:0
                      Key B_MAP:0 KEY:0
                   Anchor B_MAP:0 KEY:0
                   Scalar B_MAP:0 KEY:0
                    Value B_MAP:0 KEY:0 VAL:2
                   Scalar B_MAP:0
                     BEnd\s
                """,
                fullStack("""
                          pre:
                            &anchor1 0
                          &anchor2 key:
                            value
                          """));

        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:2\n" +
                "   Anchor B_MAP:0 KEY:0 VAL:2\n" +
                // missing BSeqStart here
                "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2\n" +
                "   Scalar B_MAP:0 KEY:0 VAL:2 B_SEQ:0\n" +
                "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2\n" +
                "   Anchor B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2\n" +
                "BSeqStart B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 B_SEQ:2\n" +
                "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 B_SEQ:2 B_ENT:4\n" +
                "   Scalar B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 B_SEQ:2\n" +
                "     BEnd B_MAP:0\n" +
                // missing BEnd here
                "     BEnd \n",
                fullStack("""
                          sequence: &anchor
                          - entry
                          - &anchor
                            - nested
                          """));
    }

    @Test
    void testTags() {
        assertEquals(
                """
                BMapStart B_MAP:0
                      Key B_MAP:0 KEY:0
                   Scalar B_MAP:0 KEY:0
                    Value B_MAP:0 KEY:0 VAL:5
                      Tag B_MAP:0 KEY:0 VAL:5
                   Scalar B_MAP:0
                     BEnd\s
                """,
                fullStack("key: !!tag value\n"));

        assertEquals(
                """
                BSeqStart B_SEQ:0
                   BEntry B_SEQ:0 B_ENT:2
                      Tag B_SEQ:0 B_ENT:2
                BMapStart B_SEQ:0 B_ENT:2 B_MAP:2
                      Key B_SEQ:0 B_ENT:2 B_MAP:2 KEY:2
                   Scalar B_SEQ:0 B_ENT:2 B_MAP:2 KEY:2
                    Value B_SEQ:0 B_ENT:2 B_MAP:2 KEY:2 VAL:8
                   Scalar B_SEQ:0 B_ENT:2 B_MAP:2
                     BEnd B_SEQ:0
                     BEnd\s
                """,
                fullStack("""
                          - !!map # Block collection
                            foo : bar
                          """));

        assertEquals(
                """
                BSeqStart B_SEQ:0
                   BEntry B_SEQ:0 B_ENT:2
                      Tag B_SEQ:0 B_ENT:2
                BSeqStart B_SEQ:0 B_ENT:2 B_SEQ:2
                   BEntry B_SEQ:0 B_ENT:2 B_SEQ:2 B_ENT:4
                   Scalar B_SEQ:0 B_ENT:2 B_SEQ:2
                     BEnd B_SEQ:0
                     BEnd\s
                """,
                fullStack("""
                          - !!seq
                            - nested item
                          """));

        assertEquals(
                "BMapStart B_MAP:0\n" +
                "      Key B_MAP:0 KEY:0\n" +
                "   Scalar B_MAP:0 KEY:0\n" +
                "    Value B_MAP:0 KEY:0 VAL:2\n" +
                "      Tag B_MAP:0 KEY:0 VAL:2\n" +
                // missing BSeqStart here
                "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2\n" +
                "   Scalar B_MAP:0 KEY:0 VAL:2 B_SEQ:0\n" +
                "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2\n" +
                "      Tag B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2\n" +
                "BSeqStart B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 B_SEQ:2\n" +
                "   BEntry B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 B_SEQ:2 B_ENT:4\n" +
                "   Scalar B_MAP:0 KEY:0 VAL:2 B_SEQ:0 B_ENT:2 B_SEQ:2\n" +
                "     BEnd B_MAP:0\n" +
                // missing BEnd here
                "     BEnd \n",
                fullStack("""
                          sequence: !!seq
                          - entry
                          - !!seq
                            - nested
                          """));
    }

    @Test
    void testFlowsImbrication() {
        assertEquals(
                """
                FSeqStart F_SEQ:1
                FSeqStart F_SEQ:1 F_SEQ:2
                   Scalar F_SEQ:1 F_SEQ:2
                  FSeqEnd F_SEQ:1
                  FSeqEnd\s
                """,
                fullStack("[[val]]\n"));

        assertEquals(
                """
                FSeqStart F_SEQ:1
                FSeqStart F_SEQ:1 F_SEQ:2
                   Scalar F_SEQ:1 F_SEQ:2
                  FSeqEnd F_SEQ:1
                   FEntry F_SEQ:1
                FSeqStart F_SEQ:1 F_SEQ:9
                   Scalar F_SEQ:1 F_SEQ:9
                  FSeqEnd F_SEQ:1
                  FSeqEnd\s
                """,
                fullStack("[[val], [val2]]\n"));

        assertEquals(
                """
                FMapStart F_MAP:1
                FMapStart F_MAP:1 F_MAP:2
                   Scalar F_MAP:1 F_MAP:2
                  FMapEnd F_MAP:1
                  FMapEnd\s
                """,
                fullStack("{{key}}\n"));

        assertEquals(
                """
                BMapStart B_MAP:0
                      Key B_MAP:0 KEY:0
                FSeqStart B_MAP:0 KEY:0 F_SEQ:1
                   Scalar B_MAP:0 KEY:0 F_SEQ:1
                  FSeqEnd B_MAP:0 KEY:0
                    Value B_MAP:0 KEY:0 VAL:7
                   Scalar B_MAP:0
                     BEnd\s
                """,
                fullStack("[key]: value\n"));

        assertEquals(
                """
                BMapStart B_MAP:0
                      Key B_MAP:0 KEY:0
                FSeqStart B_MAP:0 KEY:0 F_SEQ:1
                FSeqStart B_MAP:0 KEY:0 F_SEQ:1 F_SEQ:2
                   Scalar B_MAP:0 KEY:0 F_SEQ:1 F_SEQ:2
                  FSeqEnd B_MAP:0 KEY:0 F_SEQ:1
                  FSeqEnd B_MAP:0 KEY:0
                    Value B_MAP:0 KEY:0 VAL:9
                   Scalar B_MAP:0
                     BEnd\s
                """,
                fullStack("[[key]]: value\n"));

        assertEquals(
                """
                BMapStart B_MAP:0
                      Key B_MAP:0 KEY:0
                FMapStart B_MAP:0 KEY:0 F_MAP:1
                   Scalar B_MAP:0 KEY:0 F_MAP:1
                  FMapEnd B_MAP:0 KEY:0
                    Value B_MAP:0 KEY:0 VAL:7
                   Scalar B_MAP:0
                     BEnd\s
                """,
                fullStack("{key}: value\n"));

        assertEquals(
                """
                BMapStart B_MAP:0
                      Key B_MAP:0 KEY:0
                FMapStart B_MAP:0 KEY:0 F_MAP:1
                      Key B_MAP:0 KEY:0 F_MAP:1 KEY:1
                   Scalar B_MAP:0 KEY:0 F_MAP:1 KEY:1
                    Value B_MAP:0 KEY:0 F_MAP:1 KEY:1 VAL:6
                   Scalar B_MAP:0 KEY:0 F_MAP:1
                  FMapEnd B_MAP:0 KEY:0
                    Value B_MAP:0 KEY:0 VAL:14
                   Scalar B_MAP:0
                     BEnd\s
                """,
                fullStack("{key: value}: value\n"));

        assertEquals(
                """
                BMapStart B_MAP:0
                      Key B_MAP:0 KEY:0
                FMapStart B_MAP:0 KEY:0 F_MAP:1
                FMapStart B_MAP:0 KEY:0 F_MAP:1 F_MAP:2
                   Scalar B_MAP:0 KEY:0 F_MAP:1 F_MAP:2
                  FMapEnd B_MAP:0 KEY:0 F_MAP:1
                  FMapEnd B_MAP:0 KEY:0
                    Value B_MAP:0 KEY:0 VAL:9
                   Scalar B_MAP:0
                     BEnd\s
                """,
                fullStack("{{key}}: value\n"));
        assertEquals(
                """
                FMapStart F_MAP:1
                      Key F_MAP:1 KEY:1
                FMapStart F_MAP:1 KEY:1 F_MAP:2
                   Scalar F_MAP:1 KEY:1 F_MAP:2
                  FMapEnd F_MAP:1 KEY:1
                    Value F_MAP:1 KEY:1 VAL:8
                   Scalar F_MAP:1
                   FEntry F_MAP:1
                      Key F_MAP:1 KEY:1
                FMapStart F_MAP:1 KEY:1 F_MAP:14
                   Scalar F_MAP:1 KEY:1 F_MAP:14
                  FMapEnd F_MAP:1 KEY:1
                    Value F_MAP:1 KEY:1 VAL:21
                FMapStart F_MAP:1 KEY:1 VAL:21 F_MAP:22
                   Scalar F_MAP:1 KEY:1 VAL:21 F_MAP:22
                  FMapEnd F_MAP:1
                  FMapEnd\s
                """,
                fullStack("{{key}: val, {key2}: {val2}}\n"));

        assertEquals(
                """
                FMapStart F_MAP:1
                FSeqStart F_MAP:1 F_SEQ:2
                FMapStart F_MAP:1 F_SEQ:2 F_MAP:3
                FMapStart F_MAP:1 F_SEQ:2 F_MAP:3 F_MAP:4
                FSeqStart F_MAP:1 F_SEQ:2 F_MAP:3 F_MAP:4 F_SEQ:5
                   Scalar F_MAP:1 F_SEQ:2 F_MAP:3 F_MAP:4 F_SEQ:5
                  FSeqEnd F_MAP:1 F_SEQ:2 F_MAP:3 F_MAP:4
                  FMapEnd F_MAP:1 F_SEQ:2 F_MAP:3
                  FMapEnd F_MAP:1 F_SEQ:2
                   FEntry F_MAP:1 F_SEQ:2
                FSeqStart F_MAP:1 F_SEQ:2 F_SEQ:14
                FMapStart F_MAP:1 F_SEQ:2 F_SEQ:14 F_MAP:15
                      Key F_MAP:1 F_SEQ:2 F_SEQ:14 F_MAP:15 KEY:15
                FSeqStart F_MAP:1 F_SEQ:2 F_SEQ:14 F_MAP:15 KEY:15 F_SEQ:16
                   Scalar F_MAP:1 F_SEQ:2 F_SEQ:14 F_MAP:15 KEY:15 F_SEQ:16
                  FSeqEnd F_MAP:1 F_SEQ:2 F_SEQ:14 F_MAP:15 KEY:15
                    Value F_MAP:1 F_SEQ:2 F_SEQ:14 F_MAP:15 KEY:15 VAL:22
                   Scalar F_MAP:1 F_SEQ:2 F_SEQ:14 F_MAP:15
                  FMapEnd F_MAP:1 F_SEQ:2 F_SEQ:14
                  FSeqEnd F_MAP:1 F_SEQ:2
                  FSeqEnd F_MAP:1
                  FMapEnd\s
                """,
                fullStack("{[{{[val]}}, [{[key]: val2}]]}\n"));
    }
}
