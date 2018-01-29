package org.eclipse.tracecompass.incubator.internal.ftrace.core.trace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ftrace output parsing
 *
 * @author Alexis-Maurer Fortin
 *
 */
public class FtraceParser {
    private static final Pattern FTRACE_PATTERN = Pattern.compile("^\\s*(?<comm>.*)-(?<pid>\\d+)(?:\\s+\\(.*\\))?\\s+\\[(?<cpu>\\d+)\\](?:\\s+....)?\\s+(?<timestamp>[0-9]+(?<us>\\.[0-9]+)?): (\\w+:\\s+)+(?<data>.+)"); //$NON-NLS-1$
    private String path;

    /**
     * Ftrace output parser
     *
     * @param path
     *            the path to the Ftrace output file
     *
     */
    public FtraceParser(String path) {
        this.path = path;
    }

    /**
     *
     */
    public void parse() {

        Matcher matcher = FtraceParser.FTRACE_PATTERN.matcher(""); //$NON-NLS-1$

        try {
            Files.lines(Paths.get(this.path))
                    .map(matcher::reset)
                    .filter(Matcher::matches)
                    .forEachOrdered(m -> {
                        String comm = m.group("comm"); //$NON-NLS-1$
                        Integer pid = Integer.parseInt(m.group("pid")); //$NON-NLS-1$
                        Integer cpu = Integer.parseInt(m.group("cpu")); //$NON-NLS-1$
                        Double timestamp = Double.parseDouble(m.group("timestamp")); //$NON-NLS-1$
                        String function = m.group(6);
                        String functionData = m.group("data"); //$NON-NLS-1$
                        System.out.println(comm + " " + pid + " " + cpu + " " + timestamp + " " + function + " " + functionData); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}