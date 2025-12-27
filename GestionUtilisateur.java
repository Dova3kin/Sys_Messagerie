import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

public class GestionUtilisateur implements Runnable {
    private Socket sock;
    private static final Object lock = new Object();

    public GestionUtilisateur(Socket s) {
        sock = s;
    }

    private static void analyse(String demande, PrintWriter reponseServeur) {
        String sender, code;
        try {
            sender = demande.split(":")[0];
            code = demande.split(":")[1];
            switch (code) {
                case "101": // 101 : création du compte client + connexion
                    boolean clientExist = false;
                    synchronized (lock) {

                        /**
                         * Vérification de l'existance du chemin jusqu'au dossier
                         */
                        Path path = Paths.get("SERVEUR/clients.txt");
                        if (!Files.exists(path)) {
                            if (path.getParent() != null)
                                Files.createDirectories(path.getParent());
                            Files.createFile(path);
                        }

                        /**
                         * Rechercher du client dans le fichier
                         */
                        try (Stream<String> lignes = Files.lines(Paths.get("SERVEUR/clients.txt"))) {
                            clientExist = lignes.anyMatch(ligne -> ligne.equals(sender));
                            if (clientExist)
                                reponseServeur.println("201:Client existant");
                            else {
                                Files.write(Paths.get("SERVEUR/clients.txt"), (sender + "\n").getBytes(),
                                        StandardOpenOption.CREATE,
                                        StandardOpenOption.APPEND);
                                reponseServeur.println("200:Connexion réussie");
                                System.out.println(sender + " connecté");
                            }
                        } catch (NoSuchFileException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    break;
                case "001": // 001 : connexion
                    try (Stream<String> lignes = Files.lines(Paths.get("SERVEUR/clients.txt"))) {
                        clientExist = lignes.anyMatch(ligne -> ligne.equals(sender));
                        if (clientExist)
                            reponseServeur.println("200:Connexion réussie");
                        else
                            reponseServeur.println("202:Client introuvable");
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    System.out.println(sender + " connecté");
                    break;
                default:
                    System.out.println("Code inconnu : " + code + " (" + sender + ")");
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
            String ligne;
            while ((ligne = demandeClient.readLine()) != null) {
                analyse(ligne, reponseServeur);
                if (ligne.contains(":QUIT")) // GESTION FUTURE POUR SE DÉCO//
                    break;
            }
            System.out.println(ligne.split(":")[0] + ":déconnexion");
        } catch (Exception e) {
            System.out.println("run erreur " + e.getMessage());
        }
    }

}
