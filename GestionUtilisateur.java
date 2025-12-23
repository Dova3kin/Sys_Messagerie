import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

public class GestionUtilisateur implements Runnable {
    private Socket sock;

    public GestionUtilisateur(Socket s) {
        sock = s;
    }

    private static void analyse(BufferedReader demandeClient, PrintWriter reponseServeur) {
        String sender, code;
        try {
            String demande = demandeClient.readLine();
            sender = demande.split(":")[0];
            code = demande.split(":")[1];
            switch (code) {
                case "101": // 101 : création du compte client + connexion
                    boolean clientExist = false;

                    try (Stream<String> lignes = Files.lines(Paths.get("SERVEUR/clients.txt"))) {
                        clientExist = lignes.anyMatch(ligne -> ligne.split(":")[0].equals(sender));
                        if (clientExist)
                            reponseServeur.println("201:Client existant");
                        else {
                            Files.write(Paths.get("SERVEUR/clients.txt"), (sender + "\n").getBytes(),
                                    StandardOpenOption.CREATE,
                                    StandardOpenOption.APPEND);
                            reponseServeur.println("200:Connexion réussie");
                            System.out.println(sender + " connecté");
                        }
                    } catch (Exception E) {
                    }
                    break;
                case "001": // 001 : connexion
                    System.out.println(sender + " connecté");
                default:
                    reponseServeur.println("Demande invalide");
                    break;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());

        }
    }

    @Override
    public void run() {
        try (BufferedReader demandeClient = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                PrintWriter reponseServeur = new PrintWriter(sock.getOutputStream(), true);) {
            analyse(demandeClient, reponseServeur);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
