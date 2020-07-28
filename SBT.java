/* *****************************************************************************
 *  Name:    Atanas Dinev
 *  NetID:   adinev
 *  Course:  COS 451
 *
 *  Description:  Implements a self-balancing tree to represent the beachline
 *  and the breakpoints. The balancing operations are the same as in an AVL
 *  tree.
 *
 **************************************************************************** */

public class SBT {
    Node root; // the root of the tree
    int size; // the size of the tree

    // the most recent node an operation has been done to, added just for
    // convenience for later
    Node most_recent;

    // the x- and y- coordinates of the sites
    double[] xc;
    double[] yc;

    // initialize the self-balancing tree
    public SBT(double[] xc, double[] yc) {
        this.xc = xc;
        this.yc = yc;
        root = null;
        size = 0;

    }

    // max function, return the max of a and b
    public int max(int a, int b) {
        if (a > b) return a;
        else return b;
    }

    // is the tree empty
    public boolean isEmpty() {
        return size == 0;
    }

    // the height of the tree
    public int height(Node N) {
        if (N == null)
            return 0;
        else return N.height;
    }

    // a unitility function to rigth rotate subtree rooted with y
    public Node rightRotate(Node t) {
        Node x = t.left;
        Node T2 = x.right;


        // Perform rotation
        // adjust the parent pointers
        if (T2 == null) {
            System.out.println(t.right.point);
            System.out.println(t.left.point);
        }

        T2.parent = t;
        T2.parent_orientation = false;

        Node a = t.parent;
        boolean o = t.parent_orientation;
        x.parent = a;
        x.parent_orientation = o;
        if (a != null) {
            if (o) {
                a.right = x;
            }
            else {
                a.left = x;
            }
        }

        t.parent = x;
        t.parent_orientation = true;


        x.right = t;
        t.left = T2;


        // Update heights
        t.height = max(height(t.left), height(t.right)) + 1;
        x.height = max(height(x.left), height(x.right)) + 1;

        // return new node
        return x;


    }

    // a utility function to left rotate subtree rooted with x
    public Node leftRotate(Node x) {
        Node t = x.right;
        Node T2 = t.left;

        // perform roation
        // set pointers to parents and orientation
        T2.parent = x;
        T2.parent_orientation = true;

        Node a = x.parent;
        boolean o = x.parent_orientation;
        t.parent = a;
        t.parent_orientation = o;
        if (a != null) {
            if (o) {
                a.right = t;
            }
            else {
                a.left = t;
            }
        }

        x.parent = t;
        x.parent_orientation = false;

        t.left = x;
        x.right = T2;


        // update heigths
        x.height = max(height(x.left), height(x.right)) + 1;
        t.height = max(height(t.left), height(t.right)) + 1;

        // return new root
        return t;

    }

    // get the balance
    public int getBalance(Node N) {
        if (N == null)
            return 0;
        return height(N.left) - height(N.right);

    }

    // get the size of the tree
    public int size() {
        return size;
    }


    // get the arc right above the point with index i in the arrays xc, and yc, with a current position of the sweepline y
    public Node get(Node node, int i, double y) {

        // if node is internal we have found it
        if (!node.internal) {
            return node;
        }

        // else do standard binary search in the tree
        else {

            double xc_int = node.xc_int(y);


            // perform bst search
            if (xc[i] < xc_int) {
                return get(node.left, i, y);
            }
            else {
                return get(node.right, i, y);
            }

        }
    }


