/* *****************************************************************************
 *  Name:    Atanas Dinev
 *  NetID:   adinev
 *  Course:  COS 451
 *
 *  Description:  Node class to represent the Node in the Self- balancing
 *  tree. Node has two types, a node which stores a site on the beachline and
 *  a node which stores a breakpoint. Every site node has a reference to an
 *  event from the event queue. The class supports the methods xc_int(y), which
 *  computes the x-cooordinate of the breakpoint a node stores, given the po-
 *  sition of the sweepline y, and yc_int(y) which computes the y-coordinate of
 *  the same interseation
 *
 **************************************************************************** */

public class Node {

    // tells whether the node is internal or not
    boolean internal;

    // references to the left, rigth and parent nodes
    Node left;
    Node right;
    Node parent;
    boolean parent_orientation;
    int height;
    // orientation of the edge from the parent 1 for rigth, 0 for left

    // variables in case this is a leaf node
    int point; // index of the point it stores
    Node left_intersection; // ref to the left intersesection of the arc
    Node right_intersection; // ref to the rigth intersection of the arc
    Node prev; // reference to the previous arc on the beach line
    Node next; // reference to the next arc on the beach line
    double xc; // the x-coordinate
    double yc; // the y-coordinate
    Event event;

    // variables in case the node stores an intersection
    Node left_point; // ref to the left point
    Node right_point; // ref to the right point
    Voronoi_edge vor_edge; // the voronoi edge the node traces out

    public Node() {
        this.height = 1;
    }

    // compute x-coordiante of the intersection based on the position of the sweep line
    public double xc_int(double y) {

        Node left_p = this.left_point; // left point
        Node right_p = this.right_point; // right points

        // check if the two points have the same y-coordinate
        if (left_p.yc == right_p.yc) {
            return (left_p.xc + right_p.xc) / 2;
        }

        // check if the sweepline has the same y-coordinate as some of the 2 points
        if (y == left_p.yc) return left_p.xc;
        if (y == right_p.yc) return right_p.xc;


        // find the x-coordinate of the intersection in the non-degenerate cases
        double A = left_p.yc - right_p.yc;
        double B = -2 * right_p.xc * (left_p.yc - y) + 2 * left_p.xc * (right_p.yc - y);
        double C = right_p.xc * right_p.xc * (left_p.yc - y) - left_p.xc * left_p.xc * (
                right_p.yc - y) - (right_p.yc - y) * (
                left_p.yc - y) * (left_p.yc - right_p.yc);

        return (-B - Math.sqrt(B * B - 4 * A * C)) / (2 * A);
    }

    // compute the y-coordinate of the intersection based on the position of the sweepline if the vor edge is vertical
    public double yc_int(double y) {
        Node left_p = this.left_point; // left point
        Node right_p = this.right_point; // right points

        double x = xc_int(y);
        return (1.0 / (2 * (left_p.yc - y))) * (
                (x - left_p.xc) * (x - left_p.xc) + left_p.yc * left_p.yc - y * y);
    }


    // unit testing
    public static void main(String[] args) {


    }
}
