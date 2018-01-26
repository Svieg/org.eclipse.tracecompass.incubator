package org.eclipse.tracecompass.incubator.ftrace.core.tests.event;

import static org.junit.Assert.*;

import org.eclipse.tracecompass.incubator.internal.ftrace.core.event.FtraceEvent;
import org.eclipse.tracecompass.incubator.internal.ftrace.core.event.FtraceField;
import org.junit.Before;
import org.junit.Test;

public class FtraceEventTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testParseLine() {
        String line = "kworker/0:0-9514  [000] d..4  3210.263482: sched_wakeup: comm=daemonsu pid=16620 prio=120 success=1 target_cpu=000";
       FtraceField field = FtraceEvent.parseLine(line);
       assertEquals(field.getCpu(),"00");
       assertEquals(field.getPid(),"9514");
       assertEquals(field.getTs(),"3210.263482");
       assertEquals(field.getName(),"kworker");
    }

}
