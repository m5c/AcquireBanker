/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.kartoffelquadrat.acquirebanker;

/**
 * implements a physical player
 */
public class Player implements PlayerInterface {

    String name;
    int balance;
    ShareCollectionInterface ownedShares;

    Player(String name, ShareCollectionInterface masterCollection) {
        this.name = name;
        balance = 6000;
        ownedShares = new ShareCollection(masterCollection);
    }

    public int getBalance() {
        return balance;
    }

    public void convert(int sourceIndex, int targetIndex, int sourceNumber) {
        ownedShares.convert(sourceIndex, targetIndex, sourceNumber);
    }

    public void sell(int sourceIndex, int number, int shareValue) {
        if (ownedShares.getNumberOfSharesForIndex(sourceIndex) < number) {
            throw new RuntimeException("You do not own enough shares");
        } else {
            //manage shares
            ownedShares.decrease(sourceIndex, number);

            //manage money
            System.out.println("value=" + shareValue);
            System.out.println("number=" + number);
            int value = number * shareValue;
            balance = balance + value;
        }
    }

    public void buy(int targetIndex, int desiredNumber, int shareValue) {
        if (desiredNumber == 0) {
            throw new RuntimeException("Useless buy operation called (zero shares)");
        }

        //compute the total costs
        int costs = shareValue * desiredNumber; // additional param company

        //if not enough shares or not enough money
        if (costs > balance) {
            throw new RuntimeException("FUNDS ERROR");
            //Main.interaction.printUserInformation("You have insufficient funds");
            //return false;
        } else {
            System.out.println("performing bought");

            //manage shares
            ownedShares.increase(targetIndex, desiredNumber);

            //manage money
            balance = balance - costs;
        }
    }

    public String getName() {
        return name;
    }

    public int getNumberOfShares() {
        return ownedShares.getTotal();
    }

    public String takeForFree(int index, int availableShares) {
        if (availableShares == 0) {
            return ("You did not get a bonus share since there are non in stock");
        }
        ownedShares.increase(index, 1);
        return ("");
    }

    public int getNumberOfShares(int index) {
        return ownedShares.getNumberOfSharesForIndex(index);
    }

    @Override
    public boolean equals(PlayerInterface other) {
        return other.getName().equals(name);
    }

    public void takeBoni(int amount) {
        balance = balance + amount;
    }

    public void sellAllShares(int[] values, boolean[] activeCompanies) {
        //System.out.println("sell all shares called");
        for (int companyIndex = 0; companyIndex < Main.numberOfCompanies; companyIndex++) {
            if (activeCompanies[companyIndex]) {
                //System.out.println("company "+companyIndex+" sold");
                int sharesForIndex = ownedShares.getNumberOfSharesForIndex(companyIndex);
                ownedShares.decrease(companyIndex, sharesForIndex);
                balance = balance + sharesForIndex * values[companyIndex];
            }
        }
    }

    public String toString() {
        return getName();
    }
}
