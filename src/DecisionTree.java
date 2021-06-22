import java.io.File;
import java.util.*;
import java.io.*;

public class DecisionTree {
    public int numCategories;
    public int numAttrs;
    public List<String> categoryNames;
    public List<String> attNames;
    public List<Instance> allInstances;
    public AbstractNode tree;
    public int correct;
    public PrintWriter writer;

    public DecisionTree(String training, String test) throws IOException {
        readDataFile(training);

        writer = new PrintWriter(new FileWriter("sampledoutput.txt"));

        attNames.remove(0);
        this.tree = buildTree(allInstances, attNames);

        correct = 0;
        readDataFile(test);
        for (Instance instance : allInstances) {
            traverse(instance, tree);
        }

        tree.report("", writer);

        writer.println("***************");
        writer.println("CORRECT: " + correct + " / " + allInstances.size());
        writer.println("PERCENTAGE: " + (double) correct / (double) allInstances.size() * 100 + "%");
        writer.println("***************");

        writer.flush();
        writer.close();
    }

    public DecisionTree(String directoryPath) throws IOException {
        double total = 0;
        for (int i = 0; i < 10; i++) {
            DecisionTree p2 = new DecisionTree(directoryPath + "/hepatitis-training-run-" + i,
                    directoryPath + "/hepatitis-test-run-" + i);
            total += (double) p2.correct / (double) p2.allInstances.size();
        }

        writer = new PrintWriter(new FileWriter("sampledoutput.txt"));

        writer.println("******************");
        writer.println("CROSS VALIDATION");
        writer.print("AVERAGE PERCENTAGE OVER 10 RUNS: ");
        writer.println(total / 10);
        writer.println("******************");

        writer.flush();
        writer.close();
    }

    public AbstractNode buildTree(List<Instance> instances, List<String> attributes) {
        if (instances.isEmpty()) {
            return getMostProbable();
            // A leaf node that contains the name and probability of the most probable class across the whole training set
        } else if (checkClassesMatch(instances)) { // all belong to same class
            String cat = categoryNames.get(instances.get(0).getCategory());
            return new Leaf(cat, 1);
        } else if (attributes.isEmpty()) {
            // A leaf node that contains the name and probability of the majority class of instances
            return getMajorCat(instances);
        } else {
            double lowest = 1;
            String bestAtt = "";
            List<Instance> bestTrueInsts = new ArrayList<>();
            List<Instance> bestFalseInsts = new ArrayList<>();


            for (String att : attributes) {
                List<Instance> trueInsts = new ArrayList<>();
                List<Instance> falseInsts = new ArrayList<>();

                for (Instance instance : instances) {
                    int a = -1;
                    for (int aIn = 0; aIn < attNames.size(); aIn++) {
                        if (attNames.get(aIn).equals(att)) {
                            a = aIn;
                            break;
                        }
                    }

                    if (instance.getAtt(a)) trueInsts.add(instance);
                    else falseInsts.add(instance);
                }

                double currImpurity = calcImpurity(trueInsts, falseInsts);

                if (currImpurity < lowest) {
                    lowest = currImpurity;
                    bestAtt = att;
                    bestTrueInsts = trueInsts;
                    bestFalseInsts = falseInsts;
                }
            }

            List<String> newAtt = new ArrayList<>(attributes);
            newAtt.remove(bestAtt);
            AbstractNode left = buildTree(bestTrueInsts, newAtt);
            AbstractNode right = buildTree(bestFalseInsts, newAtt);

            return new Node(bestAtt, left, right);
        }
    }

    private Leaf getMostProbable() {
        return getMajorCat(allInstances);
    }

    private Leaf getMajorCat(List<Instance> instances) {
        int[] votes = new int[numCategories];
        int highest = 0;

        for (Instance instance : instances) {
            votes[instance.getCategory()]++;
            if (votes[instance.getCategory()] > highest) {
                highest = votes[instance.getCategory()];
            }
        }

        List<Integer> cats = new ArrayList<>();
        for (int i = 0; i < votes.length; i++) {
            if (votes[i] == highest)
                cats.add(i);
        }

        String cat = categoryNames.get((int) (Math.random() * cats.size()));
        double prob = (double) highest / (double) instances.size();

        return new Leaf(cat, prob);
    }

