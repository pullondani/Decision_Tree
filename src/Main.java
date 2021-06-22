import java.io.IOException;

public class Main {

    /**
     * Program to build a decision tree based on data supplied from Victoria University of Wellington.
     * @param args boolean - true, program runs with Cross Validation
     *
     */
    public static void main(String[] args) {
        try {
            if (args.length > 0 && args[0].equals("true")) {
                new DecisionTree("./data");
            } else {
                new DecisionTree("./data/hepatitis-training", "./data/hepatitis-test");
            }
        } catch (IOException e) {
            throw new RuntimeException("The file writer threw an exception");
        }
    }
}
