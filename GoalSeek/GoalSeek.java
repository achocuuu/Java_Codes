package com.vmetrix.v3.custom.vcalc.math.rootfind;

public class GoalSeek {

    private final double beginRange;
    private final double endRange;

    public GoalSeek(double beginRange, double endRange) {
        this.beginRange = beginRange;
        this.endRange = endRange;
    }

    double function(double x){
        return Math.pow(x, 3) - 2*Math.pow(x, 2) - 5;
	
    }

    public double getBeginRange() {
        return beginRange;
    }

    public double getEndRange() {
        return endRange;
    }
}
