/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.android.core.event;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;

/**
 * A trace compass log aspect
 *
 * @authors Guillaume Champagne, Alexis-Maurer Fortin, Hugo Genesse, Pierre-Yves
 *          Lajoie, Eva Terriault
 *
 * @param <T>
 */
interface IAndroidAspect<T> extends ITmfEventAspect<T> {

    @Override
    default @Nullable T resolve(@NonNull ITmfEvent event) {
        if (event instanceof AndroidEvent) {
            return resolveTCL((AndroidEvent) event);
        }
        return null;
    }

    T resolveTCL(@NonNull AndroidEvent event);

}