    public double calcImpurity(List<Instance> trueInst, List<Instance> falseInst) {
        int totalSize = trueInst.size() + falseInst.size();

        if (totalSize == 0) throw new RuntimeException();

        // inst = { golf, home, golf, golf }
        // size = 4
        // a = 3 / 4
        // b = 1 / 4
        // p = a * b

        double wTrue = (double) trueInst.size() / (double) totalSize;
        double wFalse = (double) falseInst.size() / (double) totalSize;

        return (calcProb(trueInst) * wTrue + calcProb(falseInst) * wFalse);
    }

    public double calcProb(List<Instance> instances) {
        if (instances.size() == 0) return 0; // FIXME SHOULD THIS BE POSSIBLE

        int[] votes = new int[numCategories];
        for (Instance inst : instances) {
            votes[inst.getCategory()]++;
        }
        double a = (double) votes[0] / (double) instances.size();
        double b = (double) votes[1] / (double) instances.size();

        return a * b;
    }

    public boolean checkClassesMatch(List<Instance> instances) {
        int cat = instances.get(0).getCategory();
        for (Instance instance : instances) {
            if (instance.getCategory() != cat)
                return false;
        }
        return true;
    }

    public void traverse(Instance instance, AbstractNode treeNode) {
        if (treeNode instanceof Node) {
            Node n = (Node) treeNode;
            traverse(instance, instance.getAtt(attNames.indexOf(n.attName) - 1) ? n.left : n.right);
        } else {
            Leaf l = (Leaf) treeNode;
            String cat = categoryNames.get(instance.getCategory());
            if (l.className.equals(cat)) {
                correct++;
            }
//            else {
//                System.out.println("EXPECTED: " + cat);
//                System.out.println("RESULT: " + l.className);
//            }
        }
    }

    /*
        Below is all boiler plate code for reading from the data files provided by
        Victoria University of Wellington.
    */

    private void readDataFile(String fname) {
        /* format of names file:
         * names of categories, separated by spaces
         * names of attributes
         * category followed by true's and false's for each instance
         */
//        System.out.println("Reading data from file " + fname);
        try {
            Scanner din = new Scanner(new File(fname));
            categoryNames = new ArrayList<>();
            attNames = new ArrayList<>();

            for (Scanner s = new Scanner(din.nextLine()); s.hasNext(); ) attNames.add(s.next());

            numAttrs = attNames.size();
            allInstances = readInstances(din);
            numCategories = categoryNames.size();
            din.close();
        } catch (IOException e) {
            throw new RuntimeException("Data File caused IO exception");
        }
    }


    private List<Instance> readInstances(Scanner scanner) {
        // Instance = classname and space separated attribute values
        List<Instance> instances = new ArrayList<>();
        while (scanner.hasNext()) {
            Scanner line = new Scanner(scanner.nextLine());
            String cat = line.next();
            if (!categoryNames.contains(cat) && !cat.equals("Class")) categoryNames.add(cat);
            instances.add(new Instance(categoryNames.indexOf(cat), line));
        }
        return instances;
    }

    private class Instance {
        private final int category;
        private final List<Boolean> vals;

        public Instance(int cat, Scanner s) {
            if (cat == -1) throw new RuntimeException("INVALID CATEGORY");
            category = cat;
            vals = new ArrayList<>();
            while (s.hasNextBoolean()) vals.add(s.nextBoolean());
        }

        public boolean getAtt(int index) {
            return vals.get(index);
        }

        public int getCategory() {
            return category;
        }

        public String toString() {
            StringBuilder ans = new StringBuilder(categoryNames.get(category));
            ans.append(" ");
            for (Boolean val : vals)
                ans.append(val ? "true  " : "false ");
            return ans.toString();
        }

    }
}
