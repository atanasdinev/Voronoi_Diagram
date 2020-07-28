/* *****************************************************************************
 *  Name:    Atanas Dinev
 *  NetID:   adinev
 *  Course:  COS 451
 *
 *  Description:  Implements a class that represents a half-edge with a
 *  direction. Contains references to previous and next edge, twin edge,
 *  incindent face, origin Vertex, destination Vertex, slope m, bias b,
 *  and boolean indicator box_edge to tell if the edge is on the bounding box.
 *  Support an equals() method to check if this edge is the same as another
 *  edge.
 **************************************************************************** */

public class Half_Edge {

    Vertex origin; // the origin of the half edge
    Vertex destination; // the destination of the half edge
    Half_Edge twin; // the twin of the half edge
    Face incidentFace; // the incident face of the half-edge
    Half_Edge prev; // the previous half-edge
    Half_Edge next; // the next half-edge
    double m; // slope
    double b; // bias
    boolean box_edge; // whether this edge is a bounding box edge


    // initialize a half edge with slope m and bias b
    public Half_Edge(double m, double b) {
        this.m = m;
        this.b = b;

    }

    // check is this edge is the same as another edge
    public boolean equals(Half_Edge that) {
        if (that == null) return false;
        else {
            if (this.origin == null && that.origin == null && that.destination != null
                    && this.destination != null) {
                return that.destination.equals(this.destination);
            }
            if (this.origin != null && that.origin != null && that.destination == null
                    && this.destination == null) {
                return that.origin.equals(this.origin);
            }
            if (this.origin == null && that.origin == null && that.destination == null
                    && this.destination == null) {
                return true;
            }
            if (this.origin != null && that.origin != null && that.destination != null
                    && this.destination != null) {
                return (this.origin.equals(that.origin) && this.destination
                        .equals(that.destination));
            }
            return false;
        }
    }


    // unit testing
    public static void main(String[] args) {

    }
}
