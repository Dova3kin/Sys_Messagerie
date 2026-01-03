import java.io.IOException;
import java.util.Scanner;

public class App {

    private static void wait(int i) {
        try {
            Thread.sleep(i);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        Client c = new Client();
        Scanner scanner = new Scanner(System.in);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            c.push();
            try {
                c.getSocket().close();
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
            scanner.close();
        }));
        new Connexion(c);
        System.out.println("Bienvenue " + (c.getPrenom() != null ? c.getPrenom() : ""));
        wait(500);
        c.openAccueil(scanner);
    }
}
