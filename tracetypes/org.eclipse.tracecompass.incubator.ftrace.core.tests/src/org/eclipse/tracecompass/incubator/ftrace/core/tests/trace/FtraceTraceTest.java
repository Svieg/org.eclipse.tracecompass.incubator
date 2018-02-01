package org.eclipse.tracecompass.incubator.ftrace.core.tests.trace;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.tracecompass.incubator.internal.ftrace.core.trace.FtraceTrace;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FtraceTraceTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testValidate() {

        FtraceTrace ftraceTrace = new FtraceTrace();

        IStatus status = ftraceTrace.validate(null,"res/android_ftrace_trace_output_14_01_18");

        assertEquals(0, status.getSeverity());

    }

    @Test
    public void testValidateFileDoesNotExist() {
        FtraceTrace ftraceTrace = new FtraceTrace();

        IStatus status = ftraceTrace.validate(null,"");

        assertEquals(0x04, status.getSeverity());
    }

    @Test
    public void testValidateDirectory() {
        FtraceTrace ftraceTrace = new FtraceTrace();

        IStatus status = ftraceTrace.validate(null,"res/");

        assertEquals(0x04, status.getSeverity());
    }
}
