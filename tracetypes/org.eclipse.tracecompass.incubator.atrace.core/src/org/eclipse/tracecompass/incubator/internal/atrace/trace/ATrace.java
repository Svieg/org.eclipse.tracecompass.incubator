/*******************************************************************************
 * Copyright (c) 2018 Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.atrace.trace;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.incubator.atrace.event.ISystraceProcessDumpConstants;
import org.eclipse.tracecompass.incubator.atrace.event.SystraceProcessDumpEvent;
import org.eclipse.tracecompass.incubator.atrace.event.SystraceProcessDumpEventField;
import org.eclipse.tracecompass.incubator.internal.atrace.core.Activator;
import org.eclipse.tracecompass.incubator.internal.ftrace.core.event.GenericFtraceField;
import org.eclipse.tracecompass.incubator.internal.ftrace.core.event.IGenericFtraceConstants;
import org.eclipse.tracecompass.incubator.internal.ftrace.core.trace.GenericFtrace;
import org.eclipse.tracecompass.incubator.internal.traceevent.core.event.ITraceEventConstants;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.eclipse.tracecompass.tmf.core.trace.location.TmfLongLocation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Traces gathered via atrace.
 *
 * @author Guillaume Champagne
 * @author Alexis-Maurer Fortin
 * @author Hugo Genesse
 * @author Pierre-Yves Lajoie
 * @author Eva Terriault
 */
public class ATrace extends GenericFtrace {

    private static final String ATRACE_TRACEEVENT_EVENT = "tracing_mark_write"; //$NON-NLS-1$

    private static final String TRACE_EVENT_PHASE_GROUP = "phase"; //$NON-NLS-1$
    private static final String TRACE_EVENT_CONTENT_GROUP = "content"; //$NON-NLS-1$

    private static final int MAX_LINES = 100;
    private static final int MAX_CONFIDENCE = 100;

