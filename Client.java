import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Client implements Serializable {
    private static final long serialVersionUID = 1L;
    private String tel, nom, prenom, dossierPerso;
    // private ArrayList<String> conversations = new ArrayList<>(); // Numéro de
    // téléphone avec qui le client à déjà parlé
    private Map<String, ArrayList<Message>> conversations = new ConcurrentHashMap<>();
    private ArrayList<String> notificationsEnAttente = new ArrayList<>();
    private transient ArrayList<Client> allClient = null;
    private transient String codeInscriptionRecu = null;
    private transient Socket soc = null;
    private transient ObjectInputStream reponseServeur = null;
    private transient ObjectOutputStream requeteServeur = null;

    /**
     * @param tel
     * @throws ClientAlreadyExistException
     */
    public void setClient(String tel) throws ClientAlreadyExistException {
        this.tel = tel;
        this.dossierPerso = "CLIENT/" + tel;
        this.codeInscriptionRecu = null;

        try {
            // On envoie un Paquet avec le code 101 et le numéro de téléphone
            sendPaquet("101", tel);

            // Boucle d'attente
            int timeout = 0;
            while (codeInscriptionRecu == null && timeout < 30) {
                Thread.sleep(100);
                timeout++;
            }

            if (codeInscriptionRecu == null) {
                System.out.println("Le serveur ne répond pas.");
                return;
            }

            // Analyse de la réponse reçue par le thread
            if (codeInscriptionRecu.equals("200")) {
                Path path = Paths.get(dossierPerso);
                Files.createDirectories(path);
                System.out.println("Compte créé avec Succès");
            } else if (codeInscriptionRecu.equals("201")) {
                throw new ClientAlreadyExistException();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.out.println("Erreur lors de la création du dossier : " + e.getMessage());
        } finally {
            codeInscriptionRecu = null; // On nettoie pour la prochaine fois
        }
    }

    public void link(String tel) throws NotFoundClientException {
        this.tel = null;
        sendPaquet("001", tel);

        /**
         * Boucle d'attente : on attend que le Thread LecteurMessages reçoive le Paquet
         * "200" et mette à jour le tel.
         */
        int timeout = 0;
        while (this.tel == null && timeout < 10) {
            try {
                Thread.sleep(100);
                timeout++;
            } catch (InterruptedException e) {
            }
        }
        if (this.tel == null) {
            throw new NotFoundClientException(tel);
        }
    }

    public boolean connect() {
        try {
            soc = new Socket("localhost", 7770);
            requeteServeur = new ObjectOutputStream(soc.getOutputStream());
            requeteServeur.flush();
            reponseServeur = new ObjectInputStream(soc.getInputStream());

            Thread t = new Thread(new LecteurMessages(this));
            t.setDaemon(true);
            t.start();

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public synchronized void sendPaquet(String code) {
        try {
            Paquet p = new Paquet(code);
            requeteServeur.writeObject(p);
            requeteServeur.flush();
            requeteServeur.reset();
        } catch (IOException e) {
            System.out.println("Erreur d'envoi : " + e.getMessage());
        }
    }

    public synchronized void sendPaquet(String code, Object contenu) {
        try {
            Paquet p = new Paquet(code, contenu);
            requeteServeur.writeObject(p);
            requeteServeur.flush();
            requeteServeur.reset();
        } catch (IOException e) {
            System.out.println("Erreur d'envoi : " + e.getMessage());
        }
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
        this.dossierPerso = "CLIENT/" + tel;
    }

    public String getPrenom() {
        return (prenom == null) ? "" : prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return (nom == null) ? "" : nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void addConv(String tel) {
        conversations.putIfAbsent(tel, new ArrayList<>());
    }

    public Map<String, ArrayList<Message>> getConv() {
        return conversations;
    }

    public void setConv(Map<String, ArrayList<Message>> conversations) {
        this.conversations = conversations;
    }

    public void setAllClient(ArrayList<Client> liste) {
        this.allClient = liste;
    }

    public ArrayList<Client> getAllClient() {
        return allClient;
    }

    public void setCodeInscriptionRecu(String code) {
        this.codeInscriptionRecu = code;
    }

    public ObjectInputStream getResponseServeru() {
        return reponseServeur;
    }

    public void ajouterNotif(String msg) {
        notificationsEnAttente.add(msg);
    }

    public ArrayList<String> getNotifs() {
        return notificationsEnAttente;
    }

    public void viderNotifs() {
        notificationsEnAttente.clear();
    }

    public Socket getSocket() {
        return soc;
    }

    public void push() {
        sendPaquet("102", this);
    }

    public void message() {
        Scanner scanner = new Scanner(System.in);
        String input = "";
        while (!input.equals("3")) {
            System.out.println("==========MESSAGE==========");
            System.out.println("1 : Conversations");
            System.out.println("2 : Nouvelles conversations");
            System.out.println("3 : Retour\n");
            System.out.print(">");
            input = scanner.nextLine();
            Chat chat = new Chat(this);
            switch (input) {
                case "1":
                    chat.openChat();
                    break;
                case "2":
                    chat.createChat();
                    break;
                case "3":
                    break;
                default:
                    break;
            }
        }
    }

    public void openNotifs() {
        for (String notif : notificationsEnAttente) {
            System.out.println("-" + notif);
        }
        viderNotifs();
    }

    @Override
    public String toString() {
        return tel + (prenom != null ? " " + prenom : "") + " " + (nom != null ? nom : "");
    }

}
