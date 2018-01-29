/*******************************************************************************
 * Copyright (c) 2018 Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.ftrace.core.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfCallsite;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfSourceLookup;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Trace compass log event
 *
 * @author Guillaume Champagne, Alexis-Maurer Fortin, Hugo Genesse, Pierre-Yves
 *          Lajoie, Eva Terriault
 */
public class FtraceEvent extends TmfEvent implements ITmfSourceLookup {

    private static final Pattern FTRACE_PATTERN = Pattern.compile("^\\s*(?<comm>.*)-(?<pid>\\d+)(?:\\s+\\(.*\\))?\\s+\\[(?<cpu>\\d+)\\](?:\\s+....)?\\s+(?<timestamp>[0-9]+(?<us>\\.[0-9]+)?): (\\w+:\\s+)+(?<data>.+)"); //$NON-NLS-1$
    private static final Pattern KEYVAL_PATTERN = Pattern.compile("(?<key>[^\\s=]+)=(?<val>[^\\s=]+)"); //$NON-NLS-1$

    private static final double SECONDS_TO_NANO = 1000000000.0;
    private final @Nullable ITmfCallsite fCallsite;
    private final Level fLogLevel;
    private @NonNull final String fName;
    private final FtraceField fField;

    private static final String[] PREV_STATE_LUT = new String[256];
    static {
        PREV_STATE_LUT['S'] = "1"; //$NON-NLS-1$
        PREV_STATE_LUT['D'] = "2"; //$NON-NLS-1$
        PREV_STATE_LUT['T'] = "4"; //$NON-NLS-1$
        PREV_STATE_LUT['t'] = "8"; //$NON-NLS-1$
        PREV_STATE_LUT['X'] = "16"; //$NON-NLS-1$
        PREV_STATE_LUT['x'] = "16"; //$NON-NLS-1$
        PREV_STATE_LUT['Z'] = "32"; //$NON-NLS-1$
        PREV_STATE_LUT['P'] = "64"; //$NON-NLS-1$
        PREV_STATE_LUT['Z'] = "32"; //$NON-NLS-1$
        PREV_STATE_LUT['I'] = "128"; //$NON-NLS-1$
        PREV_STATE_LUT['R'] = "0"; //$NON-NLS-1$
    }

    /**
     * Constructor
     */
    @Deprecated
    public FtraceEvent() {
        super();
        fCallsite = null;
        fLogLevel = Level.OFF;
        fName = StringUtils.EMPTY;
        fField = new FtraceField(StringUtils.EMPTY, 0, 0L, null, null, Collections.EMPTY_MAP);
    }

    /**
     * Constructor for simple traceEventEvent
     *
     * @param trace
     *            the trace
     * @param rank
     *            the rank
     * @param field
     *            the event field, contains all the needed data
     */
    public FtraceEvent(ITmfTrace trace, long rank, FtraceField field) {
        super(trace, rank, TmfTimestamp.fromNanos(field.getTs()), FtraceLookup.get(field.getName()), field.getContent());
        fField = field;
        fName = field.getName();
        fLogLevel = Level.INFO;
        fCallsite = null;
    }

    @Override
    public ITmfEventField getContent() {
        return fField.getContent();
    }

    @Override
    public @NonNull String getName() {
        return fName;
    }

    @Override
    public @Nullable ITmfCallsite getCallsite() {
        return fCallsite;
    }


    /** Parse a line from an ftrace ouput file
     * @param line
     *          The string to parse
     * @return An event field
     */
    @SuppressWarnings("null")
    public static FtraceField parseLine(String line) {

        // TODO: guchaj: Understand how @NonNull works (I added SupressWarnings, but I doubt thats the way to go :p)

        Matcher matcher = FTRACE_PATTERN.matcher(line); // guchaj: could this be a static object?
        if (matcher.matches()) {
            Integer pid = Integer.parseInt(matcher.group("pid")); //$NON-NLS-1$
            Integer tid = pid;
            Integer cpu = Integer.parseInt(matcher.group("cpu")); //$NON-NLS-1$
            Double timestampInSec = Double.parseDouble(matcher.group("timestamp")); //$NON-NLS-1$
            Long timestampInNano = Double.valueOf(timestampInSec * SECONDS_TO_NANO).longValue();

            String name = matcher.group(6);
            name = name.substring(0, name.length() - 2);
            String attributes = matcher.group("data"); //$NON-NLS-1$

            Map<@NonNull String, @NonNull Object> fields = new HashMap<>();

            fields.put(IFtraceConstants.TIMESTAMP, timestampInNano);
            fields.put(IFtraceConstants.NAME, name);
            fields.put(IFtraceConstants.TID, Integer.valueOf(tid).longValue());
            fields.put(IFtraceConstants.PID, Integer.valueOf(pid).longValue());

            Matcher keyvalMatcher = KEYVAL_PATTERN.matcher(attributes);
            while (keyvalMatcher.find()) {
                String key = keyvalMatcher.group("key"); //$NON-NLS-1$
                String value = keyvalMatcher.group("val"); //$NON-NLS-1$

                // guchaj: This is temporary until we find a better way to convert event
                if (key.equals("prev_state")) { //$NON-NLS-1$
                    value = PREV_STATE_LUT[value.charAt(0)];
                }
                // guchaj: end temporary section

                if (StringUtils.isNumeric(value)) {
                    fields.put(key, Long.parseLong(value));

                    // guchaj: This is weird. We should ask Genevieve what's the difference
                    // between pids and tids in the kernel context
                    if (key.contains("pid")) { //$NON-NLS-1$
                        String newkey = key.replaceAll("pid", "tid"); //$NON-NLS-1$ //$NON-NLS-2$
                        fields.put(newkey, Long.parseLong(value));
                    }
                }
                else {
                    fields.put(key, value);
                }
            }
            return new FtraceField(name, cpu, timestampInNano, pid, tid, fields);
        }
        return null;
    }

    /**
     * Get the loglevel of the event
     *
     * @return the log level
     */
    public Level getLevel() {
        return fLogLevel;
    }

    /**
     * Get the fields of the event
     *
     * @return the fields of the event
     */
    public FtraceField getField() {
        return fField;
    }

}