package com.vmetrix.v3.custom.vcalc.math.rootfind;

public class Main {

    public static void main(String[] args) {

        GoalSeek problem = new GoalSeek(-5, 5);
        RootBrent solver = new RootBrent();

        double root = solver.getRoot(problem);

        System.out.println("Solution");
        System.out.println(root);
    }

}
