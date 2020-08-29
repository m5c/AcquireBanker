/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.kartoffelquadrat.acquirebanker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * utility class for i/o
 *
 * @author Max Schiedermeier
 */
class Shell {

    public String[] getPlayers() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        //get number of players
        System.out.println("Enter the number of players");
        boolean fine = false;
        int numberOfPlayers = 0;

        while (!fine || numberOfPlayers <= 0 || numberOfPlayers > 10) {
            try {
                numberOfPlayers = Integer.parseInt(br.readLine());
                fine = true;
            } catch (IOException ex) {
                System.out.println("Not a number!");
            }
        }
        String[] players = new String[numberOfPlayers];

        //get names
        System.out.println("Enter players in order of the game commence");
        for (int i = 0; i < numberOfPlayers; i++) {
            try {
                players[i] = br.readLine();
            } catch (IOException ex) {
                Logger.getLogger(Shell.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return players;
    }

    public void printUserInformation(String information) {
        System.out.println(information);
    }

    public void printUserData(PlayerInterface[] players) {
        System.out.print("            ");
        for (int i = 0; i < players.length; i++) {
            System.out.printf("%12s", players[i].getName());
        }
        System.out.print("\nCredit      ");
        for (int i = 0; i < players.length; i++) {
            System.out.printf("%12d", players[i].getBalance());
        }
        System.out.print("\nShares      ");
        for (int i = 0; i < players.length; i++) {
            System.out.printf("%12d", players[i].getNumberOfShares());
        }
        System.out.println("");
    }

    private void printShareData(CompanyInterface[] companies, ShareCollectionInterface masterCollection) {
        System.out.print("            ");
        for (int i = 0; i < companies.length; i++) {
            //System.out.printf("%12s", companies[i].getName());
            int colour = companies[i].getColour();
            System.out.printf("\u001B[38;5;%dm%12s\u001B[m", colour, companies[i].getName());
        }
        System.out.print("\nValue       ");
        for (int i = 0; i < companies.length; i++) {
            System.out.printf("%12d", companies[i].getShareValue());
        }
        System.out.print("\nShares left ");
        for (int i = 0; i < companies.length; i++) {
            System.out.printf("%12d", masterCollection.getNumberOfSharesForIndex(i));
        }
        System.out.print("\nSize        ");
        for (int i = 0; i < companies.length; i++) {
            System.out.printf("%12d", companies[i].getSize());
        }
        System.out.println("");
    }

    private void printData(CompanyInterface[] companies, ShareCollectionInterface masterCollection, PlayerInterface[] players) {
        printShareData(companies, masterCollection);
        System.out.println("------------------------------------------------------------------------------------------------");
        printUserData(players);
        System.out.println("------------------------------------------------------------------------------------------------");
    }

    private void printRoundInfo(int round) {
        System.out.print((char) 27 + "[2J" + (char) 27 + "[H");
        System.out.println("Round " + round);
    }

    private void printTurnInfo(String player) {
        System.out.println("\u001b[2;44m It's " + player + "'s turn \u001b[m");
    }

    /**
     * ask player for tile to be layed - will continue asking until a syntatical correct position was entered
     */
    public int[] askForPosition() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int[] position = new int[2];
        boolean fine = false;

        while (!fine) {
            System.out.print("position: ");
            try {
                String positionAsString = br.readLine();
                char[] positionAsCharArray = positionAsString.toCharArray();
                if (positionAsCharArray.length > 3 || positionAsCharArray.length < 2) {
                    continue;
                }
                //check for length 2
                if (positionAsCharArray.length == 2) {
                    if (isInt(positionAsString.substring(0, 1))) {
                        //-1 due to array positions in board implementation (chars also start mapping with 0)
                        position[0] = (Integer.parseInt(positionAsString.substring(0, 1))) - 1;
                    } else {
                        System.out.println("Bad format!");
                        continue;
                    }
                }

                //check for length 3
                if (positionAsCharArray.length == 3) {
                    if (isInt(positionAsString.substring(0, 2))) {
                        //-1 due to array positions in board implementation (chars also start mapping with 0)
                        position[0] = (Integer.parseInt(positionAsString.substring(0, 2))) - 1;
                    } else {
                        System.out.println("Bad format!");
                        continue;
                    }
                }

                //get letter
                position[1] = charToInt(positionAsCharArray[positionAsCharArray.length - 1]);
                fine = true;

            } catch (Exception ex) {
                System.out.println("Bad Format!");
            }
        }

        //at this point i have a received a syntactical valid position
        return position;
    }

    public String getDesiredCompany(LinkedList<CompanyInterface> availableCompanies, boolean isFund) {
        System.out.println("Available companies:");
        System.out.print("    ");
        for (CompanyInterface currentCompany : availableCompanies) {
            int colour = currentCompany.getColour();
            System.out.printf("\u001B[38;5;%dm%s \u001B[m", colour, currentCompany.getName());
        }
        System.out.println("");
        System.out.println(isFund == true ? "Which company shall be funded" : "Which company shall remain");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        boolean fine = false;
        char selection = '-';

        while (!fine) {
            try {
                String inputString = br.readLine();
                if (inputString.equals("")) {
                    System.out.print("Enter first character: ");
                    continue;
                }

                selection = inputString.charAt(0);
                fine = !charToName(selection).equals("unknown");

            } catch (IOException ex) {
                continue;
            }
        }

        return charToName(selection);
    }

    /**
     * asks the player for a blank, single, double or tripple code for the shares he wans to buy in this round. The
     * codes are non sensitive, but must be written as one word, without blanks or other separators. example codes are:
     * sSt as two sacksons, one tower CCC as 3 continental, etc
     *
     * @return an int[] of size up to 3 will be returned, coding the indexes of the related companies
     */
    public String[] askForShareOption(LinkedList<CompanyInterface> availableShares) {
        if (availableShares.isEmpty()) {
            return new String[]
                    {
                    };
        }

        //print options
        System.out.println("Choose a combination of the following");
        System.out.print("    ");
        for (CompanyInterface currentOption : availableShares) {
            int colour = currentOption.getColour();
            System.out.printf("\u001B[38;5;%dm%s \u001B[m", colour, currentOption.getName());
        }
        System.out.println("");

        //return type variable
        String[] userOption = null;

        //reader
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        //ask for postitions until received an syntatical correct position
        boolean fine = false;
        while (!fine) {
            fine = true;
            System.out.print("Enter desired option: ");

            try {
                //read user input
                String optionAsString = br.readLine();

                //chop into chararray
                char[] optionAsCharArray = optionAsString.toCharArray();
                if (optionAsCharArray.length > 3) {
                    System.out.println("Bad format!");
                    fine = false;
                }

                if (fine) {
                    if (optionAsCharArray.length == 0) {
                        return new String[0];
                    }
                    userOption = new String[optionAsCharArray.length];

                    //set positions of array
                    for (int i = 0; i < userOption.length; i++) {
                        if (charToName(optionAsCharArray[i]).equals("unknown")) {
                            printUserInformation("No company: " + optionAsCharArray[i]);
                            fine = false;
                        }
                        userOption[i] = charToName(optionAsCharArray[i]);
                    }
                }
            } catch (Exception ex) {
                System.out.println("Bad format!");
                fine = false;
            }
        }

        return userOption;
    }

    /**
     * returns two int numbers (array of length 2)
     *
     * @return
     */
    public int[] askForShareOperation(String playerName, int posession) {
        System.out.println("Enter two numbers. First is convert amount, second is sell amount:");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        int[] numbers = new int[2];

        boolean fine = false;
        while (!fine) {
            System.out.print(playerName + " (" + posession + ") " + ": ");
            try {
                String inputString = br.readLine();
                String[] arguments = inputString.split(" ");
                if (arguments.length != 2) {
                    continue;
                }

                for (int i = 0; i < numbers.length; i++) {
                    numbers[i] = Integer.parseInt(arguments[i]);
                }

                fine = true;
            } catch (NumberFormatException nfe) {
            } catch (IOException ioe) {
            }
        }

        return numbers;
    }

    private boolean isInt(String unknown) {
        try {
            Integer.parseInt(unknown);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    private int charToInt(char xPosition) {
        switch (xPosition) {
            case ('a'):
                return 0;
            case ('b'):
                return 1;
            case ('c'):
                return 2;
            case ('d'):
                return 3;
            case ('e'):
                return 4;
            case ('f'):
                return 5;
            case ('g'):
                return 6;
            case ('h'):
                return 7;
            case ('i'):
                return 8;
            case ('j'):
                return 9;
            case ('k'):
                return 10;
            case ('l'):
                return 11;
        }

        throw new RuntimeException("Char could not be mapped to position");
    }

    private String charToName(char letter) {
        if (letter == 'w') {
            return "Worldwide";
        } else {
            if (letter == 's' || letter == 'S') {
                return "Sackson";
            } else {
                if (letter == 'f' || letter == 'F') {
                    return "Festival";
                } else {
                    if (letter == 'i' || letter == 'I') {
                        return "Imperial";
                    } else {
                        if (letter == 'a' || letter == 'A') {
                            return "American";
                        } else {
                            if (letter == 'c' || letter == 'C') {
                                return "Continental";
                            } else {
                                if (letter == 't' || letter == 'T') {
                                    return "Tower";
                                } else {
                                    return "unknown";
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * use this method to print the final balance data
     */
    public void gameOver(PlayerInterface[] players) {
        System.out.println("\n\n\nFinal scores:\n");
        System.out.print("            ");
        for (int i = 0; i < players.length; i++) {
            System.out.printf("%12s", players[i].getName());
        }
        System.out.print("\nCredit      ");
        for (int i = 0; i < players.length; i++) {
            System.out.printf("%12d", players[i].getBalance());
        }
        System.out.println("");
    }

    public void printInfo(int roundCounter, String playerName, CompanyInterface[] companies, ShareCollectionInterface masterShares, PlayerInterface[] players, BoardInterface hotelBoard) {
        printRoundInfo(roundCounter);
        printTurnInfo(playerName);
        printData(companies, masterShares, players);
        System.out.println(hotelBoard);
    }
}
