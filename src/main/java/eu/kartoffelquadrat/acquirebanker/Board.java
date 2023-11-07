/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.kartoffelquadrat.acquirebanker;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Max Schiedermeier
 */
public class Board implements BoardInterface {

    //indexes on the array positions allow to see the content
    //special codes are -1 for blank and -2 for neutral
    //x=12, y=9
    int[][] hotelArray = new int[12][9];                                                //!
    String[] companyNames;
    int[] colours;

    Board(String[] companyNames, int[] colours) {
        this.companyNames = companyNames;
        this.colours = colours;

        //itterate by columns
        for (int x = 0; x < hotelArray.length; x++)                                        //!
        {
            for (int y = 0; y < hotelArray[x].length; y++) {
                //initialize with "unused flag"
                hotelArray[x][y] = -1;
            }
        }
    }

    /*
     * 3 steps
     *
     * -run through neigbours and search for other companies
     * -resolve swallowing dependencies
     * -involve swallowed companies and neutral pieces
     *
     */
    public LayType getOperationToPerformForAddingPiece(int xPos, int yPos) {

        if (!isValidPosition(xPos, yPos) || !isBlankPosition(xPos, yPos)) {
            throw new RuntimeException("Illegal position!");
        }

        LinkedList<Integer> neighbours = getNeighbourPositions(xPos, yPos);
        if (neighbours.size() % 2 != 0) //since i allway store two elements for each position (x, y)
        {
            throw new RuntimeException("corrupted position list received!");
        }
        //save all different industrie which are in touch with this
        LinkedList<Integer> concernedCompanies = getListOfConcernedCompanies(neighbours);

        //a list of concerned neutal positions
        LinkedList<Integer> neutralPositions = getListOfNeutralNeighbours(neighbours);

        /*
         * the following cases decide which enum is to be returned
         */
        //no company neighbour
        if (concernedCompanies.size() == 0) {
            //no neutral neighbour
            if (neutralPositions.size() == 0) {
                //neutral piece to be set
                return LayType.UNDECLARED;
            } //one or more neutral neighbours
            else {
                //fund company
                return LayType.FUND;
            }
        } //one company neighbour
        else {
            if (concernedCompanies.size() == 1) {
                //expand company - no matter if there are neutral extra pieces or not the will be swallowed anywayi
                LayType returnLayType = LayType.EXPAND;

                //in case it is an expand, there is only one element, so this should do it
                int[] concernedCompaniesArray = new int[]
                        {
                                concernedCompanies.getFirst()
                        };
                returnLayType.setConcendesCompanies(concernedCompaniesArray);
                return LayType.EXPAND;
            } //this is the case whenn there is a join - be cautious there might be more then one company concerned
            else {
                LayType returnLayType = LayType.JOIN;

                //store the information of the concerned companies in the static enum
                int[] concernedCompaniesArray = new int[concernedCompanies.size()];
                int currentPosition = 0;
                for (int currentCompany : concernedCompanies) {
                    concernedCompaniesArray[currentPosition] = currentCompany;
                    currentPosition++;
                }
                returnLayType.setConcendesCompanies(concernedCompaniesArray);

                return returnLayType;
            }
        }

        /*
        LayType myFunnyLayType = LayType.FUND;
        myFunnyLayType.setConcendesCompanies(new int[] {1, 2, 3});

         * 
         */

    }

    public boolean isValidPosition(int xPos, int yPos) {
        if (xPos < 0 || xPos > hotelArray.length - 1 || yPos < 0 || yPos > hotelArray[xPos].length - 1) //!
        {
            return false;
        }
        return true;
    }

    public boolean isBlankPosition(int xPos, int yPos) {
        return (hotelArray[xPos][yPos] == -1);                                                        //!
    }

