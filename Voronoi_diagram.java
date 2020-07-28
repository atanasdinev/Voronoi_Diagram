/* *****************************************************************************
 *  Name:    Atanas Dinev
 *  NetID:   adinev
 *  Course:  COS 451
 *
 *  Description:  Implements Fortune's sweepline algorithm for computing the
 *  Voronoi diagram of a set of n points in the plane. The algorithm also
 *  handles degenrate cases when 2 of the points have the same y-coordinate.
 *  Returns the Voronoi diagram in a doubly connected edge list.
 *
 **************************************************************************** */

import edu.princeton.cs.algs4.MaxPQ;
import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.StdRandom;

import java.awt.Color;
import java.util.ArrayList;

public class Voronoi_diagram {

    int N; // number of point sites
    ArrayList<Voronoi_edge> vor_edges; // the vornoi edges
    ArrayList<Vertex> vor_vertices; // the voronoi vertices
    Face[] vor_faces; // the faces
    ArrayList<Half_Edge> vor_half_edges; // the half edges
    MaxPQ<Event> pq;// the event queue
    double[] xc; // the x-coordinates of the sites
    double[] yc; // the y-coordinates of the sites

    Voronoi_edge first_vertical; // the first vertical edge if ti exists

    double ycurr; // the current position of the sweepline

    // bounding box parameters
    double x0;
    double x1;
    double y0;
    double y1;

    // a self-balancing tree to represent the beachline and perform
    // O(logn) insertion and deletion
    SBT tree;


    // create the Voronoi diagram
    public Voronoi_diagram(int N, double[] xc, double[] yc) {

        // initialize the faces, one for each site
        vor_faces = new Face[N];
        for (int i = 0; i < N; i++) {
            Face face = new Face();
            face.index = i;
            vor_faces[i] = face;
        }

        // initialize the vor edges
        this.vor_edges = new ArrayList<>();

        // initialize the verices
        this.vor_vertices = new ArrayList<>();

        // initialize the half edges
        this.vor_half_edges = new ArrayList<>();

        // initialize the size
        this.N = N;

        // initialize the site arrays
        this.xc = xc;
        this.yc = yc;

        // initialize the event queue with the sites
        pq = new MaxPQ<>(N);

        for (int i = 0; i < N; i++) {
            Node newnode = new Node();
            newnode.point = i;
            newnode.xc = xc[i];
            newnode.yc = yc[i];
            newnode.internal = false;
            Event event = new Event(yc[i], xc[i], newnode);
            event.index = i;
            event.site_event = true;
            newnode.event = event;
            pq.insert(event);
        }


        // initialize the self-balancing tree
        tree = new SBT(xc, yc);


        // generate the voronoi diagram
        generateVoronoi();


        // initialize the bounding box parameters
        double[] a = bounding_box();
        x0 = a[0];
        x1 = a[1];
        y0 = a[2];
        y1 = a[3];

        // cut the edges
        cut_edges(tree.root);

        // cut the first edge if it vertical and neccessary
        cut_first_edge();

        // update the DCEL with the edges on the boudning box
        update_dcel();
    }

    // generate the Voronoi diagram
    public void generateVoronoi() {

        // get the first valid event, while deleting the ones that are invalid
        while (!pq.isEmpty()) {
            Event e = pq.delMax();
            while (!e.valid) {
                if (pq.isEmpty()) {
                    break;
                }
                else {
                    e = pq.delMax();
                }
            }
            if (e.valid) {

                // handle site event
                if (e.site_event) {
                    handleSiteEvent(e);
                }

                // handle circle event
                if (e.circle_event) {
                    handleCircleEvent(e);

                }
            }
        }

    }