    // insert a new node with index i in the arrays xc and yc, given a position y of the sweep line
    public Node insert(Node node, int i, double y) {
        // check if the tree is empty, if so create a new node
        if (node == null) {
            Node newnode = new Node();
            newnode.point = i;
            newnode.internal = false;
            size = size + 1;
            return newnode;
        }

        // if we go the leaf we need, then create a new smaller subtree instead of the leaf to represent the newly appeared arc on the beachline
        // reset pointers
        if (!node.internal) {
            // handle case if the first two points have the same y-coordinate
            if (yc[node.point] == yc[i]) {
                Node a = new Node();
                Node b = new Node();
                Node c = new Node();

                a.left = b;
                a.right = c;
                b.parent = a;
                c.parent = a;
                b.parent_orientation = false;
                c.parent_orientation = true;
                a.internal = true;
                b.internal = false;
                c.internal = false;

                if (xc[node.point] > xc[i]) {
                    b.point = i;
                    c.point = node.point;
                    b.right_intersection = a;
                    c.left_intersection = a;
                    a.left_point = b;
                    a.right_point = c;
                    a.height = 2;
                    b.height = 1;
                    c.height = 1;
                    b.xc = xc[i];
                    b.yc = yc[i];
                    c.xc = xc[node.point];
                    c.yc = yc[node.point];
                    b.next = c;
                    c.prev = b;

                }
                else {
                    c.point = i;
                    b.point = node.point;
                    c.right_intersection = a;
                    b.left_intersection = a;
                    a.left_point = c;
                    a.right_point = b;
                    a.height = 2;
                    b.height = 1;
                    c.height = 1;
                    c.xc = xc[i];
                    c.yc = yc[i];
                    b.xc = xc[node.point];
                    b.yc = yc[node.point];

                    c.next = b;
                    b.prev = c;
                }
                size = size + 2;
                most_recent = a;
                return a;

            }
            else {
                Node a = new Node();
                Node b = new Node();
                Node c = new Node();
                Node d = new Node();
                Node e = new Node();

                Node prev_n = node.prev;
                Node next_n = node.next;
                Node x = node.parent;
                if (x != null) {
                    if (node.parent_orientation) {
                        x.right = a;
                    }
                    else {
                        x.left = a;
                    }
                }
                // set pointers for a
                a.parent = node.parent;
                a.parent_orientation = node.parent_orientation;
                a.left_point = b;
                a.right_point = d;
                a.internal = true;
                a.left = b;
                a.right = c;
                a.height = 3;

                // set pointers for b
                b.internal = false;
                b.point = node.point;
                b.xc = xc[node.point];
                b.yc = yc[node.point];
                b.prev = prev_n;
                if (prev_n != null) {
                    prev_n.next = b;
                }
                b.next = d;
                b.parent = a;
                b.parent_orientation = false;
                b.left_intersection = node.left_intersection;
                b.right_intersection = a;

                // set pointers for c
                c.parent = a;
                c.parent_orientation = true;
                c.internal = true;
                c.left = d;
                c.right = e;
                c.left_point = d;
                c.right_point = e;
                c.height = 2;

                // set pointers for d
                d.parent = c;
                d.parent_orientation = false;
                d.point = i;
                d.xc = xc[i];
                d.yc = yc[i];
                d.internal = false;
                d.next = e;
                d.prev = b;
                d.left_intersection = a;
                d.right_intersection = c;

                // set pointers for e
                e.parent = c;
                e.parent_orientation = true;
                e.point = node.point;
                e.xc = xc[node.point];
                e.yc = yc[node.point];
                e.internal = false;
                e.next = next_n;
                if (next_n != null) {
                    next_n.prev = e;
                }
                e.prev = d;
                e.right_intersection = node.right_intersection;
                e.left_intersection = c;

                size = size + 4; // update size
                most_recent = d; // update most recent
                return a;
            }
        }

        // if node is internal do binary search by comparing the x cooridnate of the query point with the x-coordinates of the breakpoints
        // also balance the tree with rotations
        else {

            double xc_int = node.xc_int(y);


            // perform bst search
            if (xc[i] < xc_int) {
                node.left = insert(node.left, i, y);
            }
            else {
                node.right = insert(node.right, i, y);
            }


            // update the heigth
            node.height = 1 + max(height(node.left), height(node.right));


            // get the balance
            int balance = getBalance(node);

            // if this node becomes unbalanced then there are 4 cases


            // left left case
            if (balance > 1 && getBalance(node.left) >= 0) {
                return rightRotate(node);
            }

            // rigth rigth case
            if (balance < -1 && getBalance(node.right) <= 0) {
                return leftRotate(node);
            }

            // left rigth case
            if (balance > 1 && getBalance(node.left) < 0) {
                node.left = leftRotate(node.left);
                return rightRotate(node);

            }

            // rigth left case
            if (balance < -1 && getBalance(node.right) > 0) {
                node.right = rightRotate(node.right);
                return leftRotate(node);
            }


            // return the unchanged node pointer
            return node;


        }


    }


