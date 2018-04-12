package org.eclipse.tracecompass.incubator.atrace.event;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfCallsite;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfSourceLookup;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
/**
 * Systrace process dump event
 *
 * @author Guillaume Champagne
 * @author Alexis-Maurer Fortin
 * @author Hugo Genesse
 * @author Pierre-Yves Lajoie
 * @author Eva Terriault
 */
public class SystraceProcessDumpEvent  extends TmfEvent implements ITmfSourceLookup {


    private @NonNull final String fName;
    private final @Nullable ITmfCallsite fCallsite;

    /**
     * Constructor
     *
     * @param trace
     *            the trace
     * @param rank
     *            the rank
     * @param field
     *            the event field, contains all the needed data
     */
    public SystraceProcessDumpEvent(ITmfTrace trace,
            long rank,
            ITmfTimestamp timestamp,
            SystraceProcessDumpEventField field) {
        super(trace, rank, timestamp, null /* TODO: not sure if OK */, field);
         //TmfTimestamp.fromNanos(field.getTs()), SystraceProcessDumpEventTypeFactory.get(field.getName()), field.getContent());
        fName = ISystraceProcessDumpConstants.DEFAULT_SYSTRACE_PROCESS_DUMP_EVENT_NAME;
        fCallsite = null;

    }
    /**
     * Get the name of the event
     *
     * @return the name of the event
     */
    @Override
    public @NonNull String getName() {
        return fName;
    }

    @Override
    public @Nullable ITmfCallsite getCallsite() {
        return fCallsite;
    }
}
