package org.thighfill.ambi.event;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class EventDriver<E> {

    private final List<EventListener<E>> _listeners = new LinkedList<>();
    private final Event<E> myEvent;

    public EventDriver(){
        myEvent = new Event<>(this);
    }

    public void addListener(EventListener<E> listener) {
        _listeners.add(listener);
    }

    public void fire(E context){
        _listeners.forEach(l -> l.eventFired(context));
    }

    public Event<E> getMyEvent() {
        return myEvent;
    }

    public void removeListener(EventListener<E> listener) {
        _listeners.remove(listener);
    }
}
