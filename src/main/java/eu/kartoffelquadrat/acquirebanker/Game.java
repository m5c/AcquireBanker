/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.kartoffelquadrat.acquirebanker;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import java.io.*;
import java.util.LinkedList;

/**
 * The Game class cares about the whole games' logic and uses the Shell class for user interaction
 */
public class Game {

    //interaction
    Shell interaction = new Shell();
    //players
    private PlayerInterface[] players;
    //companies
    private String[] companyNames = new String[]
            {
                    "Worldwide", "Sackson", "Festival", "Imperial", "American", "Tower", "Continental"
            };
    private CompanyInterface[] companies = new Company[7];
    private BoardInterface hotelBoard;
    //shares
    private ShareCollectionInterface masterShares = new ShareCollection(null);
    //players
    private int currentPlayer;
    private boolean end = false;
    private int roundCounter = 0;

    Game() {
        //set initial share stock
        for (int i = 0; i < companies.length; i++) {
            //12 shares per company - in this case it is legal to use the master method
            masterShares.increaseMaster(i, Main.initialSharesPerCompany);
        }

        //allocate companies(first cheap, then normal, then expensive)
        for (int i = 0; i < 2; i++) {
            companies[i] = new Company(i, companyNames[i], CompanyType.CHEAP);
        }
        for (int i = 2; i < 5; i++) {
            companies[i] = new Company(i, companyNames[i], CompanyType.NORMAL);
        }
        for (int i = 5; i < companyNames.length; i++) {
            companies[i] = new Company(i, companyNames[i], CompanyType.EXPENSIVE);
        }

        //initiate board
        int[] colours = new int[companies.length];
        for (int i = 0; i < companies.length; i++) {
            colours[i] = companies[i].getColour();
        }
        hotelBoard = new Board(companyNames, colours);

        //get player names
        String[] playerNames = interaction.getPLayers();
        players = new PlayerInterface[playerNames.length];
        for (int i = 0; i < playerNames.length; i++) {
            players[i] = new Player(playerNames[i], masterShares);
        }

    }

    /**
     * loads a game state from a xml file
     *
     * @param path as the absolute path to a previously created game-save file.
     * @return a game object for the newly created game entity.
     * @throws IOException in case the provided save-game could not be parsed or interpreted
     */
    public static Game load(String path) throws IOException {
        XStream stream = new XStream(new StaxDriver());
        InputStream inputStream = new FileInputStream(path);
        Game result = (Game) stream.fromXML(inputStream);
        inputStream.close();

        return result;
    }

    /**
     * use this method to kick on a loaded or new game the initial player is either the first player or the "current
     * player" extracted from the loaded game file
     *
     * @param hopIn as a helper parementer to determine whether a round is currently in progress that mus be re-joined
     * @throws IOException in case there was an error while implicitly creating a save-game
     */
    public void continueGame(boolean hopIn) throws IOException {
        //perform rounds until one company has reached a size of 41/+ tiles
        while (!end) {
            if (hopIn) {
                //start semi round
                round(currentPlayer);
                hopIn = false;
            } else {
                //start new round
                roundCounter++; //there is no round counter in the previous clause, since it is only the continuation of a round, not a new round in the above case
                round(0);
            }
        }

        //sell all shares of all players
        sellAllShares();

        //print final score data
        interaction.gameOver(players);
    }

    private void round(int startPlayer) throws IOException {
        for (currentPlayer = startPlayer; currentPlayer < players.length; currentPlayer++) {
            if (!end) {
                save(Main.savePath + File.separatorChar + "acquire-" + roundCounter + "-" + (currentPlayer + 1) + ".xml");
                turn(currentPlayer);
            }
        }
    }

