import java.net.*;
import java.util.Scanner;

class Serveur {

    public static void main(String args[]) {
        Socket sock = null;
        try (ServerSocket server = new ServerSocket(7770)) {
            System.out.println("Démarage du serveur...");
            Thread.sleep(1000);
            System.out.println("Serveur démaré");

            while (true) {
                sock = server.accept();
                System.out.println("Tentative de connexion...");
                Thread t = new Thread(new GestionUtilisateur(sock));
                t.start();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}