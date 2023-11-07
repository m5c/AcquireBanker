package eu.kartoffelquadrat.acquirebanker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameManager {

  public static String savePath = System.getProperty("java.io.tmpdir");//"user.home");//
  public static int initialSharesPerCompany = 25;
  public static int numberOfCompanies = 7;
  public static int finalLimit = 41;
  private Game game;

  public GameManager() {
  }

  public void initGame() {
    game = new Game(true);
  }

  public void loadGame(String savegame) {
    game = Game.load(savegame);
  }

  public void runCLI() {
    System.out.println("Games are saved to: "+savePath);
    // Run the eternal command line loop
    game.continueGame(true);
  }


  @GetMapping("/shares/{player}")
  public String getPlayerActions(@PathVariable int player) {
    StringBuilder playerPrivateInfoString =
        new StringBuilder(game.getPlayers()[player].getName());
    playerPrivateInfoString.append("<br/>----------<br/>Total Shares: ").append(game.getPlayers()[player].getNumberOfShares()).append("<br/>----------");

    String[] companyNames = game.getCompanyNames();
    for (int i = 0; i < companyNames.length; i++) {
      playerPrivateInfoString.append("<br/>").append(companyNames[i]).append(": ").append(game.getPlayers()[player].getNumberOfShares(i));
    }

    return playerPrivateInfoString.toString();
  }
}