    private void turn(int playerIndex) throws IOException {
        //print info
        interaction.printInfo(roundCounter, players[playerIndex].getName(), companies, masterShares, players, hotelBoard);

        //get legal position
        int[] position = getValidPosition();

        //react appropriate
        LayType operation = hotelBoard.getOperationToPerformForAddingPiece(position[0], position[1]);

        if (operation == LayType.EXPAND) {
            int companyIndex = operation.getConcernedCompanies()[0];
            int increase = hotelBoard.setPositionToCompany(position[0], position[1], companyIndex);
            companies[companyIndex].increaseCompany(increase);
        } else {
            if (operation == LayType.UNDECLARED) {
                hotelBoard.setPositionToNeutral(position[0], position[1]);
            } else {
                if (operation == LayType.FUND) {
                    //lay anbd convert pieces on board
                    int fundedIndex = getValidFund();
                    int increase = hotelBoard.setPositionToCompany(position[0], position[1], fundedIndex);

                    //update company size
                    companies[fundedIndex].increaseCompany(increase);

                    //donate one share to player who funded company - tell player in case he did not get a bonus share
                    String didItWork = players[currentPlayer].takeForFree(fundedIndex, masterShares.getNumberOfSharesForIndex(fundedIndex));
                    if (!didItWork.equals("")) {
                        interaction.printUserInformation(didItWork);
                    }
                } else {
                    if (operation == LayType.JOIN) {
                        //resolve conflicts and get order of joins
                        int[] order = getOrder(operation);

                        //care about joins
                        int winner = executeJoins(order);

                        //lay piece
                        hotelBoard.setPositionToCompany(position[0], position[1], winner);
                        getCompanyWithIndex(winner).increaseCompany(1);
                    }
                }
            }
        }
        //update view(new tile layed, company value has changed)
        interaction.printInfo(roundCounter, players[currentPlayer].getName(), companies, masterShares, players, hotelBoard);

        //buy shares
        String[] shares = getValidOption();

        //perform buy action of player
        int[] option = optionAsStringToCompany(shares);
        for (int i = 0; i < option.length; i++) {
            if (option[i] != 0) {
                players[currentPlayer].buy(i, option[i], getCompanyWithIndex(i).getShareValue());
            }
        }

        //check if a company is greater/equal to a size of 41 tiles
        end = isCompanyGreaterLimit();
    }

    public ShareCollectionInterface getmasterShares() {
        return masterShares;
    }

    private void sellAllShares() {
        int[] values = new int[Main.numberOfCompanies];
        boolean[] activeCompanies = new boolean[Main.numberOfCompanies];

        for (int i = 0; i < values.length; i++) {
            //get company values
            values[i] = getCompanyWithIndex(i).getShareValue();

            //check if company is active
            activeCompanies[i] = getCompanyWithIndex(i).isActive();

            //boni to major and secondary shareholders
            if (activeCompanies[i]) {
                applyBoni(i);
            }
        }

        for (PlayerInterface thisPlayer : players) {
            thisPlayer.sellAllShares(values, activeCompanies);
        }
    }

    public CompanyInterface getCompanyWithIndex(int index) {
        return companies[index];
    }

    private boolean isCompanyGreaterLimit() {
        for (int i = 0; i < companies.length; i++) {
            if (companies[i].getSize() >= Main.finalLimit) {
                return true;
            }
        }
        return false;
    }

    /**
     * continues asking for a compay until a legal company is received
     *
     * @return
     */
    private int getValidFund() {
        boolean fine = false;
        int fundIndex = -1; //just a dummy value
        while (!fine) {
            String companyName = interaction.getDesiredCompany(getAvailableCompanies(), true);
            fundIndex = companyNameToIndex(companyName);
            for (CompanyInterface currentCopmany : getAvailableCompanies()) {
                if (companyNameToIndex(currentCopmany.getName()) == fundIndex) {
                    fine = true;
                }
            }
        }
        return fundIndex;
    }

