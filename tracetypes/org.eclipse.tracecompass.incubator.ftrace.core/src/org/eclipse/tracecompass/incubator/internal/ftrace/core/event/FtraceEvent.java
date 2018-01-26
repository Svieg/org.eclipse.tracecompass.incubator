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
import java.util.Iterator;
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
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Trace compass log event
 *
 * @author Guillaume Champagne, Alexis-Maurer Fortin, Hugo Genesse, Pierre-Yves
 *          Lajoie, Eva Terriault
 */
public class FtraceEvent extends TmfEvent implements ITmfSourceLookup {

    private static final Pattern FTRACE_PATTERN = Pattern.compile("^\\s*(?<comm>.*)-(?<pid>\\d+)(?:\\s+\\(.*\\))?\\s+\\[(?<cpu>\\d+)\\](?:\\s+....)?\\s+(?<timestamp>[0-9]+(?<us>\\.[0-9]+)?): (\\w+:\\s+)+(?<data>.+)"); //$NON-NLS-1$

    private static final double MICRO_TO_NANO = 1000.0;
    private final @Nullable ITmfCallsite fCallsite;
    private final Level fLogLevel;
    private @NonNull final String fName;
    private final FtraceField fField;

    /**
     * Constructor
     */
    @Deprecated
    public FtraceEvent() {
        super();
        fCallsite = null;
        fLogLevel = Level.OFF;
        fName = StringUtils.EMPTY;
        fField = new FtraceField(StringUtils.EMPTY, 0, 0.0, null, null, Collections.EMPTY_MAP); //$NON-NLS-1$
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
            Double timestamp = Double.parseDouble(matcher.group("timestamp")); //$NON-NLS-1$

            String name = matcher.group(6); //$NON-NLS-1$
            name = name.substring(0, name.length() - 2);
            String attributes = matcher.group("data"); //$NON-NLS-1$

            Map<@NonNull String, @NonNull Object> fields = new HashMap<>();

            fields.put(IFtraceConstants.TIMESTAMP, timestamp);
            fields.put(IFtraceConstants.NAME, name);
            fields.put(IFtraceConstants.TID, Integer.valueOf(tid).longValue());
            fields.put(IFtraceConstants.PID, Integer.valueOf(pid).longValue());

            // guchaj: Probably inefficient and not fail proof :-(
            for (String keyval: attributes.split(" ")) { //$NON-NLS-1$
                String[] val = keyval.split("="); //$NON-NLS-1$
                if (val.length == 2) {
                    fields.put(val[0], val[1]);
                }
            }

            return new FtraceField(name, cpu, timestamp, pid, tid, fields);
        }
        return null;
    }


    /**
     * Parse a JSON string
     *
     * @param fieldsString
     *            the string
     * @return an event field
     */
    public static FtraceField parseJson(String fieldsString) {
        // looks like this
        // {"ts":94824347413117,"phase":"B","tid":39,"name":"TimeGraphView:BuildThread","args"={"trace":"django-httpd"}}
        JSONObject root;
        Map<@NonNull String, @NonNull Object> argsMap = new HashMap<>();
        try {
            root = new JSONObject(fieldsString);
            long ts = 0;

            Double tso = optDouble(root, IFtraceConstants.TIMESTAMP);
            if (Double.isFinite(tso)) {
                ts = (long) (tso * MICRO_TO_NANO);
            }
            String phase = optString(root, IFtraceConstants.PHASE, "I"); //$NON-NLS-1$
            if (phase == null) {
                // FIXME: Easy way to avoid null warning
                phase = "I"; //$NON-NLS-1$
            }
            String name = String.valueOf(optString(root,IFtraceConstants.NAME, "E".equals(phase) ? "exit" : "unknown")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            Integer tid = optInt(root, IFtraceConstants.TID);
            if (tid == Integer.MIN_VALUE) {
                tid = null;
            }
            Object pid = root.opt(IFtraceConstants.PID);
            Double duration = optDouble(root, IFtraceConstants.DURATION);
            if (Double.isFinite(duration)) {
                duration = (duration * MICRO_TO_NANO);
            }
            String category = optString(root, IFtraceConstants.CATEGORY);
            String id = optString(root, IFtraceConstants.ID);
            JSONObject args = optJSONObject(root, IFtraceConstants.ARGS);
            if (args != null) {
                Iterator<?> keys = args.keys();
                while (keys.hasNext()) {
                    String key = String.valueOf(keys.next());
                    String value = args.optString(key);
                    argsMap.put("arg/" + key, String.valueOf(value)); //$NON-NLS-1$
                }
            }
            argsMap.put(IFtraceConstants.TIMESTAMP, ts);
            argsMap.put(IFtraceConstants.PHASE, phase);
            argsMap.put(IFtraceConstants.NAME, name);
            if (tid != null) {
                argsMap.put(IFtraceConstants.TID, tid);
            }
            if (pid != null) {
                argsMap.put(IFtraceConstants.PID, pid);
            }
            if (Double.isFinite(duration)) {
                argsMap.put(IFtraceConstants.DURATION, duration);
            }
            if (category != null) {
                argsMap.put(IFtraceConstants.CATEGORY, category);
            }
            if (id != null) {
                argsMap.put(IFtraceConstants.ID, id);
            }
            // return new FtraceField(name, ts, phase, pid, tid, category, id, duration, argsMap);
        } catch (JSONException e1) {
            // invalid, return null and it will fail
        }
        return null;
    }

    private static double optDouble(JSONObject root, String key) {
        return root.has(key) ? root.optDouble(key) : Double.NaN;
    }

    private static int optInt(JSONObject root, String key) {
        return root.has(key) ? root.optInt(key) : Integer.MIN_VALUE;
    }

    private static JSONObject optJSONObject(JSONObject root, String key){
        return root.has(key) ? root.optJSONObject(key) : null;
    }

    private static String optString(JSONObject root, String key, String defaultValue) {
        return root.has(key) ? root.optString(key) : defaultValue;
    }

    private static String optString(JSONObject root, String key) {
        return optString(root, key, null);
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