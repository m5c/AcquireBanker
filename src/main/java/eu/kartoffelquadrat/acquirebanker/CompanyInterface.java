/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.kartoffelquadrat.acquirebanker;

/**
 * Interface for the company class
 */
public interface CompanyInterface {

    //tells the value of one share of this company
    public int getShareValue();

    //tells the size of this company
    public int getSize();

    //tells wether the company is save to be swallowed by other companies
    public boolean isSave();

    //increase Size of company (either due to expansion or due to fusion)
    public void increaseCompany(int amount);

    //use this method to set the companys count to 0. Is only posible as long as the company is smaller then 11 tiles
    public void reset();

    //company name
    public String getName();

    //tells whether a companie is currently in the game (size 2 or greater)
    public boolean isActive();

    //tells you the colour scheeme
    public int getColour();
}
