import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class GestionUtilisateur implements Runnable {
    private Socket sock;

    public GestionUtilisateur(Socket s) {
        sock = s;
    }

    private void analyse(String demande, PrintWriter reponseServeur, ObjectOutputStream oos) {
        String sender, code;
        try {
            sender = demande.split(":")[0];
            code = demande.split(":")[1];
            switch (code) {

                case "101": // 101 : création du compte client + connexion
                    if (Serveur.getClient(sender) != null) {
                        reponseServeur.println("201:Client existant");
                    } else {
                        Client nouveau = new Client();
                        nouveau.setTel(sender);
                        Serveur.addClient(nouveau);
                        reponseServeur.println("200:Inscription réussie");
                        System.out.println(sender + " inscrit");
                    }
                    break;

                case "001": // 001 : connexion
                    Client c = Serveur.getClient(sender);
                    if (c != null) {
                        String prenom = (c.getPrenom() == null) ? "" : c.getPrenom();
                        String nom = (c.getNom() == null) ? "" : c.getNom();
                        reponseServeur.println("200:" + nom + ":" + prenom);
                        System.out.println(sender + " connecté");
                    } else {
                        reponseServeur.println("202:Client introuvable");
                    }
                    break;

                case "300": // 300 : demande liste client

                    oos.writeObject(Serveur.getAllClient());
                    oos.flush();
                    break;

                case "500":
                    System.out.println("Pour " + demande.split(":")[2] + " : " + demande.split(":")[3]);
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
                PrintWriter reponseServeur = new PrintWriter(sock.getOutputStream(), true);
                ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());) {
            String ligne, sender = "";
            while ((ligne = demandeClient.readLine()) != null) {
                sender = ligne.split(":")[0];
                analyse(ligne, reponseServeur, oos);
            }
            System.out.println(sender + " déconnexion");
        } catch (IOException ioe) {
            System.out.println("run erreur " + ioe.getMessage());
        }
    }

}
