/* *****************************************************************************
 *  Name:    Atanas Dinev
 *  NetID:   adinev
 *  Course:  COS 451
 *
 *  Description:  Event class to represent the events in the event queue. An
 *  event can be a site event or a circle event. The class supports comparing
 *  two events.
 **************************************************************************** */

import edu.princeton.cs.algs4.Point2D;

public class Event implements Comparable<Event> {
    Point2D p; // the event stores a point
    boolean circle_event; // check if the event is a circle event
    boolean site_event; // check if the event is site event
    boolean valid; // check if the circle event is still valid
    int index; // index of the site stored at that event
    Node node; // reference to the node


    // create an empty event object
    public Event(double y, double x, Node node) {
        this.p = new Point2D(x, y);
        this.valid = true;
        this.node = node;
    }


    // compare this event to another event
    @Override
    public int compareTo(Event o) {
        return this.p.compareTo(o.p);
    }


    // main
    public static void main(String[] args) {

    }


}

