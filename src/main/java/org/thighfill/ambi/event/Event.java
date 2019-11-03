package org.thighfill.ambi.event;

public class Event<E> {

    private final EventDriver<E> _driver;

    Event(EventDriver<E> driver){
        _driver = driver;
    }

    public void addListener(EventListener<E> listener){
        _driver.addListener(listener);
    }

    public void removeListener(EventListener<E> listener){
        _driver.removeListener(listener);
    }
}
