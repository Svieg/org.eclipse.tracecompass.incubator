package org.eclipse.tracecompass.incubator.internal.ftrace.core.event;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;

/**
 * @author Guillaume Champagne, Alexis-Maurer Fortin, Hugo Genesse, Pierre-Yves
 *          Lajoie, Eva Terriault
 */
public class FtraceEventTypeFactory {

    private static final Map<String, TmfEventType> types = new HashMap<>();

    /**
     * @param eventName Name of the event
     * @return ITmfEventType corresponding to event
     */
    public static ITmfEventType get(String eventName) {
        if (eventName == null || eventName.isEmpty()) {
            return null;
        }

        TmfEventType event = null;
        if (types.containsKey(eventName)) {
            event = types.get(eventName);
        }
        else {
            event = new TmfEventType(eventName, null);
            types.put(eventName, event);
        }
        return event;
    }
}
