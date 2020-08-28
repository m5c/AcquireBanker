/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.kartoffelquadrat.acquirebanker;

/**
 * stores a collection of interfaces - can be used either for the remaining public accessible stocks or to
 * save/administrate players stock property
 *
 * @author maex
 */
public interface ShareCollectionInterface {

    //tells how many shares of the specified type are in possesion
    public int getNumberOfSharesForIndex(int index);

    //increases number of specified share index by specified number
    //relies on valid operation - throws exception otherwise
    public void increase(int index, int amount);

    //must be called by other share collections decrease, only!
    public void increaseMaster(int index, int amount);

    //reduces number of specified share index by specified number
    //throws exception if illegal operation (e.g. since not enough in posession)
    public void decrease(int index, int amount);

    //must be called by other share collections increase, only!
    public void decreaseMaster(int index, int amount);

    //changes 2 to 1 into desired stock - avoid convertion into own type
    public void convert(int sourceIndex, int targetIndex, int sourceNumber);

    //get total number of players shares
    public int getTotal();
}
