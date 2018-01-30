/*******************************************************************************
 * Copyright (c) 2018 Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.ftrace.core.trace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
//import java.nio.ByteBuffer;
//import java.util.Collections;
//import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
//import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelUtils;
//import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.incubator.ftrace.layout.FtraceEventLayout;
import org.eclipse.tracecompass.incubator.internal.ftrace.core.Activator;
import org.eclipse.tracecompass.incubator.internal.ftrace.core.event.FtraceAspects;
import org.eclipse.tracecompass.incubator.internal.ftrace.core.event.FtraceEvent;
import org.eclipse.tracecompass.incubator.internal.ftrace.core.event.FtraceField;
//import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.KernelPidAspect;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.io.BufferedRandomAccessFile;
//import org.eclipse.tracecompass.tmf.core.project.model.ITmfPropertiesProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
//import org.eclipse.tracecompass.tmf.core.trace.ITmfTraceKnownSize;
import org.eclipse.tracecompass.tmf.core.trace.TmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;
//import org.eclipse.tracecompass.tmf.core.trace.indexer.ITmfPersistentlyIndexable;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.eclipse.tracecompass.tmf.core.trace.location.TmfLongLocation;

import com.google.common.collect.ImmutableSet;

/**
 * Ftrace trace.
 *
 * @author Guillaume Champagne, Alexis-Maurer Fortin, Hugo Genesse, Pierre-Yves
 *         Lajoie, Eva Terriault
 *
 */
public class FtraceTrace extends TmfTrace implements IKernelTrace/*, ITmfPersistentlyIndexable, ITmfPropertiesProvider, ITmfTraceKnownSize */{

   // private static final int CHECKPOINT_SIZE = 10000;
    private static final int ESTIMATED_EVENT_SIZE = 90;
    private static final TmfLongLocation NULL_LOCATION = new TmfLongLocation(-1L);
    private static final TmfContext INVALID_CONTEXT = new TmfContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
    private static final int MAX_LINES = 100;
    private static final int MAX_CONFIDENCE = 100;

    private File fFile;

    private RandomAccessFile fFileInput;

    @Override
    public IStatus validate(IProject project, String path) {
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
        try (BufferedRandomAccessFile rafile = new BufferedRandomAccessFile(path, "r")) { //$NON-NLS-1$
            int lineCount = 0;
            int matches = 0;
            String line = rafile.readLine();
            while ((line != null) && (lineCount++ < MAX_LINES)) {
                try {
                    FtraceField field = FtraceEvent.parseLine(line);
                    if (field != null) {
                        matches++;
                    }
                } catch (RuntimeException e) {
                    confidence = Integer.MIN_VALUE;
                }

                confidence = MAX_CONFIDENCE * matches / lineCount;
                line = rafile.readLine();
            }
            if (matches == 0) {
                return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Most assuredly NOT a traceevent trace"); //$NON-NLS-1$
            }
        } catch (IOException e) {
            Activator.getInstance().logError("Error validating file: " + path, e); //$NON-NLS-1$
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "IOException validating file: " + path, e); //$NON-NLS-1$
        }
        return new TraceValidationStatus(confidence, Activator.PLUGIN_ID);
    }

    @Override
    public void initTrace(IResource resource, String path, Class<? extends ITmfEvent> type) throws TmfTraceException {
        super.initTrace(resource, path, type);
        try {
            fFile = new File(path);
            fFileInput = new BufferedRandomAccessFile(fFile, "r"); //$NON-NLS-1$
        } catch (IOException e) {
            throw new TmfTraceException(e.getMessage(), e);
        }
    }

    @Override
    public synchronized void dispose() {
        if (fFileInput != null) {
            try {
                fFileInput.close();
            } catch (IOException e) {
                Activator.getInstance().logError("Error disposing trace. File: " + getPath(), e); //$NON-NLS-1$
            }
        }
        super.dispose();

    }

    @Override
    public double getLocationRatio(ITmfLocation location) {
        return ((Long) getCurrentLocation().getLocationInfo()).doubleValue() / fFile.length();
    }

    @Override
    public ITmfContext seekEvent(ITmfLocation location) {
        if (fFile == null) {
            return INVALID_CONTEXT;
        }
        final TmfContext context = new TmfContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
        if (NULL_LOCATION.equals(location) || fFile == null) {
            return context;
        }
        try {
            if (location == null) {
                long seekOffset = 0;
                fFileInput.seek(seekOffset);
                String line = fFileInput.readLine();
                while(line.charAt(0) == '#') {

                    // guchaj: Since each byte is treated as a char, the count is accurate +-
                    // the endline char. We make the assumption here that ftrace only ouputs
                    // line terminated with '\n' and never \r\n. This should be verified ( TODO ).
                    seekOffset += line.length() + 1;

                    line = fFileInput.readLine();
                }
                fFileInput.seek(seekOffset);
            } else if (location.getLocationInfo() instanceof Long) {
                fFileInput.seek((Long) location.getLocationInfo());
            }
            context.setLocation(new TmfLongLocation(fFileInput.getFilePointer()));
            context.setRank(0);
            return context;
        } catch (final FileNotFoundException e) {
            Activator.getInstance().logError("Error seeking event. File not found: " + getPath(), e); //$NON-NLS-1$
            return context;
        } catch (final IOException e) {
            Activator.getInstance().logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
            return context;
        }
    }

    @Override
    public ITmfContext seekEvent(double ratio) {
        File file = fFile;
        if (file == null) {
            return INVALID_CONTEXT;
        }
        long filePos = (long) (file.length() * ratio);
        long estimatedRank = filePos / ESTIMATED_EVENT_SIZE;
        return seekEvent(new TmfLongLocation(estimatedRank));
    }

    @Override
    public Iterable<ITmfEventAspect<?>> getEventAspects() {

        /*
         * This method needs to fill the aspects dynamically because aspects in
         * the parent class are not all present at the beginning of the trace
         */
        ImmutableSet.Builder<ITmfEventAspect<?>> builder = ImmutableSet.builder();
        builder.addAll(FtraceAspects.getAspects());
        //builder.addAll(KernelUtils.getKernelAspects());
        return builder.build();
        //return FtraceAspects.getAspects();
    }

    @Override
    public ITmfEvent parseEvent(ITmfContext context) {
        @Nullable
        ITmfLocation location = context.getLocation();
        if (location instanceof TmfLongLocation) {
            TmfLongLocation tmfLongLocation = (TmfLongLocation) location;
            Long locationInfo = tmfLongLocation.getLocationInfo();
            if (location.equals(NULL_LOCATION)) {
                locationInfo = 0L;
            }
            if (locationInfo != null) {
                try {
                    if (!locationInfo.equals(fFileInput.getFilePointer())) {
                        fFileInput.seek(locationInfo);
                    }
                    String nextLine = fFileInput.readLine();
                    if (nextLine != null) {
                        FtraceField field = FtraceEvent.parseLine(nextLine);
                        if (field != null) {
                            return new FtraceEvent(this, context.getRank(), field);
                        }
                    }
                } catch (IOException e) {
                    Activator.getInstance().logError("Error parsing event", e); //$NON-NLS-1$
                }
            }
        }
        return null;
    }

    @Override
    public ITmfLocation getCurrentLocation() {
        long temp = -1;
        try {
            temp = fFileInput.getFilePointer();
        } catch (IOException e) {
        }
        return new TmfLongLocation(temp);
    }

   /* @Override
    public @NonNull Map<@NonNull String, @NonNull String> getProperties() {
        return Collections.singletonMap("Type", "Trace-Event"); //$NON-NLS-1$ //$NON-NLS-2$
    }*/
