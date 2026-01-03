import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GestionUtilisateur implements Runnable {
    private Socket sock;
    private String telClient;
    private ObjectOutputStream reponseServeur;
    private static Map<String, ObjectOutputStream> clientsCo = new ConcurrentHashMap<>();

    public GestionUtilisateur(Socket s) {
        sock = s;
        Serveur.ajouterObservateur(this);
    }

    public void recevoirNotification(String message) {
        try {
            synchronized (reponseServeur) {
                if (reponseServeur != null) {
                    reponseServeur.writeObject(new Paquet("102_NOTIF", message));
                    reponseServeur.flush();
                    reponseServeur.reset();
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur envoi notification à " + telClient);
        }
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
                        Serveur.diffuserNotificationGlobale(telInscription + " vient de s'inscrire !", telInscription);
                    }
                    reponseServeur.reset();
                    break;

                case "001": // Connexion
                    String telConnexion = (String) demande.contenu;
                    Client c = Serveur.getClient(telConnexion);
                    if (c != null) {
                        if (clientsCo.get(telConnexion) != null) {
                            Paquet p = new Paquet("404", "Connexion déjà existante");
                            reponseServeur.writeObject(p);
                        } else {
                            reponseServeur.writeObject(new Paquet("001_REP", c));
                            telClient = c.getTel();
                            clientsCo.put(c.getTel(), reponseServeur);
                            System.out.println(telConnexion + " connecté");
                        }
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

                case "301": // Notif de lecture des messages
                    ObjectOutputStream receveur = clientsCo.get(demande.contenu);
                    if (receveur != null) { // s'il est connecté
                        Paquet p = new Paquet("102_NOTIF", telClient + " à vu les messages");
                        synchronized (receveur) {
                            receveur.writeObject(p);
                            receveur.flush();
                            receveur.reset();
                        }
                    } else {
                        Client client = Serveur.getClient((String) demande.contenu);
                        client.addNotif(telClient + " à vu les messages");
                        Serveur.saveAll();
                    }
                    break;

                case "500": // Envoi de message
                    Message<String> msg = (Message<String>) demande.contenu;
                    ObjectOutputStream destinataire = clientsCo.get(msg.getDestinataire());
                    if (destinataire != null) { // si le destinataire est connecté
                        Paquet p = new Paquet("500_REP", msg);
                        synchronized (destinataire) {
                            destinataire.writeObject(p);
                            destinataire
                                    .writeObject(
                                            new Paquet("102_NOTIF_SANS_NOTIF", "Nouveaux messages de " + telClient));
                            destinataire.flush();
                            destinataire.reset();
                        }
                    } else {
                        Client client = Serveur.getClient(msg.getDestinataire());
                        client.addMessage(telClient, msg);
                        client.addNotif("Nouveaux messages de " + telClient);
                        Serveur.saveAll();
                    }
                    break;

                case "501":
                    Message<byte[]> msgFichier = (Message<byte[]>) demande.contenu;
                    destinataire = clientsCo.get(msgFichier.getDestinataire());
                    if (destinataire != null) { // si le destinataire est connecté
                        Paquet p = new Paquet("501_REP", msgFichier);
                        synchronized (destinataire) {
                            destinataire.writeObject(p);
                            destinataire
                                    .writeObject(
                                            new Paquet("102_NOTIF_SANS_NOTIF", "Nouveaux messages de " + telClient));
                            destinataire.flush();
                            destinataire.reset();
                        }
                    } else {
                        Client client = Serveur.getClient(msgFichier.getDestinataire());
                        client.addMessage(telClient, msgFichier);
                        client.addNotif("Nouveaux messages de " + telClient);
                        Serveur.saveAll();
                    }
                    break;
                case "502":
                    msgFichier = (Message<byte[]>) demande.contenu;
                    destinataire = clientsCo.get(msgFichier.getDestinataire());
                    if (destinataire != null) { // si le destinataire est connecté
                        Paquet p = new Paquet("501_REP", msgFichier);
                        synchronized (destinataire) {
                            destinataire.writeObject(p);
                            destinataire.flush();
                            destinataire.reset();
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

    public static Map<String, ObjectOutputStream> getClientsCo() {
        return clientsCo;
    }

    public String getTelClient() {
        return telClient;
    }

    @Override
    public void run() {
        try (ObjectOutputStream reponseServeur = new ObjectOutputStream(sock.getOutputStream());
                ObjectInputStream requeteClient = new ObjectInputStream(sock.getInputStream())) {
            reponseServeur.flush();
            this.reponseServeur = reponseServeur;
            while (true) {
                Paquet paquetRecu = (Paquet) requeteClient.readObject();
                analyse(paquetRecu, reponseServeur);
            }
        } catch (Exception e) {
            System.out.println(telClient + " déconnecté");
            clientsCo.remove(telClient);
            Serveur.supprimerObservateur(this);
        }
    }

}
