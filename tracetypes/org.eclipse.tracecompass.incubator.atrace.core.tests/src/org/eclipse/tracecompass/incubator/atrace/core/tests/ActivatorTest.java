/*******************************************************************************
 * Copyright (c) 2018 Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.atrace.core.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.tracecompass.common.core.TraceCompassActivator;
import org.eclipse.tracecompass.incubator.internal.atrace.core.Activator;
import org.junit.Test;
import org.osgi.framework.BundleContext;

/**
 * Test the activator name. This class is there mostly to create a non empty
 * test plugin
 *
 * @author Geneviève Bastien
 */
public class ActivatorTest extends Plugin {
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "org.eclipse.tracecompass.incubator.atrace.core.tests"; //$NON-NLS-1$

    /**
     * The shared instance
     */
    private static Plugin PLUGIN;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The constructor
     */
    public ActivatorTest() {
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Plugin getDefault() {
        return PLUGIN;
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        PLUGIN = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        PLUGIN = null;
        super.stop(context);
    }

    /**
     * Test Activator
     */
    @Test
    public void testActivator() {
        TraceCompassActivator instance = Activator.getInstance();
        assertEquals("org.eclipse.tracecompass.incubator.atrace.core", instance.getPluginId());
    }

    /**
     * Return a path to a file relative to this plugin's base directory
     *
     * @param relativePath
     *            The path relative to the plugin's root directory
     * @return The path corresponding to the relative path in parameter
     */
    public static IPath getAbsoluteFilePath(String relativePath) {
        Plugin plugin = getDefault();
        if (plugin == null) {
            /*
             * Shouldn't happen but at least throw something to get the test to fail early
             */
            throw new IllegalStateException();
        }
        URL location = FileLocator.find(plugin.getBundle(), new Path(relativePath), null);
        try {
            return new Path(FileLocator.toFileURL(location).getPath());
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

}
