package org.thighfill.ambi.event;

public interface EventListener<E> {
    void eventFired(E context);
}