    /*
     * The return list stores the other positions gouped by x and y positions
     */
    private LinkedList<Integer> getNeighbourPositions(int centerX, int centerY) {
        if (!isValidPosition(centerX, centerY)) {
            throw new RuntimeException("Invalid Position!");
        }

        //add valid positions to list
        LinkedList<Integer> neighbourPositions = new LinkedList<Integer>();
        if (isValidPosition(centerX + 1, centerY)) {
            neighbourPositions.add(centerX + 1);
            neighbourPositions.add(centerY);
        }
        if (isValidPosition(centerX - 1, centerY)) {
            neighbourPositions.add(centerX - 1);
            neighbourPositions.add(centerY);
        }
        if (isValidPosition(centerX, centerY + 1)) {
            neighbourPositions.add(centerX);
            neighbourPositions.add(centerY + 1);
        }
        if (isValidPosition(centerX, centerY - 1)) {
            neighbourPositions.add(centerX);
            neighbourPositions.add(centerY - 1);
        }

        return neighbourPositions;
    }

    private int getCompanyAt(int xPos, int yPos) {
        return hotelArray[xPos][yPos];
    }

    private LinkedList<Integer> getListOfConcernedCompanies(LinkedList<Integer> positions) {
        //i should later get this number from the game object
        boolean[] visitedCompanies = new boolean[LocalMainLauncher.numberOfCompanies];

        //get positions of neighbours
        Iterator<Integer> neighbourIterator = positions.iterator();
        while (neighbourIterator.hasNext()) {
            int currentX = neighbourIterator.next();
            int currentY = neighbourIterator.next();

            int companyOnCurrentPosition = getCompanyAt(currentX, currentY);
            if (companyOnCurrentPosition >= 0) {
                //add new companies to list
                visitedCompanies[companyOnCurrentPosition] = true;
            }
        }

        //convert array visited-information to list
        LinkedList<Integer> concernedCompanies = new LinkedList<Integer>();

        for (int i = 0; i < visitedCompanies.length; i++) {
            if (visitedCompanies[i]) {
                concernedCompanies.add(i);
            }
        }

        return concernedCompanies;
    }

    private LinkedList<Integer> getListOfNeutralNeighbours(LinkedList<Integer> positions) {
        //List of neutral positions
        LinkedList<Integer> neutralPositions = new LinkedList<Integer>();

        //get positions of neighbours
        Iterator<Integer> neighbourIterator = positions.iterator();
        while (neighbourIterator.hasNext()) {
            int currentX = neighbourIterator.next();
            int currentY = neighbourIterator.next();

            int companyOnCurrentPosition = getCompanyAt(currentX, currentY);
            if (companyOnCurrentPosition == -2) {
                //add new companies to list
                neutralPositions.add(currentX);
                neutralPositions.add(currentY);
            }
        }

        return neutralPositions;
    }

    public void setPositionToNeutral(int xPos, int yPos) {
        //some checks might be usefull here
        //eg are there neutral neighbours/other companies
        LinkedList<Integer> neighbourPositions = getNeighbourPositions(xPos, yPos);
        LinkedList<Integer> companyNeighbours = getListOfConcernedCompanies(neighbourPositions);
        LinkedList<Integer> neutralNeighbours = getListOfNeutralNeighbours(neighbourPositions);
        if (companyNeighbours.size() > 0 || neutralNeighbours.size() > 0) {
            throw new RuntimeException("Can't add neutral position, since neutral or un neutral neighbours already exist");
        }

        hotelArray[xPos][yPos] = -2;
    }

