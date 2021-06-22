import java.io.PrintWriter;

public class Node extends AbstractNode {
    String attName;
    AbstractNode left;
    AbstractNode right;

    public Node(String attName, AbstractNode left, AbstractNode right) {
        this.attName = attName;
        this.left = left;
        this.right = right;
    }

    public void report(String indent, PrintWriter writer) {
        writer.printf("%s%s = True:%n", indent, attName);
        left.report(indent + "\t", writer);
        writer.printf("%s%s = False:%n", indent, attName);
        right.report(indent + "\t", writer);
    }
}
