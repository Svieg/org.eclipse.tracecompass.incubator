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

import java.util.Map;

/**
 * Aspects for Trace Compass Logs
 *
 * @author Guillaume Champagne, Alexis-Maurer Fortin, Hugo Genesse, Pierre-Yves
 *          Lajoie, Eva Terriault
 */
public class FtraceAspects {

    /**
     * Apects of a trace
     */
    private static Iterable<@NonNull ITmfEventAspect<?>> aspects;

    /**
     * Get the event aspects
     *
     * @return get the event aspects
     */
    public static @NonNull Iterable<@NonNull ITmfEventAspect<?>> getAspects() {
        Iterable<@NonNull ITmfEventAspect<?>> aspectSet = aspects;
        if (aspectSet == null) {
            aspectSet = ImmutableList.of(
                    new TraceCompassScopeLogLabelAspect(),
                    TmfBaseAspects.getTimestampAspect(),
                    new TraceCompassScopeLogArgsAspect(),
                    new FtracePidAspect(),
                    new FtraceCpuAspect());
            aspects = aspectSet;
        }
        return aspectSet;
    }



    private static class TraceCompassScopeLogLabelAspect implements IFtraceAspect<String> {

        @Override
        public @NonNull String getName() {
            return String.valueOf(Messages.TraceCompassScopeLogAspects_Name);
        }

        @Override
        public @NonNull String getHelpText() {
            return String.valueOf(Messages.TraceCompassScopeLogAspects_NameD);
        }

        @Override
        public @Nullable String resolveTCL(@NonNull FtraceEvent event) {
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

    private static class TraceCompassScopeLogArgsAspect implements IFtraceAspect<Map<String, Object>> {

        @Override
        public @NonNull String getName() {
            return String.valueOf(Messages.TraceCompassScopeLogAspects_Args);
        }

        @Override
        public @NonNull String getHelpText() {
            return String.valueOf(Messages.TraceCompassScopeLogAspects_ArgsD);
        }

        @Override
        public @Nullable Map<String, Object> resolveTCL(@NonNull FtraceEvent event) {
            return event.getField().getArgs();
        }
    }
}
