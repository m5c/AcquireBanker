/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.kartoffelquadrat.acquirebanker;

/**
 * @author Max Schiedermeier
 */
public class ShareCollection implements ShareCollectionInterface {

    int[] shares = new int[7];
    private ShareCollectionInterface masterCollection;

    ShareCollection(ShareCollectionInterface masterCollection) {
        this.masterCollection = masterCollection;
    }

    public int getNumberOfSharesForIndex(int index) {
        return shares[index];
    }

    //these method must not be called for the games master collection
    public void increase(int index, int amount) {
        if (masterCollection == null) {
            throw new RuntimeException("Can't perform operation on master collection.");
        }

        //check availability in master shares
        if (masterCollection.getNumberOfSharesForIndex(index) < amount) {
            throw new RuntimeException("Not enough shares in stock");
        }

        //increase own
        shares[index] = shares[index] + amount;

        //decrease master
        masterCollection.decreaseMaster(index, amount);

    }

    //this method must be called, by a share collection, only!
    public void increaseMaster(int index, int amount) {
        if (masterCollection != null) {
            throw new RuntimeException("Can't perform operation on slave collection.");
        }

        shares[index] = shares[index] + amount;

        //check if operation exceeds bounds
        if (shares[index] > Main.initialSharesPerCompany) {
            throw new RuntimeException("Seems like the total number of shares increased during the game commence");
        }
    }

    //these method must not be called for the games master collection
    public void decrease(int index, int amount) {
        if (masterCollection == null) {
            throw new RuntimeException("Can't perform operation on master collection.");
        }

        //check availability in own shares
        if (getNumberOfSharesForIndex(index) < amount) {
            throw new RuntimeException("Ou do not own enough shares");
        }

        //decrease own
        shares[index] = shares[index] - amount;

        //increase master
        masterCollection.increaseMaster(index, amount);
    }

    //this method must be called, by a share collection, only!
    public void decreaseMaster(int index, int amount) {
        if (masterCollection != null) {
            throw new RuntimeException("Can't perform operation on slave collection.");
        }
        shares[index] = shares[index] - amount;

        if (shares[index] < 0) {
            throw new RuntimeException("No negative amoounts of shares allowed");
        }
    }

    public void convert(int sourceIndex, int targetIndex, int sourceNumber) {
        if (masterCollection == null) {
            throw new RuntimeException("Can't convert shares with own collection.");
        }

        if (masterCollection.getNumberOfSharesForIndex(targetIndex) < (sourceNumber / 2)) {
            throw new RuntimeException("Not enough shares in master collection");
        }

        if (shares[sourceIndex] < sourceNumber) {
            throw new RuntimeException("You can not convert more shares than you own.");
        }

        if (sourceNumber % 2 != 0) {
            throw new RuntimeException("You can convert even numbers of shares, only");
        }

        decrease(sourceIndex, sourceNumber);
        increase(targetIndex, sourceNumber / 2);
    }

    public int getTotal() {
        int totalAmount = 0;

        for (int i = 0; i < shares.length; i++) {
            totalAmount = totalAmount + shares[i];
        }

        return totalAmount;
    }
}