    // update the DCEL (Doubly Connected Edge List)
    public void update_dcel() {

        // go through all the faces
        for (int i = 0; i < N; i++) {
            Face current_face = vor_faces[i];
            Half_Edge current = current_face.half_edge;
            Half_Edge start = current_face.half_edge;

            // detect if the face is infinte
            boolean flag = false;
            while (current.next != null) {
                current = current.next;
                if (current.equals(start)) {
                    flag = true;
                    break;

                }

            }


            // if face is infinite stop and continue the loop
            if (flag) continue;


            // find the two infinite half-edges of that face
            Half_Edge edge_1 = current;
            Half_Edge edge_2 = current_face.half_edge;
            while (edge_2.prev != null) {
                edge_2 = edge_2.prev;
            }

            // take their vertices which are not infinite
            Vertex v_2 = edge_2.origin;
            Vertex v_1 = edge_1.destination;


            // the vertices of the box
            Vertex lb = new Vertex(x0, y0);
            Vertex rb = new Vertex(x1, y0);
            Vertex lu = new Vertex(x0, y1);
            Vertex ru = new Vertex(x1, y1);

            // add the vertices of the box
            vor_vertices.add(lb);
            vor_vertices.add(rb);
            vor_vertices.add(lu);
            vor_vertices.add(ru);


            // cut the infinite edges with the bounding box with considering cases for the
            // position of the the two infinite edges on the infinte face

            // case if the cut lies on a side of the bouding box
            boolean indicator = (v_1.yc == y1 && v_2.yc == y1) || (v_1.yc == y0 && v_2.yc == y0)
                    || (v_1.xc == x0 && v_2.xc == x0) || (v_1.xc == x1 && v_2.xc == x1);

            if (indicator) {
                Half_Edge newedge = new Half_Edge(0, 0);
                newedge.box_edge = true;
                edge_1.next = newedge;
                newedge.prev = edge_1;
                edge_2.prev = newedge;
                newedge.next = edge_2;
                newedge.origin = v_1;
                newedge.destination = v_2;
                newedge.incidentFace = current_face;

                vor_half_edges.add(newedge);

            }

            // case if the cut contains the upper right angle
            if (v_1.xc == x1 && v_2.yc == y1) {
                Half_Edge newedge_1 = new Half_Edge(0, 0);
                Half_Edge newedge_2 = new Half_Edge(0, 0);
                newedge_2.incidentFace = current_face;
                newedge_1.incidentFace = current_face;
                newedge_1.box_edge = true;
                newedge_2.box_edge = true;

                edge_1.next = newedge_1;
                newedge_1.prev = edge_1;
                newedge_1.next = newedge_2;
                newedge_2.prev = newedge_1;
                newedge_2.next = edge_2;
                edge_2.prev = newedge_2;

                newedge_1.origin = v_1;
                newedge_1.destination = ru;
                newedge_2.origin = ru;
                newedge_2.destination = v_2;

                vor_half_edges.add(newedge_1);
                vor_half_edges.add(newedge_2);
            }


            // case if the cut contains the upper left angle
            if (v_1.yc == y1 && v_2.xc == x0) {
                Half_Edge newedge_1 = new Half_Edge(0, 0);
                Half_Edge newedge_2 = new Half_Edge(0, 0);
                newedge_2.incidentFace = current_face;
                newedge_1.incidentFace = current_face;
                newedge_1.box_edge = true;
                newedge_2.box_edge = true;

                edge_1.next = newedge_1;
                newedge_1.prev = edge_1;
                newedge_1.next = newedge_2;
                newedge_2.prev = newedge_1;
                newedge_2.next = edge_2;
                edge_2.prev = newedge_2;

                newedge_1.origin = v_1;
                newedge_1.destination = lu;
                newedge_2.origin = lu;
                newedge_2.destination = v_2;

                vor_half_edges.add(newedge_1);
                vor_half_edges.add(newedge_2);
            }

            // case if the cut contains the bottom left angle
            if (v_1.xc == x0 && v_2.yc == y0) {
                Half_Edge newedge_1 = new Half_Edge(0, 0);
                Half_Edge newedge_2 = new Half_Edge(0, 0);
                newedge_2.incidentFace = current_face;
                newedge_1.incidentFace = current_face;
                newedge_1.box_edge = true;
                newedge_2.box_edge = true;

                edge_1.next = newedge_1;
                newedge_1.prev = edge_1;
                newedge_1.next = newedge_2;
                newedge_2.prev = newedge_1;
                newedge_2.next = edge_2;
                edge_2.prev = newedge_2;

                newedge_1.origin = v_1;
                newedge_1.destination = lb;
                newedge_2.origin = lb;
                newedge_2.destination = v_2;

                vor_half_edges.add(newedge_1);
                vor_half_edges.add(newedge_2);
            }

            // case if the cut contains the bottom right angle
            if (v_1.yc == y0 && v_2.xc == x1) {
                Half_Edge newedge_1 = new Half_Edge(0, 0);
                Half_Edge newedge_2 = new Half_Edge(0, 0);
                newedge_2.incidentFace = current_face;
                newedge_1.incidentFace = current_face;
                newedge_1.box_edge = true;
                newedge_2.box_edge = true;

                edge_1.next = newedge_1;
                newedge_1.prev = edge_1;
                newedge_1.next = newedge_2;
                newedge_2.prev = newedge_1;
                newedge_2.next = edge_2;
                edge_2.prev = newedge_2;

                newedge_1.origin = v_1;
                newedge_1.destination = rb;
                newedge_2.origin = rb;
                newedge_2.destination = v_2;
                vor_half_edges.add(newedge_1);
                vor_half_edges.add(newedge_2);
            }

            // 4 cases if the cut passes through two opposite sides of the bounding box
            if (v_1.xc == x1 && v_2.xc == x0) {

                Half_Edge newedge_1 = new Half_Edge(0, 0);
                Half_Edge newedge_2 = new Half_Edge(0, 0);
                Half_Edge newedge_3 = new Half_Edge(0, 0);


                newedge_1.box_edge = true;
                newedge_2.box_edge = true;
                newedge_3.box_edge = true;


                edge_1.next = newedge_1;
                newedge_1.prev = edge_1;
                newedge_1.next = newedge_2;
                newedge_2.prev = newedge_1;
                newedge_2.next = newedge_3;
                newedge_3.prev = newedge_2;
                newedge_3.next = edge_2;
                edge_2.prev = newedge_3;

                newedge_1.origin = v_1;
                newedge_1.destination = ru;
                newedge_2.origin = ru;
                newedge_2.destination = lu;
                newedge_3.origin = lu;
                newedge_3.destination = v_2;

                vor_half_edges.add(newedge_1);
                vor_half_edges.add(newedge_2);
                vor_half_edges.add(newedge_3);

            }

            if (v_1.yc == y1 && v_2.yc == y0) {

                Half_Edge newedge_1 = new Half_Edge(0, 0);
                Half_Edge newedge_2 = new Half_Edge(0, 0);
                Half_Edge newedge_3 = new Half_Edge(0, 0);


                newedge_1.box_edge = true;
                newedge_2.box_edge = true;
                newedge_3.box_edge = true;


                edge_1.next = newedge_1;
                newedge_1.prev = edge_1;
                newedge_1.next = newedge_2;
                newedge_2.prev = newedge_1;
                newedge_2.next = newedge_3;
                newedge_3.prev = newedge_2;
                newedge_3.next = edge_2;
                edge_2.prev = newedge_3;

                newedge_1.origin = v_1;
                newedge_1.destination = lu;
                newedge_2.origin = lu;
                newedge_2.destination = lb;
                newedge_3.origin = lb;
                newedge_3.destination = v_2;

                vor_half_edges.add(newedge_1);
                vor_half_edges.add(newedge_2);
                vor_half_edges.add(newedge_3);

            }

            if (v_1.xc == x0 && v_2.xc == x1) {

                Half_Edge newedge_1 = new Half_Edge(0, 0);
                Half_Edge newedge_2 = new Half_Edge(0, 0);
                Half_Edge newedge_3 = new Half_Edge(0, 0);


                newedge_1.box_edge = true;
                newedge_2.box_edge = true;
                newedge_3.box_edge = true;


                edge_1.next = newedge_1;
                newedge_1.prev = edge_1;
                newedge_1.next = newedge_2;
                newedge_2.prev = newedge_1;
                newedge_2.next = newedge_3;
                newedge_3.prev = newedge_2;
                newedge_3.next = edge_2;
                edge_2.prev = newedge_3;

                newedge_1.origin = v_1;
                newedge_1.destination = lb;
                newedge_2.origin = lb;
                newedge_2.destination = rb;
                newedge_3.origin = rb;
                newedge_3.destination = v_2;

                vor_half_edges.add(newedge_1);
                vor_half_edges.add(newedge_2);
                vor_half_edges.add(newedge_3);

            }

            if (v_1.yc == y0 && v_2.yc == y1) {

                Half_Edge newedge_1 = new Half_Edge(0, 0);
                Half_Edge newedge_2 = new Half_Edge(0, 0);
                Half_Edge newedge_3 = new Half_Edge(0, 0);


                newedge_1.box_edge = true;
                newedge_2.box_edge = true;
                newedge_3.box_edge = true;


                edge_1.next = newedge_1;
                newedge_1.prev = edge_1;
                newedge_1.next = newedge_2;
                newedge_2.prev = newedge_1;
                newedge_2.next = newedge_3;
                newedge_3.prev = newedge_2;
                newedge_3.next = edge_2;
                edge_2.prev = newedge_3;

                newedge_1.origin = v_1;
                newedge_1.destination = rb;
                newedge_2.origin = rb;
                newedge_2.destination = ru;
                newedge_3.origin = ru;
                newedge_3.destination = v_2;

                vor_half_edges.add(newedge_1);
                vor_half_edges.add(newedge_2);
                vor_half_edges.add(newedge_3);

            }


            // 4 cases if the cut leaves out an angle of the box and contains the rest
            if (v_1.xc == x1 && v_2.yc == y0) {
                Half_Edge newedge_1 = new Half_Edge(0, 0);
                Half_Edge newedge_2 = new Half_Edge(0, 0);
                Half_Edge newedge_3 = new Half_Edge(0, 0);
                Half_Edge newedge_4 = new Half_Edge(0, 0);

                newedge_1.box_edge = true;
                newedge_2.box_edge = true;
                newedge_3.box_edge = true;
                newedge_4.box_edge = true;

                edge_1.next = newedge_1;
                newedge_1.prev = edge_1;
                newedge_1.next = newedge_2;
                newedge_2.prev = newedge_1;
                newedge_2.next = newedge_3;
                newedge_3.prev = newedge_2;
                newedge_3.next = newedge_4;
                newedge_4.prev = newedge_3;
                newedge_4.next = edge_2;
                edge_2.prev = newedge_4;

                newedge_1.origin = v_1;
                newedge_1.destination = ru;
                newedge_2.origin = ru;
                newedge_2.destination = lu;
                newedge_3.origin = lu;
                newedge_3.destination = lb;
                newedge_4.origin = lb;
                newedge_4.destination = v_2;

                vor_half_edges.add(newedge_1);
                vor_half_edges.add(newedge_2);
                vor_half_edges.add(newedge_3);
                vor_half_edges.add(newedge_4);
            }

            if (v_1.yc == y1 && v_2.xc == x1) {
                Half_Edge newedge_1 = new Half_Edge(0, 0);
                Half_Edge newedge_2 = new Half_Edge(0, 0);
                Half_Edge newedge_3 = new Half_Edge(0, 0);
                Half_Edge newedge_4 = new Half_Edge(0, 0);

                newedge_1.box_edge = true;
                newedge_2.box_edge = true;
                newedge_3.box_edge = true;
                newedge_4.box_edge = true;

                edge_1.next = newedge_1;
                newedge_1.prev = edge_1;
                newedge_1.next = newedge_2;
                newedge_2.prev = newedge_1;
                newedge_2.next = newedge_3;
                newedge_3.prev = newedge_2;
                newedge_3.next = newedge_4;
                newedge_4.prev = newedge_3;
                newedge_4.next = edge_2;
                edge_2.prev = newedge_4;

                newedge_1.origin = v_1;
                newedge_1.destination = lu;
                newedge_2.origin = lu;
                newedge_2.destination = lb;
                newedge_3.origin = lb;
                newedge_3.destination = rb;
                newedge_4.origin = rb;
                newedge_4.destination = v_2;

                vor_half_edges.add(newedge_1);
                vor_half_edges.add(newedge_2);
                vor_half_edges.add(newedge_3);
                vor_half_edges.add(newedge_4);
            }

            if (v_1.xc == x0 && v_2.yc == y1) {
                Half_Edge newedge_1 = new Half_Edge(0, 0);
                Half_Edge newedge_2 = new Half_Edge(0, 0);
                Half_Edge newedge_3 = new Half_Edge(0, 0);
                Half_Edge newedge_4 = new Half_Edge(0, 0);

                newedge_1.box_edge = true;
                newedge_2.box_edge = true;
                newedge_3.box_edge = true;
                newedge_4.box_edge = true;

                edge_1.next = newedge_1;
                newedge_1.prev = edge_1;
                newedge_1.next = newedge_2;
                newedge_2.prev = newedge_1;
                newedge_2.next = newedge_3;
                newedge_3.prev = newedge_2;
                newedge_3.next = newedge_4;
                newedge_4.prev = newedge_3;
                newedge_4.next = edge_2;
                edge_2.prev = newedge_4;

                newedge_1.origin = v_1;
                newedge_1.destination = lb;
                newedge_2.origin = lb;
                newedge_2.destination = rb;
                newedge_3.origin = rb;
                newedge_3.destination = ru;
                newedge_4.origin = ru;
                newedge_4.destination = v_2;

                vor_half_edges.add(newedge_1);
                vor_half_edges.add(newedge_2);
                vor_half_edges.add(newedge_3);
                vor_half_edges.add(newedge_4);
            }

            if (v_1.yc == y0 && v_2.xc == x0) {
                Half_Edge newedge_1 = new Half_Edge(0, 0);
                Half_Edge newedge_2 = new Half_Edge(0, 0);
                Half_Edge newedge_3 = new Half_Edge(0, 0);
                Half_Edge newedge_4 = new Half_Edge(0, 0);

                newedge_1.box_edge = true;
                newedge_2.box_edge = true;
                newedge_3.box_edge = true;
                newedge_4.box_edge = true;

                edge_1.next = newedge_1;
                newedge_1.prev = edge_1;
                newedge_1.next = newedge_2;
                newedge_2.prev = newedge_1;
                newedge_2.next = newedge_3;
                newedge_3.prev = newedge_2;
                newedge_3.next = newedge_4;
                newedge_4.prev = newedge_3;
                newedge_4.next = edge_2;
                edge_2.prev = newedge_4;

                newedge_1.origin = v_1;
                newedge_1.destination = rb;
                newedge_2.origin = rb;
                newedge_2.destination = ru;
                newedge_3.origin = ru;
                newedge_3.destination = lu;
                newedge_4.origin = lu;
                newedge_4.destination = v_2;

                vor_half_edges.add(newedge_1);
                vor_half_edges.add(newedge_2);
                vor_half_edges.add(newedge_3);
                vor_half_edges.add(newedge_4);
            }
        }
    }