    //continues to ask for positions until a legal one was entered
    public int[] getValidPosition() {
        int[] position = null;

        boolean fine = false;
        while (!fine) {
            position = interaction.askForPosition();

            boolean validAndNotBlank = true;
            if (!hotelBoard.isValidPosition(position[0], position[1])) {
                interaction.printUserInformation("Position is out of bounds");
                validAndNotBlank = false;
            } else {
                if (!hotelBoard.isBlankPosition(position[0], position[1])) {
                    interaction.printUserInformation("Position is already in use");
                    validAndNotBlank = false;
                }
            }

            if (validAndNotBlank) {
                LayType operation = hotelBoard.getOperationToPerformForAddingPiece(position[0], position[1]);
                if (operation == LayType.JOIN && !isJoinable(operation.getConcernedCompanies())) {
                    interaction.printUserInformation("Companies can't be joined");
                } else {
                    if (operation == LayType.FUND && !isCompanyAvailable()) {
                        interaction.printUserInformation("Funding impossible");
                    } else {
                        fine = true;
                    }
                }
            }
        }

        return position;
    }

    //checks if there is more then one save company
    private boolean isJoinable(int[] indexes) {
        int counter = 0;
        for (int i = 0; i < indexes.length; i++) {
            if (companies[indexes[i]].isSave()) {
                counter++;
            }
        }
        return (counter < 2);
    }

    //checks if there are available companies to be founded
    private boolean isCompanyAvailable() {
        return !getAvailableCompanies().isEmpty();
    }

    //gets list of available companies
    private LinkedList<CompanyInterface> getAvailableCompanies() {
        LinkedList<CompanyInterface> list = new LinkedList<CompanyInterface>();
        for (int i = 0; i < companies.length; i++) {
            if (companies[i].getSize() == 0) {
                list.add(companies[i]);
            }
        }

        return list;
    }

    public int companyNameToIndex(String name) {
        for (int i = 0; i < companies.length; i++) {
            if (companies[i].getName().equals(name)) {
                return i;
            }
        }

        throw new RuntimeException("Undefined name: " + name);
    }

    /**
     * asks shell for share options until a valid one is received
     */
    private String[] getValidOption() {
        //check which companies trade shares
        LinkedList<CompanyInterface> availableShares = new LinkedList<CompanyInterface>();
        for (int i = 0; i < companies.length; i++) {
            if (companies[i].isActive()) {
                availableShares.add(companies[i]);
            }
        }

        boolean fine = false;
        String[] option = null;

        while (!fine) {
            //i will first set the fine option true so i can abourt further test in case any test has set it to false
            fine = true;

            //get option
            option = interaction.askForShareOption(availableShares);

            //filter blank option
            if (option.length == 0) {
                return option;
            }

            //get user option number for each company index
            int[] desire = optionAsStringToCompany(option);

            //now there will be three tests(tradeable, in stock, enough money)

            if (fine) {
                //check if the company is tradeable
                for (int i = 0; i < desire.length; i++) {
                    if (desire[i] > 0) {
                        //not for sale
                        if (companies[i].getSize() == 0) {
                            interaction.printUserInformation("Company does not yet trade shares");
                            fine = false;
                        }
                    }
                }
            }

            if (fine) {
                //now check if this desire is contained in the stock
                for (int i = 0; i < desire.length; i++) {
                    if (masterShares.getNumberOfSharesForIndex(i) < desire[i]) {
                        interaction.printUserInformation("Not enough " + companies[i].getName() + " shares in stock");
                        fine = false;
                    }
                }
            }

            if (fine) {
                //check for user cash
                //compute total needed funds
                int requiredMoney = 0;
                for (int i = 0; i < desire.length; i++) {
                    //number of shares times value of current share
                    requiredMoney = requiredMoney + (desire[i] * (companies[i].getShareValue()));
                }

                if (players[currentPlayer].getBalance() < requiredMoney) {
                    //System.out.println("you have: " + players[currentPlayer].getBalance());
                    //System.out.println("you need: " + requiredMoney);

                    //TODO                   //own method here
                    interaction.printUserInformation("You have insufficient funds!");
                    fine = false;
                }
            }
        }

        return option;
    }

    /**
     * convert an String array of size up to 3 into an int array. The int arrays positions match the company index
     * numbers and the entry is the number of shares which are ment to be bought
     */
    private int[] optionAsStringToCompany(String[] option) {
        int[] desire = new int[companies.length];
        for (int i = 0; i < option.length; i++) {
            desire[companyNameToIndex(option[i])]++;
        }

        return desire;
    }

