/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.rcp.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Activator
 */
public class Activator extends AbstractUIPlugin {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "org.eclipse.tracecompass.rcp.ui"; //$NON-NLS-1$

    /**
     * The default workspace name
     */
    public static final String WORKSPACE_NAME = ".tracecompass"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The shared instance
    private static @Nullable Activator fPlugin;

    // ------------------------------------------------------------------------
    // Constructor(s)
    // ------------------------------------------------------------------------
    /**
     * The default constructor
     */
    public Activator() {
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        Activator plugin = fPlugin;
        if (plugin == null) {
            throw new NullPointerException("The activator should not be null"); //$NON-NLS-1$
        }
        return plugin;
    }

    /**
     * Gets the tracing workspace root directory. By default it uses the user's
     * home directory. This value can be overwritten by using the global
     * TRACING_RCP_ROOT environment variable.
     *
     * @return the tracing workspace root directory
     */
    public static String getWorkspaceRoot() {
        /* Look for the environment variable in the global environment variables */
        String workspaceRoot = System.getenv().get("TRACING_RCP_ROOT"); //$NON-NLS-1$
        if (workspaceRoot == null) {
            /* Use the user's home directory */
            workspaceRoot = System.getProperty("user.home"); //$NON-NLS-1$
        }
        return workspaceRoot;
    }

    // ------------------------------------------------------------------------
    // Operation
    // ------------------------------------------------------------------------
    @Override
    public void start(@Nullable BundleContext context) throws Exception {
        super.start(context);
        fPlugin = this;
    }

    @Override
    public void stop(@Nullable BundleContext context) throws Exception {
        fPlugin = null;
        super.stop(context);
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path
     *
     * @param path
     *            the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    /**
     * Log an error
     *
     * @param message
     *            the error message to log
     */
    public void logError(String message) {
        getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message));
    }

    /**
     * Log an error
     *
     * @param message
     *            the error message to log
     * @param e
     *            the exception to log
     */
    public void logError(String message, Exception e) {
        getDefault().getLog().log(
                new Status(IStatus.ERROR, PLUGIN_ID, message, e));
    }

    /**
     * Log a warning
     *
     * @param message
     *            the warning message to log
     */
    public void logWarning(String message) {
        getDefault().getLog().log(
                new Status(IStatus.WARNING, PLUGIN_ID, message));
    }


}