    // cuts the first vertical edge if the first two sites have the same y-coordinate
    public void cut_first_edge() {
        if (first_vertical == null) return;

        // get the 2 half-edges of that voronoi edge
        Half_Edge half_edge_1 = first_vertical.edge_one;
        Half_Edge half_edge_2 = first_vertical.edge_two;

        // get the finite end of the voronoi edge
        Vertex vertex;
        if (half_edge_1.origin == null) {
            vertex = half_edge_1.destination;
        }
        else {
            vertex = half_edge_1.origin;
        }

        Vertex on_box = new Vertex(first_vertical.m, y1);

        // update the half_edge pointers
        if (half_edge_1.origin == null) {
            half_edge_1.origin = on_box;
            half_edge_2.destination = on_box;
        }
        if (half_edge_1.destination == null) {
            half_edge_2.origin = on_box;
            half_edge_1.destination = on_box;
        }

        // add the new vertex to the list of vertices
        vor_vertices.add(on_box);


    }

    // cut all infinite edges with the bounding box
    // these adge corrspond to the internal nodes left in the tree after generating the voronoi diagram
    public void cut_edges(Node node) {
        if (!node.internal) return; // if the node is internal return

        Voronoi_edge vor_edge = node.vor_edge; // get the voronoi edge that this node represents

        // get the 2 half-edges of that voronoi edge
        Half_Edge half_edge_1 = vor_edge.edge_one;
        Half_Edge half_edge_2 = vor_edge.edge_two;


        // check if the voronoi edge is the perpendicular bisector of the topmost 2 points with the same y-coordinate
        if (vor_edge.upper_edge) {
            double[] a = intersection_box(x0, x1, y0, y1, vor_edge);
            Vertex v = new Vertex(a[2], a[3]);
            if (half_edge_1.origin == null) {
                half_edge_1.origin = v;
                half_edge_2.destination = v;
            }
            else {
                half_edge_2.origin = v;
                half_edge_1.destination = v;
            }
        }

        // handle the other cases
        else {

            // check if the voronoi edge is infinite in both directions
            if (half_edge_1.origin == null && half_edge_1.destination == null) {

                double[] a = intersection_box(x0, x1, y0, y1, vor_edge);
                Vertex v1 = new Vertex(a[0], a[1]);
                Vertex v2 = new Vertex(a[2], a[3]);

                half_edge_1.origin = v1;
                half_edge_1.destination = v2;
                half_edge_2.destination = v1;
                half_edge_2.origin = v2;
                vor_vertices.add(v1);
                vor_vertices.add(v2);
            }

            // handle the case where the voronoi edge has exactly one finite end of the voronoi edge
            else {

                // get the finite end of the voronoi edge
                Vertex vertex;
                if (half_edge_1.origin == null) {
                    vertex = half_edge_1.destination;
                }
                else {
                    vertex = half_edge_1.origin;
                }


                // get the parameter of the line traced by the intersection node
                double m = vor_edge.m;
                double b = vor_edge.b;

                // find the coordinates of a point on the infinite edge ray
                double x_c = node.xc_int(ycurr - 1);
                double y_c = m * x_c + b;

                // find the interseaction points with the bounding box
                double[] a = intersection_box(x0, x1, y0, y1, vor_edge);

                // get the points of intersection of the voronoi edge with the boudning box
                Vertex one = new Vertex(a[0], a[1]);
                Vertex two = new Vertex(a[2], a[3]);

                // find a point on_ray on the infinite ray starting from vertex along vor_edge
                Vertex on_ray;

                // handle case if the vor_edge is vertical
                if (vor_edge.vertical) {
                    on_ray = new Vertex(x_c, node.yc_int(ycurr - 1));
                }

                // handle case if the vor_edge is not vertical
                else {
                    on_ray = new Vertex(x_c, y_c);
                }


                // distances from on_ray to the intersection points from the boudning box
                double d1 = squared_distance(one, on_ray);
                double d2 = squared_distance(two, on_ray);


                // determine which point of the 2 intersection points will be new vertex on the infinite edge (one or two)
                if (d1 >= d2) {
                    vor_edge.infinity_point = two;
                }
                else {
                    vor_edge.infinity_point = one;
                }


                // update the half_edge pointers
                if (half_edge_1.origin == null) {
                    half_edge_1.origin = vor_edge.infinity_point;
                    half_edge_2.destination = vor_edge.infinity_point;
                }
                if (half_edge_1.destination == null) {
                    half_edge_2.origin = vor_edge.infinity_point;
                    half_edge_1.destination = vor_edge.infinity_point;
                }

                // add the new vertex to the list of vertices
                vor_vertices.add(vor_edge.infinity_point);
            }

        }


        cut_edges(node.left); // call the function on the left child
        cut_edges(node.right); // call the function on the right child


    }