    private int[] getOrder(LayType joinInformation) {
        //i'd like to filter laytypes which are not joins here and throw an exception
        int[] concernedCompanies = joinInformation.getConcernedCompanies();

        LinkedList<Integer> finalOrder = resolveOrder(concernedCompanies);

        //an information methon in the shell might be better here
        for (int currentIndex : finalOrder) {
            System.out.print(getCompanyWithIndex(currentIndex).getName() + "->");
        }
        System.out.println("");

        Integer[] integerOrder = finalOrder.toArray(new Integer[0]);

        //convert to int... i doubt this is the best way to do this
        int[] intOrder = new int[integerOrder.length];
        for (int i = 0; i < integerOrder.length; i++) {
            intOrder[i] = integerOrder[i];
        }

        return intOrder;
    }

    private LinkedList<Integer> resolveOrder(int[] remainingConcernedCompanies) {
        //System.out.println("resolving order");
        if (remainingConcernedCompanies.length > 2) //?TODO: Check condition brach
        {
            int supreme = getSupreme(remainingConcernedCompanies);

            //create array which has all elements but the supreme
            int[] nextGenerationArray = new int[remainingConcernedCompanies.length - 1];
            boolean supremeOcurred = false;
            //copy until supreme element ant then copy following position of original array
            for (int i = 0; i < nextGenerationArray.length; i++) {
                if (supremeOcurred) {
                    nextGenerationArray[i] = remainingConcernedCompanies[i + 1];
                } else {
                    if (remainingConcernedCompanies[i] == supreme) {
                        supremeOcurred = true;
                        nextGenerationArray[i] = remainingConcernedCompanies[i + 1];
                    } else {
                        nextGenerationArray[i] = remainingConcernedCompanies[i];
                    }
                }
            }

            //the call gives me an LinkedList so i can set the supreme at the front position
            LinkedList<Integer> order;
            order = resolveOrder(nextGenerationArray);
            order.addFirst(supreme);

            return order;
        } else {
            if (remainingConcernedCompanies.length == 2) {
                //System.out.println("getting supreme II");
                int supreme = getSupreme(remainingConcernedCompanies);
                LinkedList<Integer> order = new LinkedList<Integer>();

                if (remainingConcernedCompanies[0] == supreme) {
                    order.add(supreme);
                    order.addLast(remainingConcernedCompanies[1]);
                } else {
                    if (remainingConcernedCompanies[1] == supreme) {
                        order.add(supreme);
                        order.addLast(remainingConcernedCompanies[0]);
                    } //the supreme element must be in this array
                    else {
                        throw new RuntimeException("Supreme element not found");
                    }
                }

                return order;
            } //illegal situation - at least two partner needed
            else {
                throw new RuntimeException("Cant resolve order - only one company received!");
            }
        }
    }

    /**
     * get you one of the greatest comapnies received in the list. In case there a more then one the user will be
     * asked.
     *
     * @param remainingConcernedCompanies
     * @return
     */
    private int getSupreme(int[] remainingConcernedCompanies) {
        LinkedList<Integer> greatest = new LinkedList<Integer>();

        int highestSeenSize = 0;

        for (int i = 0; i < remainingConcernedCompanies.length; i++) {
            //compare size of current company with current size score of list
            if (getCompanyWithIndex(remainingConcernedCompanies[i]).getSize() > highestSeenSize) {
                highestSeenSize = getCompanyWithIndex(remainingConcernedCompanies[i]).getSize();
                greatest.clear();
                greatest.add(remainingConcernedCompanies[i]);
            } else {
                if (getCompanyWithIndex(remainingConcernedCompanies[i]).getSize() == highestSeenSize) {
                    //add the current element
                    greatest.add(remainingConcernedCompanies[i]);
                }
            }
        }

        //in case the list has only one element -> return this one
        if (greatest.size() == 1) {
            //System.out.println("supreme is "+getCompanyWithIndex(greatest.getFirst()));
            return greatest.getFirst();
        }

        //in case the list is bigger ask the user which one he wants
        //convert index list into list of company references
        LinkedList<CompanyInterface> greatestCompanies = new LinkedList<CompanyInterface>();
        for (int currentIndex : greatest) {
            greatestCompanies.add(getCompanyWithIndex(currentIndex));
        }

        String name = interaction.getDesiredCompany(greatestCompanies, false);

        //System.out.println("supreme is "+name);
        return companyNameToIndex(name);
    }

