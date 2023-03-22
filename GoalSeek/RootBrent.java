package com.vmetrix.v3.custom.vcalc.math.rootfind;
/*
    b   : is the best zero so far, f(b) is the smallest value of f(x)
    c   : is the previous value of b, so b and c produce the secant
    a   : and b bracket the sign change, so a and b provide the midpoint

    4 possible steps
    - If a <> c, try IQI
    - If a == c, try SECANT
    - If the interpolation step is near to endpoint, or outside the interval, use bisection
    - If the step is smaller than the tolerance, use the tolerance.
 */

import java.util.Collections;

public class RootBrent {

    public static final int MAX_ITER = 100;
    public static final double ZERO = Math.ulp(1.0);
    public static final double ACCURACY = 1e-15;

    public double getRoot(GoalSeek problem) {
        double a = problem.getBeginRange();
        double b = problem.getEndRange();

        double fa = problem.function(a);
        double fb = problem.function(b);

        if (fa * fb > 0) {
            throw new RuntimeException("a = " + a + " and b = " + b + " do not bracket a root");
        }

        if (Math.abs(fa) <= ZERO) {
            return a;
        }
        if (Math.abs(fb) <= ZERO) {
            return b;
        }

        double c = a;
        double fc = fa;

        double aux = a;
        double faux = fa;

        double d = b - c;
        double e = d;

        double m, p, q, tol, s, r;
        String method = "";

        for (int i = 3; i <= MAX_ITER; i++) {
            // [a, b] always contains the zero.
            if (Math.signum(fa) == Math.signum(fb)) {
                a = c;
                fa = fc;
                d = b - c;
                e = d;
            }

            // f(b) should be the smallest value of f(x)
            if (Math.abs(fa) < Math.abs(fb)) {
                aux = a;
                faux = fa;

                a = b;
                c = b;
                b = aux;

                fa = fb;
                fc = fb;
                fb = faux;

            }

            // Convergence test and possible exit
            m = 0.5 * (a - b);
            tol = 2.0 * ACCURACY * Math.max(Math.abs(b), 1.0);
            if (Math.abs(m) <= tol || Math.abs(fb) <= ZERO) {
                return b;
            }

            // Chose Method
            if (Math.abs(e) < tol || Math.abs(fc) <= Math.abs(fb)) {
                // BISECTION
                d = m;
                e = m;
                method = "BISECTION";
            } else {
                s = fb / fc;
                if (a == c) {
                    // SECANT
                    p = 2.0 * m * s;
                    q = 1.0 - s;
                    method = "SECANT";
                } else {
                    // INVERSE QUADRATIC INTERPOLATION
                    q = fc / fa;
                    r = fb / fa;
                    p = s * (2.0 * m * q * (q - r) - (b - c) * (r - 1.0));
                    q = (q - 1.0) * (r - 1.0) * (s - 1.0);
                    method = "IQI";
                }
                if (p > 0) {
                    q = -q;
                } else {
                    p = -p;
                }
                // INTERPOLATION ACCEPTABLE
                if ((2.0 * p < 3.0 * m * q - Math.abs(tol * q)) && (p < Math.abs(0.5 * e * q))) {
                    e = d;
                    d = p / q;
                } else {
                    d = m;
                    e = m;
                }
            }
            // NEXT POINT
            c = b;
            fc = fb;
            if (Math.abs(d) > tol) {
                b = b + d;
            } else {
                b = b - Math.signum(b - a) * tol;
                method = "Minimal";
            }
            fb = problem.function(b);
        }

        return b;
    }

    public double getRootWithLog(GoalSeek problem) {
        double a = problem.getBeginRange();
        double b = problem.getEndRange();

        double fa = problem.function(a);
        double fb = problem.function(b);

        if (fa * fb > 0) {
            throw new RuntimeException("a = " + a + " and b = " + b + " do not bracket a root");
        }

        if (Math.abs(fa) <= ZERO) {
            return a;
        }
        if (Math.abs(fb) <= ZERO) {
            return b;
        }

        System.out.printf("%5s|%20s|%25s|%25s|%n", "eval ", "method ", "r ", "f(r) ");
        System.out.println("");

        System.out.printf("%5d|%20s|%25.15f|%25.15f|%n", 1, "Initial (a)", a, fa);
        System.out.printf("%5d|%20s|%25.15f|%25.15f|%n", 2, "Initial (b)", b, fb);

        double c = a;
        double fc = fa;

        double aux = a;
        double faux = fa;

        double d = b - c;
        double e = d;

        double m, p, q, tol, s, r;
        String method = "";

        for (int i = 3; i <= MAX_ITER; i++) {
            // [a, b] always contains the zero.
            if (Math.signum(fa) == Math.signum(fb)) {
                a = c;
                fa = fc;
                d = b - c;
                e = d;
            }

            // f(b) should be the smallest value of f(x)
            if (Math.abs(fa) < Math.abs(fb)) {
                aux = a;
                faux = fa;

                a = b;
                c = b;
                b = aux;

                fa = fb;
                fc = fb;
                fb = faux;

            }

            // Convergence test and possible exit
            m = 0.5 * (a - b);
            tol = 2.0 * ACCURACY * Math.max(Math.abs(b), 1.0);
            if (Math.abs(m) <= tol || Math.abs(fb) <= ZERO) {
                return b;
            }

            // Chose Method
            if (Math.abs(e) < tol || Math.abs(fc) <= Math.abs(fb)) {
                // BISECTION
                d = m;
                e = m;
                method = "BISECTION";
            } else {
                s = fb / fc;
                if (a == c) {
                    // SECANT
                    p = 2.0 * m * s;
                    q = 1.0 - s;
                    method = "SECANT";
                } else {
                    // INVERSE QUADRATIC INTERPOLATION
                    q = fc / fa;
                    r = fb / fa;
                    p = s * (2.0 * m * q * (q - r) - (b - c) * (r - 1.0));
                    q = (q - 1.0) * (r - 1.0) * (s - 1.0);
                    method = "IQI";
                }
                if (p > 0) {
                    q = -q;
                } else {
                    p = -p;
                }
                // INTERPOLATION ACCEPTABLE
                if ((2.0 * p < 3.0 * m * q - Math.abs(tol * q)) && (p < Math.abs(0.5 * e * q))) {
                    e = d;
                    d = p / q;
                } else {
                    d = m;
                    e = m;
                }
            }
            // NEXT POINT
            c = b;
            fc = fb;
            if (Math.abs(d) > tol) {
                b = b + d;
            } else {
                b = b - Math.signum(b - a) * tol;
                method = "Minimal";
            }
            fb = problem.function(b);
            System.out.printf("%5d|%20s|%25.15f|%25.15f|%n", i, method, b, fb);
        }

        return b;
    }

}

