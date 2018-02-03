/*******************************************************************************
 * Copyright (c) 2018 Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.ftrace.core.event;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.LinuxPidAspect;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfBaseAspects;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;

/**
 * Aspects for Trace Compass Logs
 *
 * @author Guillaume Champagne
 * @author Alexis-Maurer Fortin
 * @author Hugo Genesse
 * @author Pierre-Yves Lajoie
 * @author Eva Terriault
 */
public class FtraceAspects {

    /**
     * Apects of a trace
     */
    private static Iterable<@NonNull ITmfEventAspect<?>> sfAspects;

    /**
     * Get the event aspects
     *
     * @return get the event aspects
     */
    public static @NonNull Iterable<@NonNull ITmfEventAspect<?>> getAspects() {
        Iterable<@NonNull ITmfEventAspect<?>> aspectSet = sfAspects;
        if (aspectSet == null) {
            aspectSet = ImmutableList.of(
                    new FtraceEventNameAspect(),
                    TmfBaseAspects.getTimestampAspect(),
                    new FtracePidAspect(),
                    new FtraceCpuAspect());
            sfAspects = aspectSet;
        }
        return aspectSet;
    }

    private static class FtraceEventNameAspect implements ITmfEventAspect<String> {

        @Override
        public @NonNull String getName() {
            return String.valueOf(Messages.FtraceAspects_Name);
        }

        @Override
        public @NonNull String getHelpText() {
            return String.valueOf(Messages.FtraceAspects_NameD);
        }

        @Override
        public @Nullable String resolve(@NonNull ITmfEvent event) {
            return event.getName();
        }
    }

    private static class FtracePidAspect extends LinuxPidAspect {

        @Override
        public @Nullable Integer resolve(ITmfEvent event) {
            if (event instanceof FtraceEvent) {
                FtraceEvent ftraceEvent = (FtraceEvent) event;
                return ftraceEvent.getField().getPid();
            }
            return null;
        }
    }

    private static class FtraceCpuAspect extends TmfCpuAspect {

        @Override
        public @Nullable Integer resolve(ITmfEvent event) {
            if (!(event instanceof FtraceEvent)) {
                return null;
            }
            return ((FtraceEvent) event).getField().getCpu();
        }
    }
}