    /**
     * this method initiates the swallowing according to an order using the swallow method
     * <p>
     * so the greatest company will swallow the second greatest, then the third greatest an so on...
     *
     * @param order
     */
    private int executeJoins(int[] order) {
        //this is the company which will swallow the other ones one by one
        int masterCompany = order[0];

        for (int i = 1; i < order.length; i++) {
            swallow(masterCompany, order[i]);
        }

        return masterCompany;
    }

    /**
     * this method does the share, balance and piece managaement for a swallow beetween two companies and asks users
     * what the want to do with their shares
     *
     * @param winnerIndex as the index of the swallowing company
     * @param looserIndex as the index of the company being swallowed
     */
    private void swallow(int winnerIndex, int looserIndex) {
        applyBoni(looserIndex);

        //ask users what the want to do with their shares
        convert(winnerIndex, looserIndex);

        //reset looser company
        getCompanyWithIndex(looserIndex).reset();

        //swallow company on board
        int growth = hotelBoard.swallow(winnerIndex, looserIndex);
        getCompanyWithIndex(winnerIndex).increaseCompany(growth);

    }

    /**
     * computes the boni for major and second shareholder
     * <p>
     * multiple majors -> sum of major and second boni / no of players one major, no second -> sum of major and second
     * to major shareholder one mejaor multiple second -> major to major and equal parts of second to each of second
     * shareholder
     *
     * @param looserindex as index of the sold company
     * @return 2d array: in x: users, in y: amount
     */
    private void applyBoni(int looserIndex) {
        //compute boni
        int primaryBoni = getCompanyWithIndex(looserIndex).getShareValue() * 10;
        int secondaryBoni = primaryBoni / 2;

        //get primary and secondary shareholder
        //get list of people with shares
        LinkedList<PlayerInterface> majorShareholders = new LinkedList<PlayerInterface>();
        LinkedList<PlayerInterface> secondaryShareholders = new LinkedList<PlayerInterface>();

        //iterate over array
        int highScore = 0;
        int secondScore = 0;
        for (PlayerInterface currentBoniPlayer : players) {
            if (currentBoniPlayer.getNumberOfShares(looserIndex) != 0) {
                if (currentBoniPlayer.getNumberOfShares(looserIndex) > highScore) {
                    //store highscores in secondscores
                    secondaryShareholders = majorShareholders;
                    //set new secondary highscore
                    if (!secondaryShareholders.isEmpty()) {
                        secondScore = secondaryShareholders.getFirst().getNumberOfShares(looserIndex);
                    }
                    majorShareholders = new LinkedList<PlayerInterface>();
                    highScore = currentBoniPlayer.getNumberOfShares(looserIndex);
                    majorShareholders.add(currentBoniPlayer);
                } else {
                    if (currentBoniPlayer.getNumberOfShares(looserIndex) == highScore) {
                        majorShareholders.add(currentBoniPlayer);
                    } //so from here on the score must be smaller than the high sore
                    else {
                        if (currentBoniPlayer.getNumberOfShares(looserIndex) > secondScore) {
                            secondaryShareholders.clear();
                            secondaryShareholders.add(currentBoniPlayer);
                            secondScore = currentBoniPlayer.getNumberOfShares(looserIndex);
                        } else {
                            if (currentBoniPlayer.getNumberOfShares(looserIndex) == secondScore) {
                                secondaryShareholders.add(currentBoniPlayer);
                            }
                        }
                    }
                }
            }
        }

        //in case there is more then one major shareholder, the boni will be added and splitten into equal parts
        if (majorShareholders.size() > 1) {
            int totalBoni = primaryBoni + secondaryBoni;
            int equalAmount = totalBoni / majorShareholders.size();

            //give boni to each of the major shareholders
            for (PlayerInterface currentMajorShareHolder : majorShareholders) {
                //print info
                interaction.printUserInformation(currentMajorShareHolder.getName() + " received a boni of " + equalAmount + " (Combined shareholder of " + getCompanyWithIndex(looserIndex).getName() + ")");
                currentMajorShareHolder.takeBoni(equalAmount);
            }
        } //one major, no second
        else {
            if (majorShareholders.size() == 1 && secondaryShareholders.isEmpty()) {
                //all boni to major
                int amount = primaryBoni + secondaryBoni;

                //print info
                interaction.printUserInformation(majorShareholders.getFirst() + " received a boni of " + amount + " (Exclusive shareholder of " + getCompanyWithIndex(looserIndex).getName() + ")");
                majorShareholders.getFirst().takeBoni(amount);
            } //one major, several second
            else {
                if (majorShareholders.size() == 1 && secondaryShareholders.size() >= 1) {
                    //major gets major boni, second mony will be splitted
                    interaction.printUserInformation(majorShareholders.getFirst() + " received a boni of " + primaryBoni + " (Major of " + getCompanyWithIndex(looserIndex).getName() + ")");
                    majorShareholders.getFirst().takeBoni(primaryBoni);

                    //works for one player, too
                    int equalAmount = secondaryBoni / secondaryShareholders.size();
                    for (PlayerInterface currentSecondaryShareholder : secondaryShareholders) {
                        interaction.printUserInformation(currentSecondaryShareholder + " received a boni of " + equalAmount + " (Secondary of " + getCompanyWithIndex(looserIndex).getName() + ")");
                        currentSecondaryShareholder.takeBoni(equalAmount);
                    }
                } //no major
                else {
                    if (majorShareholders.isEmpty()) {
                        throw new RuntimeException("No primary shareholder found");
                    }
                }
            }
        }
    }

