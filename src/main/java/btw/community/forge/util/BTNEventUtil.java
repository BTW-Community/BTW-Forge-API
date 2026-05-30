package btw.community.forge.util;

import cpw.mods.fml.common.eventhandler.ForgeEvent;
import net.legacyfabric.fabric.api.event.EventFactory;

import java.util.function.Consumer;

public class BTNEventUtil {
    /** Create a fabric event, wrapping the forge event with a consumer. Store this in a static final thing somewhere idk */
    public static <T extends ForgeEvent> net.legacyfabric.fabric.api.event.Event<Consumer<T>> createNoResult(Class<T> eventType) {
        return EventFactory.createArrayBacked(Consumer.class, (callbacks) -> (event -> {
            for (var callback : callbacks) {
                callback.accept(event);
                if (event.isCanceled()) {
                    return;
                }
            }
        }));
    }
}
