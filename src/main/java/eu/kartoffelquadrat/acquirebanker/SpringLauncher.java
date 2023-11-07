package eu.kartoffelquadrat.acquirebanker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpringLauncher {

  public static String version = "0.0.4";

  // This substitutes the LocalMainLauncher
  public static void main(String[] args) {

    ConfigurableApplicationContext context = SpringApplication.run(SpringLauncher.class, args);

//    new Thread(() -> {
    //clear screen
    System.out.print(((char) 27) + "[2J" + ((char) 27) + "[H");
    System.out.println("\u001B[31;1m Acquire - " + version + " \u001B[m");

    //start either new game with the info as param that it is a new game or
    //restart an old game with the information, that the game is to be continued
    //- this is relevant to the behaviour of the round counter
    GameManager manager = context.getBean(GameManager.class);
    if (args.length == 0) {
      manager.initGame();
    } else {
      System.out.println("loading game");
      manager.loadGame(args[0]);
    }
    manager.runCLI();
  }


}