    private static final TmfLongLocation NULL_LOCATION = new TmfLongLocation(-1L);
    private static final TmfContext INVALID_CONTEXT = new TmfContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);

    //TODO: enum
    int parsing_state = 0; //0 : process dump, 1 : traces
    private long first_timestamp;
    List<SystraceProcessDumpEvent> processDumpEvents = new ArrayList<>();

    @Override
    public IStatus validate(IProject project, String path) {
        File file = new File(path);
        if (!file.exists()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "File not found: " + path); //$NON-NLS-1$
        }
        if (!file.isFile()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Not a file. It's a directory: " + path); //$NON-NLS-1$
        }
        int confidence = 0;
        try {
            if (!TmfTraceUtils.isText(file)) {
                return new TraceValidationStatus(confidence, Activator.PLUGIN_ID);
            }
        } catch (IOException e) {
            Activator.getInstance().logError("Error validating file: " + path, e); //$NON-NLS-1$
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "IOException validating file: " + path, e); //$NON-NLS-1$
        }
        try (BufferedRandomAccessFile rafile = new BufferedRandomAccessFile(path, "r")) { //$NON-NLS-1$
            int lineCount = 0;
            int matches = 0;
            String line = rafile.readLine();
            int function_call_count = 0;
            boolean isSystrace = false;
            while ((line != null) && (lineCount++ < MAX_LINES)) {
                if (line.contains("<title>Android System Trace</title>")) { //$NON-NLS-1$
                    confidence = MAX_CONFIDENCE;
                    isSystrace = true;
                    matches++;
                    break;
                }
                try {
                    GenericFtraceField field = this.parseLine(line);
                    if (field != null) {
                        matches++;
                        /*
                         * If the Tid and Pid are different, we know the event happened
                         * on a thread
                         */
                        if (field.getTid() != field.getPid()) {
                            function_call_count++;
                        }
                    }
                } catch (RuntimeException e) {
                    confidence = Integer.MIN_VALUE;
                }
                confidence = MAX_CONFIDENCE * matches / lineCount;
                line = rafile.readLine();
            }
            // We increase the confidence if there is function calls
            if (function_call_count > 0 || isSystrace) {
                confidence++;
            } else {
                confidence = 0;
            }
            if (matches == 0) {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Most assuredly NOT a atrace trace"); //$NON-NLS-1$
            }
        } catch (IOException e) {
            Activator.getInstance().logError("Error validating file: " + path, e); //$NON-NLS-1$
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "IOException validating file: " + path, e); //$NON-NLS-1$
        }
        return new TraceValidationStatus(confidence, Activator.PLUGIN_ID);

    }

    @Override
    public ITmfContext seekEvent(ITmfLocation location) {
        if (fFile == null) {
            return INVALID_CONTEXT;
        }
        final TmfContext context = new TmfContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
        if (NULL_LOCATION.equals(location)) {
            return context;
        }
        try {
            if (location == null) {
                fFileInput.seek(0);
                long lineStartOffset = fFileInput.getFilePointer();
                String line = fFileInput.readLine();

                //Look for process dump matches
                Matcher processDumpMatcher = ISystraceProcessDumpConstants.PROCESS_DUMP_PATTERN.matcher(line);
                Matcher atraceMatcher = IGenericFtraceConstants.FTRACE_PATTERN.matcher(line);

                line = fFileInput.readLine();
                while (!atraceMatcher.matches() && !processDumpMatcher.matches()) {
                    lineStartOffset = fFileInput.getFilePointer();
                    atraceMatcher = IGenericFtraceConstants.FTRACE_PATTERN.matcher(line);
                    processDumpMatcher = ISystraceProcessDumpConstants.PROCESS_DUMP_PATTERN.matcher(line);
                    line = fFileInput.readLine();
                }

             fFileInput.seek(lineStartOffset);
            } else if (location.getLocationInfo() instanceof Long) {
                fFileInput.seek((Long) location.getLocationInfo());
            }
            context.setLocation(new TmfLongLocation(fFileInput.getFilePointer()));
            context.setRank(0);
            return context;
        } catch (final java.lang.NullPointerException e) {
            Activator.getInstance().logError("Error seeking event. Null Pointer Exception: " + getPath(), e); //$NON-NLS-1$
            return context;
        } catch (final FileNotFoundException e) {
            Activator.getInstance().logError("Error seeking event. File not found: " + getPath(), e); //$NON-NLS-1$
            return context;
        } catch (final IOException e) {
            Activator.getInstance().logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
            return context;
        }

    }

    @Override
    public ITmfEvent parseEvent(ITmfContext context) {
        ITmfEvent event = super.parseEvent(context);
        if (event != null)
        {
            if (parsing_state == 0) { //Was in process dump state
                first_timestamp = event.getTimestamp().toNanos();
                updateTimestamps();
                parsing_state = 1;
            }
            return event;
        }
            //We might be in the process dump generated by systrace
            ITmfLocation location = context.getLocation();
            if (location instanceof TmfLongLocation) {
                TmfLongLocation tmfLongLocation = (TmfLongLocation) location;
                Long locationInfo = tmfLongLocation.getLocationInfo();
                if (location.equals(NULL_LOCATION)) {
                    locationInfo = 0L;
                }
                super.parseEvent(context);
                if (locationInfo != null) {
                    try {
                        if (!locationInfo.equals(fFileInput.getFilePointer())) {
                            fFileInput.seek(locationInfo);
                        }
                        String nextLine = fFileInput.readLine();

                        SystraceProcessDumpEventField field = SystraceProcessDumpEventField.parseLine(nextLine);
                        if (field != null) {
                            event = new SystraceProcessDumpEvent(this, context.getRank(), TmfTimestamp.fromNanos(1), field);
                            processDumpEvents.add((SystraceProcessDumpEvent) event);
                            return event;
                        }
                    } catch (IOException e) {
                        Activator.getInstance().logError("Error parsing event", e); //$NON-NLS-1$
                    }
                }
            }
            return event;
     }

    private void updateTimestamps() {
        // TODO Auto-generated method stub
        for (SystraceProcessDumpEvent e: processDumpEvents) {
            e.setTimestamp(TmfTimestamp.fromNanos(first_timestamp));
        }
    }

    @Override
    protected @Nullable GenericFtraceField parseLine(String line) {
        if (line == null || line.isEmpty()) {
            return null;
        }

        GenericFtraceField field = GenericFtraceField.parseLine(line);

        if (field != null) {
            Matcher matcher = IGenericFtraceConstants.FTRACE_PATTERN.matcher(line);
            if (matcher.matches()) {

                /*
                 * User spaces event that permit us to create the call stack are inserted in the
                 * raw trace. Those events are named 'tracing_mark_write'. The format in the
                 * "function" column is not like any other ftrace events, so we must handle them
                 * separately.
                 */
                if (field.getName().equals(ATRACE_TRACEEVENT_EVENT)) {
                    String data = matcher.group(IGenericFtraceConstants.FTRACE_DATA_GROUP);
                    Matcher atraceMatcher = ISystraceProcessDumpConstants.TRACE_EVENT_PATTERN.matcher(data);
                    if (atraceMatcher.matches()) {
                        String phase = atraceMatcher.group(TRACE_EVENT_PHASE_GROUP);
                        String pname = matcher.group(IGenericFtraceConstants.FTRACE_COMM_GROUP);
                        String content = atraceMatcher.group(TRACE_EVENT_CONTENT_GROUP);
                        Integer tid = field.getTid();
                        Integer pid = field.getPid();

                        Map<@NonNull String, @NonNull Object> argmap = new HashMap<>();
                        if (phase != null) {
                            argmap.put(ITraceEventConstants.PHASE, phase);
                        }
                        if (tid != null) {
                            argmap.put(ITraceEventConstants.TID, tid);
                        }
                        if (pid != null) {
                            argmap.put("pid", pid); //$NON-NLS-1$
                        }
                        if (pname != null) {
                            argmap.put("tname", pname); //$NON-NLS-1$
                        }
                        if (content != null) {
                            field.setName(content);
                        }
                        field.setContent(argmap);
                    }
                }
            }
        }

        return field;
    }

}
