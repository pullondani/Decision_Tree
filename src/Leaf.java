import java.io.PrintWriter;

public class Leaf extends AbstractNode {
    public String className;
    public double prob;

    public Leaf(String className, double prob) {
        this.className = className;
        this.prob = prob;
    }

    public void report(String indent, PrintWriter writer) {
        if (prob == 0) { //Error-checking
            writer.printf("%sUnknown%n", indent);
        } else {
            writer.printf("%sClass %s, prob=%.2f%n", indent, className, prob);
        }
    }
}
