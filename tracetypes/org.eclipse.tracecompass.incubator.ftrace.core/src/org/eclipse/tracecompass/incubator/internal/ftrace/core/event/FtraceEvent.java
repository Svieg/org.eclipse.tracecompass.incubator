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
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.LinuxValues;
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

    private static final Pattern FTRACE_PATTERN = Pattern.compile("^\\s*(?<comm>.*)-(?<pid>\\d+)(?:\\s+\\(.*\\))?\\s+\\[(?<cpu>\\d+)\\](?:\\s+....)?\\s+(?<timestamp>[0-9]+(?<us>\\.[0-9]+)?): (?<name>\\w+:\\s+)+(?<data>.+)"); //$NON-NLS-1$
    private static final String FTRACE_PATTERN_PID_GROUP = "pid"; //$NON-NLS-1$
    private static final String FTRACE_PATTERN_CPU_GROUP = "cpu"; //$NON-NLS-1$
    private static final String FTRACE_PATTERN_TIMESTAMP_GROUP = "timestamp"; //$NON-NLS-1$
    private static final String FTRACE_PATTERN_NAME_GROUP = "name"; //$NON-NLS-1$
    private static final String FTRACE_PATTERN_DATA_GROUP = "data"; //$NON-NLS-1$

    private static final Pattern KEYVAL_PATTERN = Pattern.compile("(?<key>[^\\s=]+)=(?<val>[^\\s=]+)"); //$NON-NLS-1$
    private static final String KEYVAL_PATTERN_KEY_GROUP = "key"; //$NON-NLS-1$
    private static final String KEYVAL_PATTERN_VAL_GROUP = "val"; //$NON-NLS-1$

    private static final double SECONDS_TO_NANO = 1000000000.0;
    private final @Nullable ITmfCallsite fCallsite;
    private final Level fLogLevel;
    private @NonNull final String fName;
    private final FtraceField fField;

    private static final int[] PREV_STATE_LUT = new int[256];
    static {
        PREV_STATE_LUT['R'] = LinuxValues.TASK_STATE_RUNNING;
        PREV_STATE_LUT['S'] = LinuxValues.TASK_INTERRUPTIBLE;
        PREV_STATE_LUT['D'] = LinuxValues.TASK_UNINTERRUPTIBLE;
        PREV_STATE_LUT['T'] = LinuxValues.TASK_STOPPED__;
        PREV_STATE_LUT['t'] = LinuxValues.TASK_TRACED__;
        PREV_STATE_LUT['X'] = LinuxValues.EXIT_ZOMBIE;
        PREV_STATE_LUT['x'] = LinuxValues.EXIT_ZOMBIE;
        PREV_STATE_LUT['Z'] = LinuxValues.EXIT_DEAD;
        PREV_STATE_LUT['P'] = LinuxValues.TASK_DEAD;
        PREV_STATE_LUT['I'] = LinuxValues.TASK_WAKEKILL;
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
    public static FtraceField parseLine(String line) {
        Matcher matcher = FTRACE_PATTERN.matcher(line);
        if (matcher.matches()) {
            Integer pid = Integer.parseInt(matcher.group(FTRACE_PATTERN_PID_GROUP));
            Integer tid = pid;
            Integer cpu = Integer.parseInt(matcher.group(FTRACE_PATTERN_CPU_GROUP));
            Double timestampInSec = Double.parseDouble(matcher.group(FTRACE_PATTERN_TIMESTAMP_GROUP));
            Long timestampInNano = Double.valueOf(timestampInSec * SECONDS_TO_NANO).longValue();

            String name = matcher.group(FTRACE_PATTERN_NAME_GROUP);
            name = name.substring(0, name.length() - 2);

            String attributes = matcher.group(FTRACE_PATTERN_DATA_GROUP);

            Map<@NonNull String, @NonNull Object> fields = new HashMap<>();
            fields.put(IFtraceConstants.TIMESTAMP, timestampInNano);
            fields.put(IFtraceConstants.NAME, name);
            fields.put(IFtraceConstants.TID, Integer.valueOf(tid).longValue());
            fields.put(IFtraceConstants.PID, Integer.valueOf(pid).longValue());

            Matcher keyvalMatcher = KEYVAL_PATTERN.matcher(attributes);
            while (keyvalMatcher.find()) {
                String key = keyvalMatcher.group(KEYVAL_PATTERN_KEY_GROUP);
                String value = keyvalMatcher.group(KEYVAL_PATTERN_VAL_GROUP);
                if (value != null) {
                    // guchaj: This is a temporary solution. Refactor suggestions are welcome.
                    if (key.equals("prev_stave")) { //$NON-NLS-1$
                        fields.put(key, PREV_STATE_LUT[value.charAt(0)]);
                    }
                    else if (StringUtils.isNumeric(value)) {
                        fields.put(key, Long.parseLong(value));
                    }
                    else {
                        fields.put(key, value);
                    }
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