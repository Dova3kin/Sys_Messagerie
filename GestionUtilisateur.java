import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GestionUtilisateur implements Runnable {
    private Socket sock;
    private String telClient;
    private static Map<String, ObjectOutputStream> clientsCo = new ConcurrentHashMap<>();

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
                        System.out.println(telInscription + " inscrit/connecté");
                        telClient = telInscription;
                        clientsCo.put(nouveau.getTel(), reponseServeur);
                    }
                    reponseServeur.reset();
                    break;

                case "001": // Connexion
                    String telConnexion = (String) demande.contenu;
                    Client c = Serveur.getClient(telConnexion);
                    if (c != null) {
                        reponseServeur.writeObject(new Paquet("001_REP", c));
                        telClient = c.getTel();
                        clientsCo.put(c.getTel(), reponseServeur);
                        System.out.println(telConnexion + " connecté");
                    } else
                        reponseServeur.writeObject(new Paquet("001_REP", null));
                    reponseServeur.reset();
                    break;

                case "102": // Mise a jour des données client côté serveurs
                    Client clientAJour = (Client) demande.contenu;
                    Serveur.updateClient(telClient, clientAJour);
                    System.out.println(telClient + " mis à jour");
                    break;
                case "300": // Liste des clients
                    reponseServeur.writeObject(new Paquet("300_REP", Serveur.getAllClient()));
                    break;
                case "500": // Envoi de message
                    Message msg = (Message) demande.contenu;
                    ObjectOutputStream destinaire = clientsCo.get(msg.getDestinataire());
                    if (destinaire != null) {
                        Paquet p = new Paquet("500_REP", msg);
                        synchronized (destinaire) {
                            destinaire.writeObject(p);
                            destinaire.flush();
                            destinaire.reset();
                        }
                    }
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
            clientsCo.remove(telClient);
        }
    }

}
