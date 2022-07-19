package org.prithvidiamond1.Listeners;

import org.javacord.api.DiscordApi;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.util.event.ListenerManager;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ListenerRegistry {
    private final Map<GloballyAttachableListener, Collection<ListenerManager<? extends GloballyAttachableListener>>> listenerMap;

    public ListenerRegistry(List<GloballyAttachableListener> listenerList, DiscordApi api){
        this.listenerMap = new ConcurrentHashMap<>();

        for (var listener: listenerList){
            addListener(listener, api);
        }
    }

    public void addListener(GloballyAttachableListener listener, DiscordApi api){
        var listenerManagers = api.addListener(listener);
        listenerMap.put(listener, listenerManagers);
    }
}
