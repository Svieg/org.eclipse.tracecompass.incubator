/*******************************************************************************
 * Copyright (c) 2018 Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.android.trace;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.incubator.internal.android.core.Activator;
import org.eclipse.tracecompass.incubator.internal.ftrace.core.event.FtraceField;
import org.eclipse.tracecompass.incubator.internal.ftrace.core.event.IFtraceConstants;
import org.eclipse.tracecompass.incubator.internal.ftrace.core.trace.FtraceTrace;
import org.eclipse.tracecompass.incubator.internal.traceevent.core.event.ITraceEventConstants;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;

/**
 * Android trace gathered via atrace.
 *
 * @author Guillaume Champagne
 * @author Alexis-Maurer Fortin
 * @author Hugo Genesse
 * @author Pierre-Yves Lajoie
 * @author Eva Terriault
 */
@SuppressWarnings("restriction")
public class AndroidTrace extends FtraceTrace {

    private static final String ATRACE_TRACEEVENT_EVENT = "tracing_mark_write"; //$NON-NLS-1$

    private static final Pattern TRACE_EVENT_PATTERN = Pattern.compile("(?<phase>\\w)(\\|(?<tid>\\d+)\\|(?<content>[^\\|]+))?"); //$NON-NLS-1$
    private static final String TRACE_EVENT_PHASE_GROUP = "phase"; //$NON-NLS-1$
    // private static final String TRACE_EVENT_TID_GROUP = "tid"; //$NON-NLS-1$
    private static final String TRACE_EVENT_CONTENT_GROUP = "content"; //$NON-NLS-1$

    @Override
    public IStatus validate(IProject project, String path) {
        /* guchaj: Ideally, we would be able to differentiate
         * atrace traces from "real" ftrace traces. ATM, the only difference
         * we could base this choice on is the presence of the 'TGID' column
         * in atrace and not in ftrace, but that is not really future proof.
         * We should investigate what is the best decision here (TODO)
         */

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

        return new TraceValidationStatus(confidence, Activator.PLUGIN_ID);
    }

    @Override
    protected @Nullable FtraceField parseLine(String line) {
        FtraceField field = super.parseLine(line);

        if (field != null) {
            Matcher matcher = IFtraceConstants.FTRACE_PATTERN.matcher(line);
            if (matcher.matches()) {

                /*
                 * User spaces event that permit us to create the call stack are inserted
                 * in the raw trace. Those events are named 'tracing_mark_write'. The format
                 * in the "function" column is not like any other ftrace events, so we must
                 * handle them separately.
                 */
                if (field.getName().equals(ATRACE_TRACEEVENT_EVENT)) {
                    String data = matcher.group(IFtraceConstants.FTRACE_DATA_GROUP);
                    Matcher atraceMatcher = TRACE_EVENT_PATTERN.matcher(data);
                    if (atraceMatcher.matches()) {
                        String phase = atraceMatcher.group(TRACE_EVENT_PHASE_GROUP);
                        String pname = matcher.group("comm");
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
