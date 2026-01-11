package cpw.mods.fml.common.eventhandler;

import btw.community.forge.BTNForgeAddon;
import com.google.common.base.Preconditions;
import cpw.mods.fml.common.FMLLog;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EventBus implements IEventExceptionHandler
{
    private static int maxID = 0;

    private ConcurrentHashMap<Object, ArrayList<IEventListener>> listeners = new ConcurrentHashMap<Object, ArrayList<IEventListener>>();
//    private Map<Object, ModContainer> listenerOwners = new MapMaker().weakKeys().weakValues().makeMap();
    private final int busID = maxID++;
    private IEventExceptionHandler exceptionHandler;

    public EventBus()
    {
        ListenerList.resize(busID + 1);
        exceptionHandler = this;
    }

    public EventBus(@NotNull IEventExceptionHandler handler)
    {
        this();
        Preconditions.checkArgument(handler != null, "EventBus exception handler can not be null");
        exceptionHandler = handler;
    }

    public void registerNew(Object target) {
        for (Method method : target.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(SubscribeEvent.class)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) {
                    throw new IllegalArgumentException(
                            "Method " + method + " has @SubscribeEvent annotation, but has " + parameterTypes.length +
                                    " arguments.  Event handler methods require a single argument."
                    );
                }

                Class<?> eventType = parameterTypes[0];

                if (!Event.class.isAssignableFrom(eventType)) {
                    throw new IllegalArgumentException("Method " + method + " has @SubscribeEvent annotation, but takes a argument that is not an Event " + eventType);
                }

                //noinspection unchecked Silence compiler
                register((Class<? extends Event>) eventType, method, target);
            }
        }
    }

    public boolean post(Event event)
    {
        var fabricEvent = BTNForgeAddon.EVENT_FACTORY_TO_CLASS.get(event.getClass());
        if (fabricEvent == null) {
            FMLLog.warning("No fabric event found for " + event.getClass().getSimpleName());
            return false;
        }
        ((Consumer<Event>) fabricEvent.invoker()).accept(event);
        return (event.isCancelable() ? event.isCanceled() : false);
    }

    private void register(Class<? extends Event> eventType, Method method, Object target) {
        net.legacyfabric.fabric.api.event.Event<Consumer<?>> event = BTNForgeAddon.EVENT_FACTORY_TO_CLASS.get(eventType);
        try {
            if (event == null) {
                Field eventField = eventType.getDeclaredField("EVENT");
                eventField.setAccessible(true);
                event = (net.legacyfabric.fabric.api.event.Event<Consumer<?>>) eventField.get(null);
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            event = BTNForgeAddon.EVENT_FACTORY_TO_CLASS.get(eventType);
        }
        if (event == null) {
            throw new IllegalArgumentException("This event is not registered: " + eventType.getSimpleName());
        }
        event.register((eventObj) -> {
            try {
                method.invoke(target, eventObj);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

//    /**
//     * Now requires the {@link BTWAddon} context, can be obtained by passing your addon class
//     * */
//    public void register(Object target, Class<? extends BTWAddon> caller) {
//        register(target, AddonHandler.modList.get(caller));
//    }
//
//    /**
//     * Now requires the {@link BTWAddon} context, can be obtained by passing the instance
//     * */
//    public void register(Object target, BTWAddon caller)
//    {
//        if (listeners.containsKey(target))
//        {
//            return;
//        }
//
//        ModContainer activeModContainer = FabricLoader.getInstance().getModContainer(caller.getModID()).orElse(null);
//        if (activeModContainer == null)
//        {
//            FMLLog.log(Level.ERROR, new Throwable(), "Unable to determine registrant mod for %s. This is a critical error and should be impossible", target);
////            activeModContainer = Loader.instance().getMinecraftModContainer();
//            activeModContainer = FabricLoader.getInstance().getModContainer("minecraft").orElse(null);
//        }
////        listenerOwners.put(target, activeModContainer);
//        Set<? extends Class<?>> supers = TypeToken.of(target.getClass()).getTypes().rawTypes();
//        for (Method method : target.getClass().getMethods())
//        {
//            for (Class<?> cls : supers)
//            {
//                try
//                {
//                    Method real = cls.getDeclaredMethod(method.getName(), method.getParameterTypes());
//                    if (real.isAnnotationPresent(SubscribeEvent.class))
//                    {
//                        Class<?>[] parameterTypes = method.getParameterTypes();
//                        if (parameterTypes.length != 1)
//                        {
//                            throw new IllegalArgumentException(
//                                "Method " + method + " has @SubscribeEvent annotation, but requires " + parameterTypes.length +
//                                " arguments.  Event handler methods must require a single argument."
//                            );
//                        }
//
//                        Class<?> eventType = parameterTypes[0];
//
//                        if (!Event.class.isAssignableFrom(eventType))
//                        {
//                            throw new IllegalArgumentException("Method " + method + " has @SubscribeEvent annotation, but takes a argument that is not an Event " + eventType);
//                        }
//
//                        register(eventType, target, method, activeModContainer);
//                        break;
//                    }
//                }
//                catch (NoSuchMethodException e)
//                {
//                    ;
//                }
//            }
//        }
//    }
//
//    private void register(Class<?> eventType, Object target, Method method, ModContainer owner)
//    {
//        try
//        {
//            Constructor<?> ctr = eventType.getConstructor();
//            ctr.setAccessible(true);
//            Event event = (Event)ctr.newInstance();
//            ASMEventHandler listener = new ASMEventHandler(target, method, owner);
//            event.getListenerList().register(busID, listener.getPriority(), listener);
//
//            ArrayList<IEventListener> others = listeners.get(target);
//            if (others == null)
//            {
//                others = new ArrayList<IEventListener>();
//                listeners.put(target, others);
//            }
//            others.add(listener);
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//    }

    @Deprecated(since = "Currently NOOP")
    public void unregister(Object object)
    {
//        ArrayList<IEventListener> list = listeners.remove(object);
//        if (list == null)
//            return;
//        for (IEventListener listener : list)
//        {
//            ListenerList.unregisterAll(busID, listener);
//        }
    }

//    public boolean post(Event event)
//    {
//        IEventListener[] listeners = event.getListenerList().getListeners(busID);
//        int index = 0;
//        try
//        {
//            for (; index < listeners.length; index++)
//            {
//                listeners[index].invoke(event);
//            }
//        }
//        catch (Throwable throwable)
//        {
//            exceptionHandler.handleException(this, event, listeners, index, throwable);
//            Throwables.propagate(throwable);
//        }
//        return (event.isCancelable() ? event.isCanceled() : false);
//    }

    @Override
    public void handleException(EventBus bus, Event event, IEventListener[] listeners, int index, Throwable throwable)
    {
        FMLLog.log(Level.ERROR, throwable, "Exception caught during firing event %s:", event);
        FMLLog.log(Level.ERROR, "Index: %d Listeners:", index);
        for (int x = 0; x < listeners.length; x++)
        {
            FMLLog.log(Level.ERROR, "%d: %s", x, listeners[x]);
        }
    }
}