/* *****************************************************************************
 *  Name:    Atanas Dinev
 *  NetID:   adinev
 *  Course:  COS 451
 *
 *  Description:   Vornoi-edge class represents the line and has refs to the
 *                 2 half-edges on both sides. Handles case when the voronoi
 *                 edge is vertical. Stores the slope and bias term of the line
 *                 it represents. Supports a method to reference the half-edge
 *                 incident with face i.
 **************************************************************************** */


public class Voronoi_edge {
    double m;
    // slope of the line in case it is not vertical, and the x-coordinate in case it is vertical
    double b; // the translation bias (only for non-vertical lines)

    // the two twin half-edges attached to that voronoi edge
    Half_Edge edge_one;
    Half_Edge edge_two;

    Vertex infinity_point; // a vertex to represent the infinity point in case the vor edge is
    boolean vertical; // is the voronoi edge vertical
    // is the voronoi edge the first edge when the 2 topmost points have the same y-coordinate
    boolean upper_edge;


    // initialize a Voronoi_edge
    public Voronoi_edge(double m, double b) {
        this.m = m;
        this.b = b;
        this.vertical = false;

    }

    // reference to the half-edge to the side of site i
    public Half_Edge get_half_edge(int i) {
        if (this.edge_one.incidentFace.index == i) {
            return edge_one;
        }
        else {
            return edge_two;
        }

    }

    // unit testing
    public static void main(String[] args) {

    }
}