    // finds a bounding box for the voronoi diagram
    // returns x0, x1, y0, y1
    public double[] bounding_box() {
        double x0 = xc[0];
        double y0 = yc[0];
        double x1 = xc[0];
        double y1 = yc[0];

        // fit all the sites
        for (int i = 0; i < N; i++) {
            if (xc[i] < x0) x0 = xc[i];
            if (xc[i] > x1) x1 = xc[i];
            if (yc[i] < y0) y0 = yc[i];
            if (yc[i] > y1) y1 = yc[i];

        }

        // fit all the veritices of the voronoi diagram
        for (int i = 0; i < vor_vertices.size(); i++) {
            if (vor_vertices.get(i).xc < x0) x0 = vor_vertices.get(i).xc;
            if (vor_vertices.get(i).xc > x1) x1 = vor_vertices.get(i).xc;
            if (vor_vertices.get(i).yc < y0) y0 = vor_vertices.get(i).yc;
            if (vor_vertices.get(i).yc > y1) y1 = vor_vertices.get(i).yc;
        }

        // store the parameters of the boudning box in an array
        double[] a = new double[4];
        a[0] = x0 - 0.1;
        a[1] = x1 + 0.1;
        a[2] = y0 - 0.1;
        a[3] = y1 + 0.1;
        return a;

    }