    // delete a node and update the pointers
    public void delete(Node node) {
        Node current = new Node();

        // delete the node and set the pointers accordingly

        // consider 2 cases for the orienation of the parent
        if (node.parent_orientation) {
            Node a = node.parent;
            Node b = a.left;
            Node c = a.parent;
            b.parent = c;
            if (a.parent_orientation) {
                c.right = b;
            }
            else {
                c.left = b;
            }
            b.parent_orientation = a.parent_orientation;
            Node prev_n = node.prev;
            Node next_n = node.next;
            if (prev_n != null) {
                prev_n.next = next_n;
            }
            if (next_n != null) {
                next_n.prev = prev_n;
            }
            Node intersection = node.right_intersection;
            if (intersection != null) {
                intersection.left_point = prev_n;
                prev_n.right_intersection = intersection;
                next_n.left_intersection = intersection;
            }

            most_recent = prev_n;
            current = b;
        }

        else {
            Node a = node.parent;
            Node b = a.right;
            Node c = a.parent;
            b.parent = c;
            if (a.parent_orientation) {
                c.right = b;
            }
            else {
                c.left = b;
            }
            b.parent_orientation = a.parent_orientation;
            Node prev_n = node.prev;
            Node next_n = node.next;
            prev_n.next = next_n;
            next_n.prev = prev_n;
            Node intersection = node.left_intersection;

            if (intersection != null) {
                intersection.right_point = next_n;
                prev_n.right_intersection = intersection;
                next_n.left_intersection = intersection;
            }

            most_recent = prev_n; // update most recent
            current = b;
        }
        size = size - 2; // update size

        // balance the tree by performing rotations
        while (current != null) {

            // update the height
            current.height = max(height(current.left), height(current.right)) + 1;

            // get the balance
            int balance = getBalance(current);

            // left left case
            if (balance > 1 && getBalance(current.left) >= 0) {
                current = rightRotate(current);
            }

            // left right case
            if (balance > 1 && getBalance(current.left) < 0) {
                current.left = leftRotate(current.left);
                current = rightRotate(current);
            }

            // right right case
            if (balance < -1 && getBalance(current.right) <= 0) {
                current = leftRotate(current);
            }

            // right left case
            if (balance < -1 && getBalance(current.right) > 0) {
                current.right = rightRotate(current.right);
                current = leftRotate(current);

            }

            if (current.parent == null) {
                root = current;

            }
            current = current.parent;
        }
    }

    // method to print the beachline of the self-balancing tree
    public void print_beachline() {
        Node current = root;
        while (current.left != null) {
            current = current.left;
        }
        Node t = current;
        while (t != null) {
            System.out.print(t.point + ", ");
            t = t.next;
        }
        System.out.println("\n");
    }


    // test cases
    public static void main(String[] args) {

        int N = 6;
        double[] xc = new double[N];
        double[] yc = new double[N];


        xc[0] = 0.2;
        yc[0] = 0.8;

        xc[1] = 0.6;
        yc[1] = 0.7;

        xc[2] = 0.3;
        yc[2] = 0.6;

        xc[3] = 0.5;
        yc[3] = 0.5;

        xc[4] = 0.4;
        yc[4] = 0.4;

        xc[5] = 0.1;
        yc[5] = 0.3;

        SBT tree = new SBT(xc, yc);

        tree.root = tree.insert(tree.root, 0, 0.8);
        tree.root = tree.insert(tree.root, 1, 0.7);
        tree.root = tree.insert(tree.root, 2, 0.6);
        Node p = tree.root.left.right.right;
        tree.delete(p);
        tree.root = tree.insert(tree.root, 3, 0.5);
        Node s = tree.root.right.left.left;
        tree.delete(s);
        tree.root = tree.insert(tree.root, 4, 0.4);
        Node k = tree.root.left.right;
        tree.delete(k);
        tree.root = tree.insert(tree.root, 5, 0.3);
        tree.delete(tree.root.left.left.right);
        tree.delete(tree.root.left.right.right);
        System.out.println(tree.root.right.right.xc_int(0.2));


    }
}
