/*******************************************************************************
 * Copyright (c) 2018 Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.atrace.event;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;

/**
 * Ftrace field class
 *
 * @author Guillaume Champagne
 * @author Alexis-Maurer Fortin
 * @author Hugo Genesse
 * @author Pierre-Yves Lajoie
 * @author Eva Terriault
 */
@NonNullByDefault
public class SystraceProcessDumpEventField extends TmfEventField {
    /*private final Long fTs;
    private String fName;
    private final Integer fCpu;
    private @Nullable Integer fTid;
    private @Nullable Integer fPid;
    private ITmfEventField fContent;*/

    /**
     * Constructor
     *
     * @param name   event name
     * @param cpu    the cpu number
     * @param ts     the timestamp in ns
     * @param pid    the process id
     * @param tid    the threadId
     * @param fields event fields (arguments)
     */
    public SystraceProcessDumpEventField(String name, Integer cpu, Long ts, @Nullable Integer pid, @Nullable Integer tid, Map<String, Object> fields) {
        super(name, null, fields.entrySet().stream()
                .map(entry -> new TmfEventField(entry.getKey(), entry.getValue(), null))
                .toArray(ITmfEventField[]::new)); //TODO: What's value?

       /* fName = name;
        fCpu = cpu;
        fPid = pid;
        fTid = tid;
        ITmfEventField[] array = fields.entrySet().stream()
                .map(entry -> new TmfEventField(entry.getKey(), entry.getValue(), null))
                .toArray(ITmfEventField[]::new) ;
        fContent = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, fields, array);

        fTs = ts;*/
    }
    @Override
    public Object getValue() {
        return (Long) super.getValue();
    }

}
