import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class GestionUtilisateur implements Runnable {
    private Socket sock;
    private String telClient;

    public GestionUtilisateur(Socket s) {
        sock = s;
    }

    private void analyse(Paquet demande, ObjectOutputStream reponseServeur) {
        try {
            switch (demande.code) {
                case "101": // Inscription + connexion
                    String telInscription = (String) demande.contenu;
                    if (Serveur.getClient(telInscription) != null) {
                        reponseServeur.writeObject(new Paquet("101_REP", "201"));
                    } else {
                        Client nouveau = new Client();
                        nouveau.setTel(telInscription);
                        Serveur.addClient(nouveau);
                        reponseServeur.writeObject(new Paquet("101_REP", "200"));
                        System.out.println(telInscription + " inscrit");
                        telClient = telInscription;
                    }
                    break;

                case "001": // Connexion
                    String telConnexion = (String) demande.contenu;
                    Client c = Serveur.getClient(telConnexion);
                    if (c != null) {
                        reponseServeur.writeObject(new Paquet("001_REP", c));
                        telClient = c.getTel();
                    } else
                        reponseServeur.writeObject(new Paquet("001_REP", null));
                    break;

                case "300": // Liste des clients
                    reponseServeur.writeObject(new Paquet("300_REP", Serveur.getAllClient()));
                    break;
            }
            reponseServeur.flush();
            reponseServeur.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try (ObjectOutputStream reponseServeur = new ObjectOutputStream(sock.getOutputStream());
                ObjectInputStream requeteClient = new ObjectInputStream(sock.getInputStream())) {
            reponseServeur.flush();
            while (true) {
                Paquet paquetRecu = (Paquet) requeteClient.readObject();
                analyse(paquetRecu, reponseServeur);
            }
        } catch (Exception e) {
            System.out.println(telClient + " déconnecté");
        }
    }

}
