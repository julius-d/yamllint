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

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FormatTest {
    @Test
    void format() {
        List<LintProblem> problems = Arrays.asList(new LintProblem(1, 2, null));
        String file = "/my/filename.yaml";

        assertThat(Format.format(file, problems, Format.OutputFormat.PARSABLE)).isEqualTo("/my/filename.yaml:1:2:::<no description>");

        assertThat(Format.format(file, problems, Format.OutputFormat.GITHUB)).isEqualTo(":: file=/my/filename.yaml,line=1,col=2::<no description>");

        assertThat(Format.format(file, problems, Format.OutputFormat.STANDARD)).isEqualTo(file + System.lineSeparator() + "  1:2                <no description>" + System.lineSeparator());

        assertThat(Format.format(file, problems, Format.OutputFormat.COLORED)).isEqualTo("\u001B[4m" + file + "\u001B[0m" + System.lineSeparator() + "  \u001B[2m1:2\u001B[0m                         <no description>" + System.lineSeparator());

        assertThat(Format.format(file, problems, Format.OutputFormat.AUTO)).isEqualTo(file + System.lineSeparator() + "  1:2                <no description>" + System.lineSeparator());
    }

    @Test
    void parsable() {
        LintProblem problem = new LintProblem(1, 2, null);
        assertThat(Format.parsable(problem, "/my/filename.yaml")).isEqualTo("/my/filename.yaml:1:2:::<no description>");

        problem = new LintProblem(1, 2, "desc");
        problem.setLevel(Linter.INFO_LEVEL);
        assertThat(Format.parsable(problem, "/my/filename.yaml")).isEqualTo("/my/filename.yaml:1:2::info:desc");

        problem = new LintProblem(1, 2, null, "rule-id");
        assertThat(Format.parsable(problem, "/my/filename.yaml")).isEqualTo("/my/filename.yaml:1:2:rule-id::<no description>");

        problem = new LintProblem(1, 2, null, "rule-id", "extra desc");
        assertThat(Format.parsable(problem, "/my/filename.yaml")).isEqualTo("/my/filename.yaml:1:2:rule-id::<no description>");
    }

    @Test
    void github() {
        LintProblem problem = new LintProblem(1, 2, null);
        assertThat(Format.github(problem, "/my/filename.yaml")).isEqualTo(":: file=/my/filename.yaml,line=1,col=2::<no description>");

        problem = new LintProblem(1, 2, "desc");
        problem.setLevel(Linter.INFO_LEVEL);
        assertThat(Format.github(problem, "/my/filename.yaml")).isEqualTo("::info file=/my/filename.yaml,line=1,col=2::desc");

        problem = new LintProblem(1, 2, null, "rule-id");
        assertThat(Format.github(problem, "/my/filename.yaml")).isEqualTo(":: file=/my/filename.yaml,line=1,col=2::[rule-id] <no description>");

        problem = new LintProblem(1, 2, null, "rule-id", "extra desc");
        assertThat(Format.github(problem, "/my/filename.yaml")).isEqualTo(":: file=/my/filename.yaml,line=1,col=2::[rule-id] <no description>");
    }

    @Test
    void standard() {
        LintProblem problem = new LintProblem(1, 2, null);
        assertThat(Format.standard(problem)).isEqualTo("  1:2                <no description>");

        problem = new LintProblem(1, 2, "desc");
        problem.setLevel(Linter.INFO_LEVEL);
        assertThat(Format.standard(problem)).isEqualTo("  1:2       info     desc");

        problem = new LintProblem(1, 2, null, "rule-id");
        assertThat(Format.standard(problem)).isEqualTo("  1:2                <no description>  (rule-id)");

        problem = new LintProblem(1, 2, null, "rule-id", "extra desc\nwith lines");
        assertThat(Format.standard(problem)).isEqualTo("  1:2                <no description>  (rule-id)" + System.lineSeparator() +
                "                     extra desc" + System.lineSeparator() + "                     with lines");
    }

    @Test
    void standardColor() {
        LintProblem problem = new LintProblem(1, 2, null);
        assertThat(Format.standardColor(problem)).isEqualTo("  \u001B[2m1:2\u001B[0m                         <no description>");

        problem = new LintProblem(1, 2, "desc");
        problem.setLevel(Linter.INFO_LEVEL);
        assertThat(Format.standardColor(problem)).isEqualTo("  \u001B[2m1:2\u001B[0m       info              desc");
        problem = new LintProblem(1, 2, "desc");
        problem.setLevel(Linter.WARNING_LEVEL);
        assertThat(Format.standardColor(problem)).isEqualTo("  \u001B[2m1:2\u001B[0m       \u001B[33mwarning\u001B[0m  desc");

        problem = new LintProblem(1, 2, "desc");
        problem.setLevel(Linter.ERROR_LEVEL);
        assertThat(Format.standardColor(problem)).isEqualTo("  \u001B[2m1:2\u001B[0m       \u001B[31merror\u001B[0m    desc");

        problem = new LintProblem(1, 2, null, "rule-id");
        assertThat(Format.standardColor(problem)).isEqualTo("  \u001B[2m1:2\u001B[0m                         <no description>  \u001B[2m(rule-id)\u001B[0m");

        problem = new LintProblem(1, 2, null, "rule-id", "extra desc\nwith lines");
        assertThat(Format.standardColor(problem)).isEqualTo("  \u001B[2m1:2\u001B[0m                         <no description>  \u001B[2m(rule-id)\u001B[0m" +
                System.lineSeparator() + "                     extra desc" + System.lineSeparator() + "                     with lines");
    }

    // Hard to test in non-interactive, multi-platform build...
    @Test
    void supportsColor() {
        // Save platform name for future restoration
        String pf = System.getProperty("os.name");

        // Colors not supported on Windows platform
        System.setProperty("os.name", "Windows");
        assertThat(Format.supportsColor()).isFalse();

        // On other platform, it depends
        System.setProperty("os.name", "foo");
        if (System.console() == null) {
            assertThat(Format.supportsColor()).isFalse();
        } else if (System.getenv("ANSICON") != null || (System.getenv("TERM") != null && "ANSI".equals(System.getenv("TERM")))) {
            assertThat(Format.supportsColor()).isTrue();
        } else {
            assertThat(Format.supportsColor()).isFalse();
        }

        // Restore platform
        System.setProperty("os.name", pf);
    }

    @Test
    void getFiller() {
        assertThat(Format.getFiller(4)).isEqualTo("    ");
    }

    @Test
    void repeat() {
        assertThat(Format.repeat(10, "ab")).isEqualTo("abababababababababab");
    }
}