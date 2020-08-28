/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.kartoffelquadrat.acquirebanker;

/**
 * @author maex
 */
public interface BoardInterface {

    //use this method to lay a tile on the board - throws exception if illegal
    //returns an enum which does allow the game to react apropriate
    public LayType getOperationToPerformForAddingPiece(int xPos, int yPos);

    //tells wether is is legal to lay a tile on a specified position
    public boolean isValidPosition(int xPos, int yPos);

    //adds a neutral company piece at position x,y
    public void setPositionToNeutral(int xPos, int yPos);

    //adds a piece of specified company at position x,y
    //tells the growth of the company
    public int setPositionToCompany(int xPos, int yPos, int companyIndex);

    //returns the size of the smaller company
    public int swallow(int greaterCompanyIndex, int smallerCompanyIndex);

    //tells whether a specified Position is blank
    public boolean isBlankPosition(int xPos, int yPos);

    public String toString();
}
