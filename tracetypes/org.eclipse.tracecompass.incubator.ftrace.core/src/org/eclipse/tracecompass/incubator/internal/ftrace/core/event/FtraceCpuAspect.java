/*******************************************************************************
 * Copyright (c) 2018 Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/


package org.eclipse.tracecompass.incubator.internal.ftrace.core.event;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;

/**
 * "CPU" event aspect for CTF traces.
 *
 * @author Guillaume Champagne, Alexis-Maurer Fortin, Hugo Genesse, Pierre-Yves
 *          Lajoie, Eva Terriault
 */

public class FtraceCpuAspect extends TmfCpuAspect {

    @Override
    public @Nullable Integer resolve(ITmfEvent event) {
        if (!(event instanceof FtraceEvent)) {
            return null;
        }
        int cpu = ((FtraceEvent) event).getField().getCpu();
        return cpu;
    }

}