    /**
     * asks all players what the want to do with their shares of the swallowed company. They can either: - keep their
     * shares - convert an even number n of shares into n/2 shares of the winner company - sell an desired number of
     * shares
     *
     * @param winnerIndex
     * @param looserIndex
     */
    private void convert(int winnerIndex, int looserIndex) {
        //iterate over players, geginning with the currentplayer and ask them what the want to do
        for (int i = currentPlayer; (i < (players.length + currentPlayer)); i++) {
            if (players[i % players.length].getNumberOfShares(looserIndex) != 0) {
                //get a semantical correct operation
                boolean fine = false;
                PlayerInterface cp = null; //dummy value
                int[] operation = new int[0]; //dummy value

                while (!fine) {
                    fine = true;

                    cp = players[i % players.length];

                    //convert is 0 sell is index 1
                    operation = interaction.askForShareOperation(cp.getName(), cp.getNumberOfShares(looserIndex));

                    if (operation.length != 2) {
                        throw new RuntimeException("Operation request sent illegal number of parameters");
                    }

                    //check if the user has enough shares for his request
                    if (operation[0] + operation[1] > cp.getNumberOfShares(looserIndex)) {
                        fine = false;
                    }

                    if (fine && (operation[0] % 2 != 0)) {
                        fine = false;
                    }

                    //the first request skips the rest in case it is useless
                    //not enough shares for conversion in stock
                    if (fine && (operation[0] / 2 > masterShares.getNumberOfSharesForIndex(winnerIndex))) {
                        fine = false;
                    }

                }
                cp.convert(looserIndex, winnerIndex, operation[0]);

                //
                System.out.println("size=" + getCompanyWithIndex(looserIndex).getSize());
                System.out.println("value=" + getCompanyWithIndex(looserIndex).getShareValue());
                System.out.println("amount=" + operation[1]);
                cp.sell(looserIndex, operation[1], getCompanyWithIndex(looserIndex).getShareValue());
            }
        }
    }

    public void save(String path) throws IOException {
        XStream stream = new XStream(new StaxDriver());
        OutputStream outputStream = new FileOutputStream(path);
        stream.toXML(this, outputStream);
        outputStream.close();
    }
}