    // handle Site event
    public void handleSiteEvent(Event e) {
        int i = e.index; // the index of the site event

        double y = e.p.y(); // the y-cooridnate of the site event
        ycurr = y; // update the current state of the sweepline

        // check if tree is empty and insert
        if (tree.isEmpty()) {
            tree.root = tree.insert(tree.root, i, y);
        }
        else {
            Node node_above = tree.get(tree.root, i, y); // get the arc above the new point


            // create a vornoi edge out of the prependicular bisector
            int j = node_above.point;
            double[] ar = perpendicular_bisector(xc[i], yc[i], xc[j], yc[j]);
            Voronoi_edge vor_edge = new Voronoi_edge(ar[0], ar[1]);

            // detect the case if the first two points on the event queue have the same y-coordinate
            if ((tree.size == 1) && (yc[i] == yc[j])) {
                vor_edge.upper_edge = true;
                vor_edge.vertical = true;
                first_vertical = vor_edge;
            }

            // detect the case if the new voronoi edge is vetical
            if (ar[2] == 1) {
                vor_edge.vertical = true;

            }


            // invalidate the circle event of the above arc
            if (node_above.event != null) {
                node_above.event.valid = false;
            }


            tree.root = tree.insert(tree.root, i, y); // insert the new arc


            // check if the size is 3 and handle a special if the first 2 sites have the same y-coordinate
            if (tree.size() == 3) {
                Node X = tree.most_recent;
                X.vor_edge = vor_edge;

            }

            // handle the other cases
            else {
                Node current = tree.most_recent; // get the most recent
                Node next_1 = current.next;
                Node next_2 = next_1.next;
                Node prev_1 = current.prev;
                Node prev_2 = prev_1.prev;

                // set references to the voronoi edge these break points trace out
                Node X = current.left_intersection;
                Node Y = current.right_intersection;
                X.vor_edge = vor_edge;
                Y.vor_edge = vor_edge;

                // handle potential circle events that may occur
                if (next_2 != null) {
                    Node A = current.right_intersection;
                    Node B = next_1.right_intersection;

                    int a = current.point;
                    int b = next_1.point;
                    int c = next_2.point;

                    if (is_convergent(A, B, xc, yc, y)) {
                        double y_lowest = lowest_yc(a, b, c, xc, yc);
                        Event newevent = new Event(y_lowest, circle(a, b, c, xc, yc)[1], next_1);
                        next_1.event = newevent;
                        newevent.circle_event = true;
                        pq.insert(newevent);

                    }

                }
                if (prev_2 != null) {
                    Node A = prev_2.right_intersection;
                    Node B = prev_1.right_intersection;
                    int a = prev_2.point;
                    int b = prev_1.point;
                    int c = current.point;

                    if (is_convergent(A, B, xc, yc, y)) {
                        double y_lowest = lowest_yc(a, b, c, xc, yc);
                        Event newevent = new Event(y_lowest, circle(a, b, c, xc, yc)[1], prev_1);
                        prev_1.event = newevent;
                        newevent.circle_event = true;
                        pq.insert(newevent);


                    }
                }
            }


            // update the DCEL with new half-edges and a voronoi edge
            Half_Edge half_edge_1 = new Half_Edge(ar[0], ar[1]);
            Half_Edge half_edge_2 = new Half_Edge(ar[0], ar[1]);


            // set up twin references
            half_edge_1.twin = half_edge_2;
            half_edge_2.twin = half_edge_1;

            // set up incidences with the faces
            half_edge_1.incidentFace = vor_faces[i];
            half_edge_2.incidentFace = vor_faces[j];

            // set up references to the half-edges
            vor_edge.edge_one = half_edge_1;
            vor_edge.edge_two = half_edge_2;

            // set up a ref from the face to the half edge
            vor_faces[i].half_edge = half_edge_1;
            vor_faces[j].half_edge = half_edge_2;


            // add the new half-edges
            vor_half_edges.add(half_edge_1);
            vor_half_edges.add(half_edge_2);

            // add the voronoi edge to the DCEL
            vor_edges.add(vor_edge);


        }
    }


