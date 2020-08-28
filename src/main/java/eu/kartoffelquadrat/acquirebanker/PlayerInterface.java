/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.kartoffelquadrat.acquirebanker;

/**
 * boolean returns are for shell repetition until the players input was legal
 *
 * @author maex
 */
public interface PlayerInterface {

    //tells the funds of a player
    public int getBalance();

    //changes a desired even number of a players stocks into a dofferent type of stocks
    //this is a tunnel method which only calls the players sharecollections method
    public void convert(int sourceIndex, int targetIndex, int sourceNumber);

    //sells a players shares of specified type and number
    public void sell(int sourceIndex, int number, int shareValue);

    //buys shares
    public void buy(int targetIndex, int desiredNumber, int shareValue);

    public String getName();

    //computesd the totan nuber of shares a playes ownes
    public int getNumberOfShares();

    //returns the number of shares a player ownes for a specific company
    public int getNumberOfShares(int index);

    //a player shall get one share for free in case he funds a company
    public String takeForFree(int index, int availableShares);

    //lay piece
    //public boolean layPiece(char xPos, int yPos);

    public boolean equals(PlayerInterface other);

    public void takeBoni(int amount);

    public void sellAllShares(int[] values, boolean[] activeCompanies);

    public String toString();
}