/*
    @Override
    public ITmfLocation restoreLocation(ByteBuffer bufferIn) {
        return new TmfLongLocation(bufferIn);
    }
*/
    /*@Override
    public int getCheckpointSize() {
        return CHECKPOINT_SIZE;
    }
*/
    /**
     * Wrapper to get a character reader, allows to reconcile between java.nio and
     * java.io
     *
     * @author Matthew Khouzam
     *
     */
    public static interface IReaderWrapper {
        /**
         * Read the next character
         *
         * @return the next char
         * @throws IOException
         *             out of chars to read
         */
        char read() throws IOException;
    }

    /**
     * Manually parse a string of JSON. High performance to extract one object
     *
     * @param parser
     *            the reader
     * @return a String with a json object
     * @throws IOException
     *             end of file, file not found or such
     */
    public static @Nullable String readNextEventString(IReaderWrapper parser) throws IOException {
        StringBuffer sb = new StringBuffer();
        int scope = -1;
        int arrScope = 0;
        boolean inQuotes = false;
        char elem = parser.read();
        while (elem != (char) -1) {
            if (elem == '"') {
                inQuotes = !inQuotes;
            } else {
                if (inQuotes) {
                    // do nothing
                } else if (elem == '[') {
                    arrScope++;
                } else if (elem == ']') {
                    if (arrScope > 0) {
                        arrScope--;
                    } else {
                        return null;
                    }
                } else if (elem == '{') {
                    scope++;
                } else if (elem == '}') {
                    if (scope > 0) {
                        scope--;
                    } else {
                        sb.append(elem);
                        return sb.toString();
                    }
                }
            }
            if (scope >= 0) {
                sb.append(elem);
            }
            elem = parser.read();
        }
        return null;
    }

    /*@Override
    public int size() {
        RandomAccessFile fileInput = fFileInput;
        if (fileInput == null) {
            return 0;
        }
        long length = 0;
        try {
            length = fileInput.length();
        } catch (IOException e) {
            // swallow it for now
        }
        return length > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) length;
    }*/

   /* @Override
    public int progress() {
        RandomAccessFile fileInput = fFileInput;
        if (fileInput == null) {
            return 0;
        }
        long length = 0;
        try {
            length = fileInput.getFilePointer();
        } catch (IOException e) {
            // swallow it for now
        }
        return length > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) length;
    }
*/
    @Override
    public IKernelAnalysisEventLayout getKernelEventLayout() {
        return FtraceEventLayout.getInstance();
    }

}