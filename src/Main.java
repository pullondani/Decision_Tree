import java.io.IOException;

public class Main {

    /**
     * @param args 2 arguments for hepatitis training and test,
     *             1 argument for the DIRECTORY of where the CROSS VALIDATION files are
     */
    public static void main(String[] args) {
        try {
            if (args.length > 1) {
                new DecisionTree(args[0], args[1]);
            } else {
                new DecisionTree(args[0]);
            }
        } catch (IOException e) {
            throw new RuntimeException("The file writer threw an exception");
        }
    }
}
