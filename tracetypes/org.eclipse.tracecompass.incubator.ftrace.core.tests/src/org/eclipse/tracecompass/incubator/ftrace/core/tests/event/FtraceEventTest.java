package org.eclipse.tracecompass.incubator.ftrace.core.tests.event;

import org.eclipse.tracecompass.incubator.internal.ftrace.core.event.FtraceEvent;
import org.eclipse.tracecompass.incubator.internal.ftrace.core.event.FtraceField;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FtraceEventTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testParseLine() {
        String line = "kworker/0:0-9514  [000] d..4  3210.263482: sched_wakeup: comm=daemonsu pid=16620 prio=120 success=1 target_cpu=000";
        FtraceField field = FtraceEvent.parseLine(line);

        assertEquals(0, field.getCpu().intValue());
        Integer pid = field.getPid();
        assertNotNull(pid);
        assertEquals(9514, pid.intValue());
        Integer tid = field.getTid();
        assertNotNull(tid);
        assertEquals(9514, tid.intValue());
        assertEquals(3210263482000L, (long) field.getTs());
        assertEquals("sched_wakeup", field.getName());

        assertEquals(8, field.getContent().getFields().size());
        assertEquals("sched_wakeup", field.getContent().getFieldValue(String.class, "name"));
        assertEquals("daemonsu", field.getContent().getFieldValue(String.class, "comm"));
        Long fieldValue = field.getContent().getFieldValue(Long.class, "success");
        assertNotNull(fieldValue);
        assertEquals(1L, fieldValue.longValue());
        assertEquals((Long) 16620L, field.getContent().getFieldValue(Long.class, "pid"));
        assertEquals((Long) 16620L, field.getContent().getFieldValue(Long.class, "tid"));
        assertEquals((Long) 3210263482000L, field.getContent().getFieldValue(Long.class, "ts"));
        assertEquals((Long) 120L, field.getContent().getFieldValue(Long.class, "prio"));
        assertEquals((Long) 0L, field.getContent().getFieldValue(Long.class, "target_cpu"));
    }

}
