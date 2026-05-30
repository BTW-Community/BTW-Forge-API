package btw.community.forge;

import api.BTWAddon;
import btw.community.forge.util.BTNEventUtil;
import cpw.mods.fml.common.eventhandler.ForgeEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.legacyfabric.fabric.api.event.Event;
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
    public static Map<Class<? extends ForgeEvent>, Event<Consumer<?>>> EVENT_FACTORY_TO_CLASS = new HashMap<>();

    public static Event<Consumer<TestEvent>> TEST = BTNEventUtil.createNoResult(TestEvent.class);

}