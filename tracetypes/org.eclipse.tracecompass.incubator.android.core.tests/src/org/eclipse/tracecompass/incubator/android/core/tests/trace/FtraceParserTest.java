package org.eclipse.tracecompass.incubator.android.core.tests.trace;

import static org.junit.Assert.*;

import org.eclipse.tracecompass.incubator.internal.android.core.trace.FtraceParser;
import org.junit.Before;
import org.junit.Test;

/**
 * Testing of ftrace output parsing
 *
 * @author Alexis-Maurer Fortin
 *
 */
public class FtraceParserTest {

    @Before
    public void setUp() throws Exception {
    }

    /**
     *
     */
    @Test
    public void ftraceFileParseTest() {
        FtraceParser ftraceParser = new FtraceParser("res/android_ftrace_trace_output_14_01_18");
        ftraceParser.parse();
    }

}
