/* *****************************************************************************
 *  Name:    Atanas Dinev
 *  NetID:   adinev
 *  Course:  COS 451
 *
 *  Description:  Class that represents a face of the doubly connected edge
 *  list. Contains a reference to an incindent half-edge;
 *
 **************************************************************************** */

public class Face {
    Half_Edge half_edge;
    int index;

    // initialize the variables
    public Face() {
        this.half_edge = null;
        this.index = 0;
    }

    // unit testing
    public static void main(String[] args) {

    }
}
