package fertilizer;

import javafx.event.Event;
import javafx.event.EventType;

public class SolveItEvent extends Event {
    public static final EventType<SolveItEvent> READY = new EventType<SolveItEvent>("Ready");
    private static final long serialVersionUID = 1L;

    public SolveItEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }
    
    public SolveItEvent() {
        this(READY);
    }
}
