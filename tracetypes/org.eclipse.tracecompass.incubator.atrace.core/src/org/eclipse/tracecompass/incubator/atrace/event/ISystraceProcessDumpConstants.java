/*******************************************************************************
 * Copyright (c) 2018 Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.atrace.event;

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Constants for the ftrace format
 *
 * @author Guillaume Champagne
 * @author Alexis-Maurer Fortin
 * @author Hugo Genesse
 * @author Pierre-Yves Lajoie
 * @author Eva Terriault
 */
@NonNullByDefault
public interface ISystraceProcessDumpConstants {

    //TODO: Should be the same as the layout. What's the good way to do that?
    String DEFAULT_SYSTRACE_PROCESS_DUMP_EVENT_NAME = "state_dump_ftrace_test"; //$NON-NLS-1$

    Pattern PROCESS_DUMP_PATTERN = Pattern
            .compile("^\\s*(?<user>.*?)\\s+(?<pid>\\d+)\\s+(?<ppid>\\d+)\\s+(?<vsz>\\d+)\\s+(?<rss>\\d+)\\s+(?<wchan>.*?)\\s+(?<pc>.*?)\\s+(?<s>.*?)\\s+(?<name>.*?)?"); //$NON-NLS-1$
            //.compile("^\\s*(?<user>.*?)\\s+(?<pid>\\d+)\\s+(?<ppid>\\d+)\\s+(?<vsz>\\d+)\\s+(?<rss>\\d+)\\s+(?<wchan>.*?)\\s+(?<pc>.*?)\\s+(?<s>.*?)\\s+(?<name>.*?)\\s+(?:<comm>.*?)?"); //$NON-NLS-1$

    //TODO: Change name of class or move that
    Pattern TRACE_EVENT_PATTERN = Pattern.compile("(?<phase>\\w)(\\|(?<tid>\\d+)\\|(?<content>[^\\|]+))?"); //$NON-NLS-1$

}