    // handle a circle event
    public void handleCircleEvent(Event e) {
        double y = e.p.y(); // get the position of the sweepline

        ycurr = y; // update its current position

        // get the site nodes correposing to the circle event
        Node current = e.node;
        Node prev = current.prev;
        Node next = current.next;


        // get the two voronoi edges that will converge at a voronoi vertex
        Node X = current.left_intersection;
        Node Y = current.right_intersection;
        Voronoi_edge vor_edge_left = X.vor_edge;
        Voronoi_edge vor_edge_right = Y.vor_edge;


        // create the voronoi vertex by intesecting the voronoi edges
        double[] ar = intersection_point(vor_edge_left, vor_edge_right);
        Vertex vertex = new Vertex(ar[0], ar[1]);


        // get the 3 sites that surround the vertex
        int index = current.point;
        int index1 = prev.point;
        int index2 = next.point;


        // get the existing half_edges
        Half_Edge one_ = vor_edge_left.get_half_edge(index);
        Half_Edge two_ = vor_edge_right.get_half_edge(index);
        Half_Edge three_ = vor_edge_right.get_half_edge(index2);
        Half_Edge six_ = vor_edge_left.get_half_edge(index1);


        // create the new voronoi edge that starts to be traced out (the bisector of sites index1 and index2)
        double[] arr = perpendicular_bisector(xc[index1], yc[index1], xc[index2], yc[index2]);
        Voronoi_edge vor_edge = new Voronoi_edge(arr[0], arr[1]);


        // check if the edge if vertical and handle the case
        if (arr[2] == 1) {

            vor_edge.vertical = true;

        }


        // create the new 2 half-edges for the new voronoi edge
        Half_Edge four_ = new Half_Edge(arr[0], arr[1]);
        Half_Edge five_ = new Half_Edge(arr[0], arr[1]);

        // update face refrences
        four_.incidentFace = vor_faces[index2];
        five_.incidentFace = vor_faces[index1];

        // update twin references
        five_.twin = four_;
        four_.twin = five_;

        // update vor edge refrences
        vor_edge.edge_one = four_;
        vor_edge.edge_two = five_;


        // update the half-edges prev and next references
        one_.next = two_;
        two_.prev = one_;
        one_.destination = vertex;
        two_.origin = vertex;

        four_.prev = three_;
        three_.next = four_;
        four_.origin = vertex;
        three_.destination = vertex;

        six_.prev = five_;
        five_.next = six_;
        six_.origin = vertex;
        five_.destination = vertex;

        // update reference from a vertex to a half-edge
        vertex.half_edge = two_;

        // update the DCEL

        // add the new half-edge
        vor_half_edges.add(four_);
        vor_half_edges.add(five_);

        // add the new vor edge
        vor_edges.add(vor_edge);

        // add the new voronoi vertex
        vor_vertices.add(vertex);


        // invalidate possible circle event events
        if (current.event != null) {
            current.event.valid = false;
        }
        if (prev.event != null) {
            prev.event.valid = false;
        }
        if (next.event != null) {
            next.event.valid = false;
        }

        tree.delete(
                current); // delete the node corresponding to the disappearing arc on the beach-line

        Node c_node = tree.most_recent; // get a reference to the most recent node


        c_node.right_intersection.vor_edge = vor_edge; // set the refrence to the new vor edge

        // check for potential new circle events
        Node next_n = c_node.next;
        Node next_n_1 = next_n.next;
        Node prev_n = c_node.prev;


        if (prev_n != null) {
            Node A = prev_n.right_intersection;
            Node B = c_node.right_intersection;
            int a = prev_n.point;
            int b = c_node.point;
            int c = next_n.point;

            if (is_convergent(A, B, xc, yc, y)) {
                double y_lowest = lowest_yc(a, b, c, xc, yc);
                Event newevent = new Event(y_lowest, circle(a, b, c, xc, yc)[1], c_node);
                c_node.event = newevent;
                newevent.circle_event = true;
                pq.insert(newevent);
            }
        }
        if (next_n_1 != null) {
            Node A = c_node.right_intersection;
            Node B = next_n.right_intersection;
            int a = c_node.point;
            int b = next_n.point;
            int c = next_n_1.point;

            if (is_convergent(A, B, xc, yc, y)) {
                double y_lowest = lowest_yc(a, b, c, xc, yc);
                Event newevent = new Event(y_lowest, circle(a, b, c, xc, yc)[1], next_n);
                next_n.event = newevent;
                newevent.circle_event = true;
                pq.insert(newevent);


            }
        }

    }


    // return the y-coordinate of the midpoint of two points
    public double mid_point_y(int i, int j, double[] xc, double[] yc) {
        return (yc[i] + yc[j]) / 2;
    }

    // compute the sqaured distance between two points
    public double squared_distance(double xa, double ya, double xb, double yb) {
        return (xa - xb) * (xa - xb) + (ya - yb) * (ya - yb);
    }

    // compute the squared distance between Vertex a and Vertex b
    public double squared_distance(Vertex a, Vertex b) {
        return (a.xc - b.xc) * (a.xc - b.xc) + (a.yc - b.yc) * (a.yc - b.yc);
    }

    // compute the perpendicular bisector of two points
    public double[] perpendicular_bisector(double x1, double y1, double x2, double y2) {
        if (y1 == y2) {
            double[] a = new double[3];
            a[0] = (x1 + x2) / 2;
            a[2] = 1;
            return a;
        }

        double m = -(x2 - x1) / (y2 - y1);
        double b = (y1 + y2) / 2 - m * (x1 + x2) / 2;
        double[] ar = new double[3];
        ar[0] = m;
        ar[1] = b;
        ar[2] = 0;
        return ar;
    }


    // compute the intersection point of two lines with slopes m1, m2 and biases b1, b2 resp.
    public double[] intersection_point(double m1, double b1, double m2, double b2) {
        double[] a = new double[2];
        a[0] = (b2 - b1) / (m1 - m2);
        a[1] = m1 * a[0] + b1;
        return a;
    }


    // compute the intersection point of two voronoi edges
    public double[] intersection_point(Voronoi_edge e1, Voronoi_edge e2) {
        double[] a = new double[2];
        if (e1.vertical) {
            double m1 = e1.m;

            double m2 = e2.m;
            double b2 = e2.b;

            a[0] = m1;
            a[1] = m2 * m1 + b2;

        }
        if (e2.vertical) {
            double m2 = e2.m;

            double m1 = e1.m;
            double b1 = e1.b;

            a[0] = m2;
            a[1] = m1 * m2 + b1;

        }
        if (!e1.vertical && !e2.vertical) {

            a = intersection_point(e1.m, e1.b, e2.m, e2.b);

        }

        return a;


    }