    //why was the first param called companies???
    private boolean isPieceNotNextToForeignCompanies(LinkedList<Integer> neighbourPositions, int ownCompanyIndex) {
        LinkedList<Integer> allConcernedCompanies = getListOfConcernedCompanies(neighbourPositions);

        for (Integer currentCompanyIndex : allConcernedCompanies) {
            if (currentCompanyIndex != ownCompanyIndex) {
                return false;
            }
        }
        return true;
        /*

        //counter for appearances of own company
        int legalAppearanceCounter = 0;

        //iterate over list and filter all own company positions
        Iterator<Integer> neighbourIterator = allConcernedCompanies.iterator();
        while (neighbourIterator.hasNext())
        {
        int currentX = neighbourIterator.next();
        int currentY = neighbourIterator.next();

        int companyOnCurrentPosition = getCompanyAt(currentX, currentY);
        if (companyOnCurrentPosition == ownCompanyIndex)
        {
        //note that there are two more positions in this list which are legal
        legalAppearanceCounter = legalAppearanceCounter + 2;
        }
        }

        //in case the piece is legal there are no foreign pieces next to this one
        return (allConcernedCompanies.size() - legalAppearanceCounter == 0);
         */
    }

    //return the number of pieces the company has grown by
    //this method exploits the fact that a neutral piece cannot have other neutral neighbours
    //(otherwise the piece would not still be neutral)
    public int setPositionToCompany(int xPos, int yPos, int companyIndex) {
        //some checks might be usefull here
        //eg is this piece next to a foreign companies piece - neutral however is allowed
        LinkedList<Integer> neighbourPositions = getNeighbourPositions(xPos, yPos);
        if (!isPieceNotNextToForeignCompanies(neighbourPositions, companyIndex)) {
            throw new RuntimeException("Can not lay piece since foreign company is next to this position");
        }

        //set company index for layed position in hotelarray
        hotelArray[xPos][yPos] = companyIndex;

        //now add all neutral pieces which are next to the added position
        //this can only be direct neighbours due to the characteristics of neutral parts
        //iterate over nieghbours and give me the neutal ones, only
        LinkedList<Integer> neutralNeighbourPositions = getListOfNeutralNeighbours(neighbourPositions);

        Iterator<Integer> neighbourIterator = neutralNeighbourPositions.iterator();
        while (neighbourIterator.hasNext()) {
            int currentX = neighbourIterator.next();
            int currentY = neighbourIterator.next();

            //set neutral neighboures positions to founded companies index
            hotelArray[currentX][currentY] = companyIndex;
        }

        // division by too, since i have 2 elements for each position
        //+1 due to own piece was layed, too
        return (neutralNeighbourPositions.size() / 2 + 1);
    }

    public int swallow(int greaterCompanyIndex, int smallerCompanyIndex) {
        //iterate over hotel array and convert all apperances of the swallowed company
        int appearanceCounter = 0;
        //itterate by columns
        for (int x = 0; x < hotelArray.length; x++)                                        //!
        {
            for (int y = 0; y < hotelArray[x].length; y++) {
                //initialize with "unused flag"
                if (hotelArray[x][y] == smallerCompanyIndex) {
                    hotelArray[x][y] = greaterCompanyIndex;
                    appearanceCounter++;
                }
            }
        }

        return appearanceCounter;
    }

    public String toString() {
        StringBuilder returnString = new StringBuilder();

        char row = 'A';

        System.out.println("");
        System.out.println("Map:");
        System.out.print(" ");
        for (int i = 0; i < hotelArray.length; i++) {
            System.out.printf("%2d", i + 1);
        }
        System.out.println("");

        //iterate over array and print indexes (formated)
        for (int y = 0; y < hotelArray[0].length; y++)                                        //!
        {
            returnString.append(row).append(" ");
            row++;

            for (int x = 0; x < hotelArray.length; x++) {
                //initialize with "unused flag"
                if (hotelArray[x][y] >= 0) {
                    char letter = companyNames[hotelArray[x][y]].charAt(0);
                    returnString.append("\u001B[38;5;" + colours[hotelArray[x][y]] + "m" + letter + "\u001B[m ");
                }
                if (hotelArray[x][y] == -1) {
                    returnString.append("  ");
                }
                if (hotelArray[x][y] == -2) {
                    returnString.append("N ");
                }
            }
            returnString.append("\n");
        }
        return returnString.toString();
    }
}
