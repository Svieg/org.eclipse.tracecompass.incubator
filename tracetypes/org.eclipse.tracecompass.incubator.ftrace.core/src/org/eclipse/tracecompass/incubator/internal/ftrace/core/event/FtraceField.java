/*******************************************************************************
 * Copyright (c) 2018 Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.ftrace.core.event;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;

/**
 * Trace Compass Log fields, used as a quick wrapper for Trace compass log data
 *
 * @author Guillaume Champagne, Alexis-Maurer Fortin, Hugo Genesse, Pierre-Yves
 *          Lajoie, Eva Terriault
 */
@NonNullByDefault
public class FtraceField {

    private final Long fTs;
    private final String fName;
    private final Integer fCpu;
    private ITmfEventField fContent;
    private final @Nullable Map<String, Object> fArgs;
    private final @Nullable Integer fTid;
    private final @Nullable Integer fPid;

    /**
     * Constructor
     *
     * @param name
     *            event name
     * @param cpu
     *            the cpu number
     * @param ts
     *            the timestamp in ns
     * @param pid
     *            the process id
     * @param tid
     *            the threadId
     * @param fields
     *            event fields (arguments)
     */
    public FtraceField(String name, Integer cpu, Long ts, @Nullable Integer pid, @Nullable Integer tid, Map<String, Object> fields) {
        fName = name;
        fCpu = cpu;
        fPid = pid;
        fTid = tid;
        ITmfEventField[] array = fields.entrySet().stream()
                .map(entry -> new TmfEventField(entry.getKey(), entry.getValue(), null))
                .toArray(ITmfEventField[]::new);
        fContent = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, fields, array);
        fTs = ts;
        @SuppressWarnings("null")
        Map<@NonNull String, @NonNull Object> args = fields.entrySet().stream()
                .filter(entry -> {
                    return entry.getKey().startsWith("arg/"); //$NON-NLS-1$
                })
                .collect(Collectors.toMap(entry -> entry.getKey().substring(4), Entry::getValue));
        fArgs = args.isEmpty() ? null : args;
    }

    /**
     * Get the event content
     *
     * @return the event content
     */
    public ITmfEventField getContent() {
        return fContent;
    }

    /**
     * Get the name of the event
     *
     * @return the event name
     */
    public String getName() {
        return fName;
    }

    /**
     * Get the TID of the event
     *
     * @return the event TID
     */
    public @Nullable Integer getTid() {
        return fTid;
    }

    /**
     * Get the timestamp
     *
     * @return the timestamp in ns
     */
    public Long getTs() {
        return fTs;
    }

    /**
     * Get pid
     *
     * @return the process ID
     */
    @Nullable
    public Integer getPid() {
        return fPid;
    }

    /**
     * Get the arguments passed
     *
     * @return a map of the arguments and their field names
     */
    @Nullable
    public Map<String, Object> getArgs() {
        return fArgs;
    }

    /**
     * Get the cpu number
     * @return the cpu number
     */
    public Integer getCpu() {
        return fCpu;
    }
}