    // does x lie in [a,b]
    public boolean lies_in(double x, double a, double b) {
        return (x >= a) && (x <= b);

    }


    // compute the intersection of a line with a bounding box
    // the first one is the lower one
    public double[] intersection_box(double x1, double x2, double y1, double y2,
                                     Voronoi_edge edge) {
        double[] ar = new double[4];

        if (edge.vertical) {
            ar[0] = edge.m;
            ar[1] = y1;
            ar[2] = edge.m;
            ar[3] = y2;

        }
        else {
            double m = edge.m;
            double b = edge.b;


            double i_y1 = (y1 - b) / m;
            double i_y2 = (y2 - b) / m;
            double i_x1 = (m * x1 + b);
            double i_x2 = m * x2 + b;

            if (lies_in(i_y1, x1, x2) && lies_in(i_y2, x1, x2)) {

                ar[0] = i_y1;
                ar[1] = y1;
                ar[2] = i_y2;
                ar[3] = y2;
            }
            if (lies_in(i_y1, x1, x2) && lies_in(i_x1, y1, y2)) {
                ar[0] = i_y1;
                ar[1] = y1;
                ar[2] = x1;
                ar[3] = i_x1;
            }
            if (lies_in(i_y1, x1, x2) && lies_in(i_x2, y1, y2)) {
                ar[0] = i_y1;
                ar[1] = y1;
                ar[2] = x2;
                ar[3] = i_x2;
            }

            if (lies_in(i_y2, x1, x2) && lies_in(i_x1, y1, y2)) {
                ar[0] = x1;
                ar[1] = i_x1;
                ar[2] = i_y2;
                ar[3] = y2;
            }
            if (lies_in(i_y2, x1, x2) && lies_in(i_x2, y1, y2)) {
                ar[0] = x2;
                ar[1] = i_x2;
                ar[2] = i_y2;
                ar[3] = y2;

            }
            if (lies_in(i_x2, y1, y2) && lies_in(i_x1, y1, y2)) {

                if (i_x1 > i_x2) {
                    ar[0] = x2;
                    ar[1] = i_x2;
                    ar[2] = x1;
                    ar[3] = i_x1;

                }
                else {
                    ar[0] = x1;
                    ar[1] = i_x1;
                    ar[2] = x2;
                    ar[3] = i_x2;


                }

            }
        }

        return ar;
    }

    // ccw function
    public static int ccw(Vertex a, Vertex b, Vertex c) {
        double xa = a.xc;
        double ya = a.yc;
        double xb = b.xc;
        double yb = b.yc;
        double xc = c.xc;
        double yc = c.yc;


        double det = -(xa * (yb - yc) + xb * (yc - ya) + xc * (ya - yb));
        if (det != 0) {
            if (det < 0) return -1;
            else return 1;
        }
        else {
            if (((xb - xa) * (xc - xa) < 0) || ((yb - ya) * (yc - ya) < 0)) {
                return -2;
            }
            else {

                if (Math.abs(xb - xa) + Math.abs(yb - ya) >= Math.abs(xc - xa) + Math
                        .abs(yc - ya)) {
                    return 0;
                }
                else return 2;

            }
        }
    }


    // do the voronoi edges of A and B converge
    public boolean is_convergent(Node A, Node B, double[] xc, double[] yc, double y) {
        int a = A.left_point.point;
        int b = A.right_point.point;
        int c = B.right_point.point;

        double x1 = xc[a];
        double y1 = yc[a];
        double x2 = xc[b];
        double y2 = yc[b];
        double x3 = xc[c];
        double y3 = yc[c];
        Vertex a_ = new Vertex(x1, y1);
        Vertex b_ = new Vertex(x2, y2);
        Vertex c_ = new Vertex(x3, y3);

        return ccw(c_, b_, a_) == -1;
    }

    // get the center and radius of a circle through 3 points
    public double[] circle(int a, int b, int c, double[] xc, double[] yc) {
        double x1 = xc[a];
        double y1 = yc[a];
        double x2 = xc[b];
        double y2 = yc[b];
        double x3 = xc[c];
        double y3 = yc[c];

        double x12 = x1 - x2;
        double x13 = x1 - x3;


        double y12 = y1 - y2;
        double y13 = y1 - y3;

        double y31 = y3 - y1;
        double y21 = y2 - y1;

        double x31 = x3 - x1;
        double x21 = x2 - x1;

        // x1^2 - x3^2
        double sx13 = (x1 * x1 -
                x3 * x3);

        // y1^2 - y3^2
        double sy13 = (y1 * y1 -
                y3 * y3);

        double sx21 = (x2 * x2 -
                x1 * x1);

        double sy21 = (y2 * y2 -
                y1 * y1);

        double f = ((sx13) * (x12)
                + (sy13) * (x12)
                + (sx21) * (x13)
                + (sy21) * (x13))
                / (2 * ((y31) * (x12) - (y21) * (x13)));
        double g = ((sx13) * (y12)
                + (sy13) * (y12)
                + (sx21) * (y13)
                + (sy21) * (y13))
                / (2 * ((x31) * (y12) - (x21) * (y13)));

        double p = -Math.pow(x1, 2) - Math.pow(y1, 2) -
                2 * g * x1 - 2 * f * y1;

        // eqn of circle be x^2 + y^2 + 2*g*x + 2*f*y + c = 0
        // where centre is (h = -g, k = -f) and radius r
        // as r^2 = h^2 + k^2 - c
        double h = -g;
        double k = -f;
        double sqr_of_r = h * h + k * k - p;

        // r is the radius
        double r = Math.sqrt(sqr_of_r);

        double[] ar = new double[3];
        ar[0] = r;
        ar[1] = h;
        ar[2] = k;

        return ar;
    }

