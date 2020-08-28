/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.kartoffelquadrat.acquirebanker;

/**
 * @author maex
 */
public enum LayType {
    UNDECLARED(), //1 new piece without any neighbours
    JOIN(), //joining multiple companies
    FUND(), //only one undeclared neighbour
    EXPAND(), //only one company neighbour
    ILLEGAL(); //something else - dunno if i realy need this one

    public int[] cc;

    public void setConcendesCompanies(int[] cc) {
        this.cc = cc;
    }

    public int[] getConcernedCompanies() {
        return cc;
    }
}
