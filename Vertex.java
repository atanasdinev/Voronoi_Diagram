/* *****************************************************************************
 *  Name:    Atanas Dinev
 *  NetID:   adinev
 *  Course:  COS 451
 *
 *  Description:  A Vertex class to represent a vertex in the doubly-connected
 *  edge list. Supports a toString() representation method and an equals()
 *  method that checks if the vertex is the same as another vertex
 *
 **************************************************************************** */

public class Vertex {
    Half_Edge half_edge;
    double xc;
    double yc;

    public Vertex(double xc, double yc) {
        this.xc = xc;
        this.yc = yc;
    }

    @Override
    public String toString() {
        String s = xc + " , " + yc;
        return s;
    }

    public boolean equals(Vertex that) {
        if (that.xc == this.xc && that.yc == this.yc) return true;
        else return false;
    }


    public static void main(String[] args) {

    }
}
