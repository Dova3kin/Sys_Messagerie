import java.io.BufferedReader;
import java.io.IOException;

public class LecteurMessages implements Runnable {
    private BufferedReader in;

    public LecteurMessages(BufferedReader in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            String message;
            // Cette boucle attend les messages du serveur sans bloquer le reste du
            // programme
            while ((message = in.readLine()) != null) {
                System.out.println("\n[MESSAGE REÇU] : " + message);
                System.out.print("> "); // Réaffiche le curseur pour l'utilisateur
            }
        } catch (IOException ioe) {
            System.out.println("Connexion fermée par le serveur.");
        }
    }
}