    // get the lowest y-coordinate on a circle through 3 points
    public double lowest_yc(int a, int b, int c, double[] xc, double[] yc) {
        double x1 = xc[a];
        double y1 = yc[a];
        double x2 = xc[b];
        double y2 = yc[b];
        double x3 = xc[c];
        double y3 = yc[c];

        double x12 = x1 - x2;
        double x13 = x1 - x3;


        double y12 = y1 - y2;
        double y13 = y1 - y3;

        double y31 = y3 - y1;
        double y21 = y2 - y1;

        double x31 = x3 - x1;
        double x21 = x2 - x1;

        // x1^2 - x3^2
        double sx13 = (x1 * x1 -
                x3 * x3);

        // y1^2 - y3^2
        double sy13 = (y1 * y1 -
                y3 * y3);

        double sx21 = (x2 * x2 -
                x1 * x1);

        double sy21 = (y2 * y2 -
                y1 * y1);

        double f = ((sx13) * (x12)
                + (sy13) * (x12)
                + (sx21) * (x13)
                + (sy21) * (x13))
                / (2 * ((y31) * (x12) - (y21) * (x13)));
        double g = ((sx13) * (y12)
                + (sy13) * (y12)
                + (sx21) * (y13)
                + (sy21) * (y13))
                / (2 * ((x31) * (y12) - (x21) * (y13)));

        double p = -Math.pow(x1, 2) - Math.pow(y1, 2) -
                2 * g * x1 - 2 * f * y1;

        // eqn of circle be x^2 + y^2 + 2*g*x + 2*f*y + c = 0
        // where centre is (h = -g, k = -f) and radius r
        // as r^2 = h^2 + k^2 - c
        double h = -g;
        double k = -f;
        double sqr_of_r = h * h + k * k - p;

        // r is the radius
        double r = Math.sqrt(sqr_of_r);
        return k - r;


    }


    // unit testing
    public static void main(String[] args) {

        // 1. Random input
        int N = 50; // select the number of points N

        // create the point arrays
        double[] xc = new double[N];
        double[] yc = new double[N];

        // generate random N points in the unit sqaure
        for (int i = 0; i < N; i++) {
            xc[i] = StdRandom.uniform();
            yc[i] = StdRandom.uniform();
        }

        // // 2. Standard input
        // Scanner in = new Scanner(System.in);
        // int N = in.nextInt(); // read N from StdIn
        // double[] xc = new double[N];
        // double[] yc = new double[N];
        //
        // // read the x-coordinates
        // for (int i = 0; i < N; i++) {
        //     xc[i] = in.nextDouble();
        //
        // }
        //
        // // read the y-coordinates
        // for (int i = 0; i < N; i++) {
        //     yc[i] = in.nextDouble();
        // }


        // 3. Custom input, wirte the input yourself
        // int N = 5;
        // double[] xc = new double[N];
        // double[] yc = new double[N];
        // xc[0] = 1;
        // yc[0] = 0;
        //
        // xc[1] = 0;
        // yc[1] = 1;
        //
        //
        // xc[2] = 0;
        // yc[2] = -1;
        //
        // xc[3] = -1;
        // yc[3] = 0;
        //
        //
        // xc[4] = 0;
        // yc[4] = -2;


        Voronoi_diagram voronoi_diag = new Voronoi_diagram(N, xc,
                                                           yc); // create the voronoi diagram of threse points

        // get the DCEL of the diagram
        ArrayList<Half_Edge> vor_half_edges = voronoi_diag.vor_half_edges;
        ArrayList<Vertex> vor_vertices = voronoi_diag.vor_vertices;
        ArrayList<Voronoi_edge> vor_edges = voronoi_diag.vor_edges;

        // find the bounding box parameters
        double x0 = voronoi_diag.x0;
        double x1 = voronoi_diag.x1;
        double y0 = voronoi_diag.y0;
        double y1 = voronoi_diag.y1;


        // select a bounding box that fits only the sites
        double X0 = xc[0];
        double X1 = xc[1];
        double Y0 = yc[0];
        double Y1 = yc[0];

        for (int i = 0; i < N; i++) {
            if (xc[i] < X0) X0 = xc[i];
            if (xc[i] > X1) X1 = xc[i];
            if (yc[i] < Y0) Y0 = yc[i];
            if (yc[i] > Y1) Y1 = yc[i];

        }

        // here is where you choose the scaling of the drawing


        // uncomment below to see the diagram with the big bounding box
        StdDraw.setXscale(x0 - 0.05, x1 + 0.15);
        StdDraw.setYscale(y0 - 0.15, y1 + 0.05);
        // StdDraw.text(x1 - 0.09, y0 - 0.1, "N = " + N);


        // uncommpent below to see the diagram more clearly with a zoom, that shows only the sites (recommended for big N, as the bounding box gets much bigger than the bounding box of the sites)
        // StdDraw.setXscale(X0 - 0.1, X1 + 0.1);
        // StdDraw.setYscale(Y0 - 0.1, Y1 + 0.1);
        // StdDraw.text(X1 - 0.09, Y0 - 0.01, "N = " + N);


        // Draw all sites in blue
        for (int i = 0; i < N; i++) {
            StdDraw.setPenColor(Color.blue);
            StdDraw.filledCircle(xc[i], yc[i], 0.007); // DRAW
            // System.out.println(yc[i]);

        }


        // draw all edges in green
        for (int i = 0; i < vor_half_edges.size(); i = i + 1) {
            Half_Edge edge = vor_half_edges.get(i);

            StdDraw.setPenColor(Color.green);
            StdDraw.line(edge.origin.xc, edge.origin.yc,
                         edge.destination.xc,
                         edge.destination.yc);

            StdDraw.setPenColor(Color.red);
            StdDraw.filledCircle(edge.origin.xc, edge.origin.yc, 0.007);
            StdDraw.filledCircle(edge.destination.xc, edge.destination.yc, 0.007);


        }
    }
}


