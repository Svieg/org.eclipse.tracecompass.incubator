/*******************************************************************************
 * Copyright (c) 2018 Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.ftrace.core.event;

import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;

/**
 * Ftrace type lookup
 *
 * @author Guillaume Champagne, Alexis-Maurer Fortin, Hugo Genesse, Pierre-Yves
 *          Lajoie, Eva Terriault
 *
 */
public class FtraceLookup {

    private static final ITmfEventType[] TYPES = new TmfEventType[256];
    static {
        TYPES['B'] = new TmfEventType("Entry", null); //$NON-NLS-1$
        TYPES['E'] = new TmfEventType("Exit", null); //$NON-NLS-1$
        TYPES['X'] = new TmfEventType("Complete", null); //$NON-NLS-1$
        TYPES['I'] = TYPES['i'] = new TmfEventType("Instant", null); //$NON-NLS-1$
        TYPES['b'] = new TmfEventType("Nested Begin", null); //$NON-NLS-1$
        TYPES['n'] = new TmfEventType("Nested Info", null); //$NON-NLS-1$
        TYPES['e'] = new TmfEventType("Nested End", null); //$NON-NLS-1$
        TYPES['N'] = new TmfEventType("Object Created", null); //$NON-NLS-1$
        TYPES['D'] = new TmfEventType("Object Destroyed", null); //$NON-NLS-1$
        TYPES['C'] = new TmfEventType("Counter", null); //$NON-NLS-1$
        TYPES['R'] = new TmfEventType("Mark", null); //$NON-NLS-1$
        TYPES['s'] = new TmfEventType("Flow Start", null); //$NON-NLS-1$
        TYPES['t'] = new TmfEventType("Flow Step", null); //$NON-NLS-1$
        TYPES['f'] = new TmfEventType("Flow End", null); //$NON-NLS-1$
    }

    private static final TmfEventType fakeEvent = new TmfEventType("FAKE", null);

    /**
     * Get the event type
     *
     * @param c
     *            the character describing the event type
     * @return a TmfEventType
     */
    public static ITmfEventType get(String c) {
        return fakeEvent;
    }

}