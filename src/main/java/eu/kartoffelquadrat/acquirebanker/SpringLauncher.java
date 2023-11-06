package eu.kartoffelquadrat.acquirebanker;

import static eu.kartoffelquadrat.acquirebanker.Main.main;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringLauncher {

  // This literally just starts up the old launcher.
  public static void main(String[] args) {
    Main.main(args);
  }

}
