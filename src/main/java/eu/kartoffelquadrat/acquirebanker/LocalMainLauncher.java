package eu.kartoffelquadrat.acquirebanker;

/**
 * this softwoare is intended to support acquire board game geeks to accelerating the games' managament process
 *
 * @author Max Schiedermeier (c) 2011
 */
public class LocalMainLauncher {

    /**
     * the main class will handle the game commander interaction and care about to comunication with the games logic
     * components
     */
    public static String version = "0.0.3";
    public static String savePath = System.getProperty("java.io.tmpdir");//"user.home");//
    public static int initialSharesPerCompany = 25;
    public static int numberOfCompanies = 7;
    public static int finalLimit = 41;

    public static void main(String[] args) {
        //clear screen
        System.out.print(((char) 27) + "[2J" + ((char) 27) + "[H");
        System.out.println("\u001B[31;1m Acquire - " + version + " \u001B[m");


        //start either new game with the info as param that it is a new game or
        //restart an old game with the information, that the game is to be continued
        //- this is relevant to the behaviour of the round counter
        (args.length == 0 ? new Game(true) : Game.load(args[0])).continueGame(args.length != 0);
    }
}
