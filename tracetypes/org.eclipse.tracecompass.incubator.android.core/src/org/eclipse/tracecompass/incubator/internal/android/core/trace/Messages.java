/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.android.core.trace;

import org.eclipse.osgi.util.NLS;

/**
 * Messages
 *
 * @authors Guillaume Champagne, Alexis-Maurer Fortin, Hugo Genesse, Pierre-Yves
 *          Lajoie, Eva Terriault
 *
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.incubator.internal.android.core.trace.messages"; //$NON-NLS-1$

    /**
     * Description
     */
    public static String SortingJob_description;

    /**
     * Merging phase
     */
    public static String SortingJob_merging;

    /**
     * Sorting phase
     */
    public static String SortingJob_sorting;

    /**
     * Splitting phase
     */
    public static String SortingJob_splitting;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
