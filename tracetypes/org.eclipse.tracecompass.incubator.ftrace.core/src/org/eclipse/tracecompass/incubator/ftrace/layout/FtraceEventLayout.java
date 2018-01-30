package org.eclipse.tracecompass.incubator.ftrace.layout;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.DefaultEventLayout;

/**
 * Event and field definitions for ftrace traces.
 * @author Guillaume Champagne, Alexis-Maurer Fortin, Hugo Genesse, Pierre-Yves
 *          Lajoie, Eva Terriault
 *
 */
public class FtraceEventLayout extends DefaultEventLayout {

    private static @Nullable FtraceEventLayout INSTANCE;

    /**
     * The instance of this event layout
     *
     * This object is completely immutable, so no need to create additional
     * instances via the constructor.
     *
     * @return the instance
     */
    public static synchronized DefaultEventLayout getInstance() {
        FtraceEventLayout inst = INSTANCE;
        if (inst == null) {
            inst = new FtraceEventLayout();
            INSTANCE = inst;
        }
        return inst;
    }

    /* Field names */
    private static final String NEXT_PID = "next_pid"; //$NON-NLS-1$
    private static final String PREV_PID = "prev_pid"; //$NON-NLS-1$

    @Override
    public String fieldNextTid() {
        return NEXT_PID;
    }

    @Override
    public String fieldPrevTid() {
        return PREV_PID;
    }
}
