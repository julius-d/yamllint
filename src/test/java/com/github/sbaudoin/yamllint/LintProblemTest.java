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

import static org.assertj.core.api.Assertions.assertThat;

class LintProblemTest {
    @Test
    void simpleProblem() {
        LintProblem problem = new LintProblem(1, 2, "desc");
        problem.setLevel(Linter.ERROR_LEVEL);
        assertThat(problem.getRuleId()).isNull();
        assertThat(problem.getLine()).isEqualTo(1);
        assertThat(problem.getColumn()).isEqualTo(2);
        assertThat(problem.getLevel()).isEqualTo(Linter.ERROR_LEVEL);
        assertThat(problem.getDesc()).isEqualTo("desc");
        assertThat(problem.getExtraDesc()).isNull();
        assertThat(problem.getMessage()).isEqualTo("desc");
        assertThat(problem.getLongMessage()).isEqualTo("desc");
        assertThat(problem.toString()).isEqualTo("1:2:desc");
        assertThat(problem).isEqualTo(new LintProblem(1, 2, null));
        assertThat(problem.hashCode()).isEqualTo(922381112);
    }

    @Test
    void completeProblem() {
        LintProblem problem = new LintProblem(1, 2, "desc", "rule-id");
        problem.setLevel(Linter.ERROR_LEVEL);
        assertThat(problem.getRuleId()).isEqualTo("rule-id");
        assertThat(problem.getLine()).isEqualTo(1);
        assertThat(problem.getColumn()).isEqualTo(2);
        assertThat(problem.getLevel()).isEqualTo(Linter.ERROR_LEVEL);
        assertThat(problem.getDesc()).isEqualTo("desc");
        assertThat(problem.getExtraDesc()).isNull();
        assertThat(problem.getMessage()).isEqualTo("desc (rule-id)");
        assertThat(problem.getLongMessage()).isEqualTo("desc (rule-id)");
        assertThat(problem.toString()).isEqualTo("1:2:desc (rule-id)");
        assertThat(problem).isEqualTo(new LintProblem(1, 2, null, "rule-id"));
        assertThat(problem.hashCode()).isEqualTo(-1290166725);
    }

    @Test
    void extraProblem() {
        LintProblem problem = new LintProblem(1, 2, "desc", "rule-id", "an extra desc");
        problem.setLevel(Linter.ERROR_LEVEL);
        assertThat(problem.getRuleId()).isEqualTo("rule-id");
        assertThat(problem.getLine()).isEqualTo(1);
        assertThat(problem.getColumn()).isEqualTo(2);
        assertThat(problem.getLevel()).isEqualTo(Linter.ERROR_LEVEL);
        assertThat(problem.getDesc()).isEqualTo("desc");
        assertThat(problem.getExtraDesc()).isEqualTo("an extra desc");
        assertThat(problem.getMessage()).isEqualTo("desc (rule-id)");
        assertThat(problem.getLongMessage()).isEqualTo("desc (rule-id)" + System.lineSeparator() + "an extra desc");
        assertThat(problem.toString()).isEqualTo("1:2:desc (rule-id)");
        assertThat(problem).isEqualTo(new LintProblem(1, 2, null, "rule-id"));
        assertThat(problem.hashCode()).isEqualTo(-1290166725);

        problem = new LintProblem(1, 2, "desc");
        problem.setExtraDesc("an extra desc");
        assertThat(problem.getLongMessage()).isEqualTo("desc" + System.lineSeparator() + "an extra desc");
    }

    @Test
    void problemNullDesc() {
        LintProblem problem = new LintProblem(1, 2, null);
        assertThat(problem.getDesc()).isEqualTo("<no description>");
        assertThat(problem.getMessage()).isEqualTo("<no description>");
        assertThat(problem.toString()).isEqualTo("1:2:<no description>");
        assertThat(problem).isEqualTo(new LintProblem(1, 2, null, null));
        assertThat(problem.hashCode()).isEqualTo(922381112);
    }

    @Test
    void problemNullDescNullRuleId() {
        LintProblem problem = new LintProblem(1, 2, null, null);
        assertThat(problem.getDesc()).isEqualTo("<no description>");
        assertThat(problem.getMessage()).isEqualTo("<no description>");
        assertThat(problem.toString()).isEqualTo("1:2:<no description>");
        assertThat(problem).isEqualTo(new LintProblem(1, 2, null, null));
        assertThat(problem.hashCode()).isEqualTo(922381112);
    }

    @Test
    void problemNullDescWithRuleId() {
        LintProblem problem = new LintProblem(1, 2, null, "rule-id");
        assertThat(problem.getDesc()).isEqualTo("<no description>");
        assertThat(problem.getMessage()).isEqualTo("<no description> (rule-id)");
        assertThat(problem.toString()).isEqualTo("1:2:<no description> (rule-id)");
        assertThat(problem).isEqualTo(new LintProblem(1, 2, null, "rule-id"));
        assertThat(problem.hashCode()).isEqualTo(-1290166725);
    }

    @Test
    void notEquals() {
        LintProblem problem = new LintProblem(1, 2, "desc", "rule-id");
        assertThat(problem.toString()).isNotEqualTo("some text");
    }
}