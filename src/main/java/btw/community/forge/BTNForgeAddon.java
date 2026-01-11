package btw.community.forge;

import api.AddonHandler;
import api.BTWAddon;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class BTNForgeAddon extends BTWAddon {
    public static BTNForgeAddon INSTANCE = new BTNForgeAddon();
    public static final String MOD_ID = "betterthannothing";

    private EventBus eventBus = new EventBus();
    /**
     * The FML event bus. Subscribe here for FML related events
     *
     * @return the event bus
     */
    public EventBus bus()
    {
        return eventBus;
    }

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
        bus().register(new EventTest(), INSTANCE);
        bus().post(new TestEvent());
        System.out.println("Goodbye world!");
    }

    public static class TestEvent extends Event {
        public final String message = "Hello World!";

        public TestEvent() {
        }
    }

    public static class EventTest {
        public EventTest() {
        }

        @SubscribeEvent
        public void anEventListener(TestEvent event) {
            System.out.println(event.message);
        }
    }
}