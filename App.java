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
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            c.push();
            try {
                c.getSocket().close();
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        }));
        new Connexion(c);
        System.out.println("Bienvenue " + (c.getPrenom() != null ? c.getPrenom() : ""));
        wait(500);
        Scanner scanner = new Scanner(System.in);
        String input = "";
        while (!input.equals("4")) {
            System.out.println("==========ACCUEIL==========");
            System.out.println("1 : Messages");
            System.out.println("2 : Notifications");
            System.out.println("3 : Options");
            System.out.println("4 : Déconnexion");
            System.out.print(">");
            input = scanner.nextLine();
            switch (input) {
                case "1":
                    c.message();
                    break;
                case "2":
                    break;
                case "3":
                    break;
                case "4":
                    break;
                default:
                    break;
            }
        }
    }
}
