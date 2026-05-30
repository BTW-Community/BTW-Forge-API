package btw.community.forge;

import api.BTWAddon;
import cpw.mods.fml.common.eventhandler.ForgeEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.legacyfabric.fabric.api.event.EventFactory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BTNForgeAddon extends BTWAddon {
    public static BTNForgeAddon INSTANCE = new BTNForgeAddon();
    public static final String MOD_ID = "betterthannothing";

    public BTNForgeAddon() {
        super();
        this.modID = MOD_ID;
    }

    @Override
    public void postSetup() {
        BTWAddon.addResourcePackDomain("forge");
    }

    @Override
    public void initialize() {
        var event = new EventTest();
        MinecraftForge.EVENT_BUS.registerNew(event);
        var testEvn = new TestEvent("Hello World!");
//        TEST.invoker().accept(testEvn);
//        MinecraftForge.EVENT_BUS.register(new EventTest(), INSTANCE);
        MinecraftForge.EVENT_BUS.post(testEvn);
        System.out.println("Goodbye world!");
    }

    @Override
    public void postInitialize() {
        var testEvn = new TestEvent("Hello again World!");
        MinecraftForge.EVENT_BUS.post(testEvn);
    }

    public static class TestEvent extends ForgeEvent {
        public final String message;

        public TestEvent(String message) {
            this.message = message;
        }
    }
    public static Map<Class<? extends ForgeEvent>, net.legacyfabric.fabric.api.event.Event<Consumer<?>>> EVENT_FACTORY_TO_CLASS = new HashMap<>();

    public static net.legacyfabric.fabric.api.event.Event<Consumer<TestEvent>> TEST = createNoResult(TestEvent.class);

    /** Create a fabric event, wrapping the forge event with a consumer. Store this in a static final thing somewhere idk */
    public static <T extends Event> net.legacyfabric.fabric.api.event.Event<Consumer<T>> createNoResult(Class<T> eventType) {
        net.legacyfabric.fabric.api.event.Event<Consumer<T>> ret = EventFactory.createArrayBacked(Consumer.class, (callbacks) -> (event -> {
            for (var callback : callbacks) {
                callback.accept(event);
                if (event.isCanceled()) {
                    return;
                }
            }
        }));
        var added = add(eventType, ret);
        if (added != null) {
            return added;
        }
        return ret;
    }

    private static <T> net.legacyfabric.fabric.api.event.Event<Consumer<T>> add(Class<? extends ForgeEvent> eventType, net.legacyfabric.fabric.api.event.Event<Consumer<T>> event) {
        return EVENT_FACTORY_TO_CLASS.putIfAbsent(eventType, (net.legacyfabric.fabric.api.event.Event) event);
    }

    public static class EventTest {
        public EventTest() {
            MinecraftForge.EVENT_BUS.registerNew(this);
        }

        @SubscribeEvent
        public void testEvent(TestEvent event) {
            System.out.println(event.message);
        }

        @SubscribeEvent
        public void checkSpawn(LivingSpawnEvent.CheckSpawn event) {

            System.out.printf("Checking spawn for %s at: %.2f %.2f %.2f%n", event.entity.getClass().getSimpleName(), event.x, event.y, event.z);
        }
    }
}