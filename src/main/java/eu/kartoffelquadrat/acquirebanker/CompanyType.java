/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.kartoffelquadrat.acquirebanker;

/**
 * @author Max Schiedermeier
 */
public enum CompanyType {

    CHEAP(0),
    NORMAL(1),
    EXPENSIVE(2);

    private int shift;
    /*stores the market value of a company of type cheap in the followong steps
     * Pos -> Size
     * 0 -> 2
     * 1 -> 3
     * 2 -> 4
     * 3 -> 5
     * 4 -> 6-10
     * 5 -> 11-20
     * 6 -> 21-30
     * 7 -> 31-40
     * 8 -> 41 & over
     * 9 -> normal 41 & over
     * 10 -> expensive 41 & over
     */
    private int[] values =
            {
                    200,
                    300,
                    400,
                    500,
                    600,
                    700,
                    800,
                    900,
                    1000,
                    1100,
                    1200
            };

    //the number stores the value increase as an array position shift relative to the cheapest company type
    private CompanyType(int shift) {
        this.shift = shift;
    }

    public int getValueForSize(int size) {
        if (size < 2) {
            return 0;
        }

        if (size == 2) {
            return values[0 + shift];
        }

        if (size == 3) {
            return values[1 + shift];
        }

        if (size == 4) {
            return values[2 + shift];
        }

        if (size == 5) {
            return values[3 + shift];
        }

        if (size >= 6 && size <= 10) {
            return values[4 + shift];
        }

        if (size >= 11 && size <= 20) {
            return values[5 + shift];
        }

        if (size >= 21 && size <= 30) {
            return values[6 + shift];
        }

        if (size >= 31 && size <= 40) {
            return values[7 + shift];
        }

        return values[8 + shift];
    }
}
