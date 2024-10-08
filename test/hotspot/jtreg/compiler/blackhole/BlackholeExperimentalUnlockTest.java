/*
 * Copyright (c) 2021, Red Hat, Inc. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/**
 * @test
 * @library /test/lib /
 * @requires vm.flagless
 * @requires ! vm.opt.final.UnlockExperimentalVMOptions
 * @requires vm.compMode != "Xint"
 * @run driver compiler.blackhole.BlackholeExperimentalUnlockTest
 */

package compiler.blackhole;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import jdk.test.lib.Platform;
import jdk.test.lib.process.ProcessTools;
import jdk.test.lib.process.OutputAnalyzer;

public class BlackholeExperimentalUnlockTest {

    private static final int CYCLES = 100_000;
    private static final int TRIES = 10;

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            driver();
        } else {
            runner();
        }
    }

    private static final String MSG = "Blackhole compile option is experimental and must be enabled via -XX:+UnlockExperimentalVMOptions";

    private static List<String> cmdline(String[] args) {
        List<String> r = new ArrayList();
        r.add("-Xmx128m");
        r.add("-Xbatch");
        r.addAll(Arrays.asList(args));
        r.add("compiler.blackhole.BlackholeExperimentalUnlockTest");
        r.add("run");
        return r;
    }

    public static void shouldFail(String... args) throws IOException {
        ProcessBuilder pb = ProcessTools.createLimitedTestJavaProcessBuilder(cmdline(args));
        OutputAnalyzer output = new OutputAnalyzer(pb.start());
        output.shouldHaveExitValue(0);
        output.shouldContain(MSG);
    }

    public static void shouldPass(String... args) throws IOException {
        ProcessBuilder pb = ProcessTools.createLimitedTestJavaProcessBuilder(cmdline(args));
        OutputAnalyzer output = new OutputAnalyzer(pb.start());
        output.shouldHaveExitValue(0);
        output.shouldNotContain(MSG);
    }

    public static void driver() throws IOException {
        // Option is disabled by default, should fail:
        shouldFail(
            "-XX:CompileCommand=quiet",
            "-XX:CompileCommand=option,compiler/blackhole/BlackholeTarget.bh_*,Blackhole"
        );
        shouldFail(
            "-XX:CompileCommand=quiet",
            "-XX:CompileCommand=blackhole,compiler/blackhole/BlackholeTarget.bh_*"
        );

        // Option should be enabled by UnlockExperimentalVMOptions
        shouldPass(
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:CompileCommand=quiet",
            "-XX:CompileCommand=option,compiler/blackhole/BlackholeTarget.bh_*,Blackhole"
        );
        shouldPass(
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:CompileCommand=quiet",
            "-XX:CompileCommand=blackhole,compiler/blackhole/BlackholeTarget.bh_*"
        );

        // Should be able to shun the warning
        shouldPass(
            "-XX:-PrintWarnings",
            "-XX:CompileCommand=quiet",
            "-XX:CompileCommand=option,compiler/blackhole/BlackholeTarget.bh_*,Blackhole"
        );
        shouldPass(
            "-XX:-PrintWarnings",
            "-XX:CompileCommand=quiet",
            "-XX:CompileCommand=blackhole,compiler/blackhole/BlackholeTarget.bh_*"
        );
    }

    public static void runner() {
        for (int t = 0; t < TRIES; t++) {
            run();
        }
    }

    public static void run() {
        for (int c = 0; c < CYCLES; c++) {
            BlackholeTarget.bh_s_int_1(c);
        }
    }

}
