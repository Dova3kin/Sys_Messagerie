import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class LecteurMessages implements Runnable {
    private Client client;

    public LecteurMessages(Client c) {
        client = c;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Paquet p = (Paquet) client.getResponseServeru().readObject();
                switch (p.code) {
                    case "101_REP":
                        client.setCodeInscriptionRecu((String) p.contenu);
                        break;
                    case "001_REP": // Réponse à la demande d'infos (link)
                        if (p.contenu != null) {
                            Client infos = (Client) p.contenu;
                            client.setTel(infos.getTel());
                            client.setPrenom(infos.getPrenom());
                            client.setNom(infos.getNom());
                            client.setConv(infos.getConv());
                            client.setNotif(infos.getNotifs());
                        }
                        break;

                    case "102_NOTIF":
                        client.addNotif((String) p.contenu);
                        System.out.println(
                                "\r" + Couleur.ROUGE + "nouvelle notif " + Couleur.RESET);
                        System.out.print(">");
                        break;

                    case "102_NOTIF_SANS_NOTIF":
                        client.addNotif((String) p.contenu);
                        break;

                    case "300_REP": // Réponse à la demande de liste
                        @SuppressWarnings("unchecked")
                        ArrayList<Client> liste = (ArrayList<Client>) p.contenu;
                        client.setAllClient(liste); //
                        break;

                    case "404": // Erreur
                        System.out.println(p.contenu);
                        System.exit(0);

                    case "500_REP": // Un message de chat
                        Message<String> msg = (Message<String>) p.contenu;
                        client.addConv(msg.getEnvoyeur());
                        client.addMessage(msg.getEnvoyeur(), msg);
                        if (Chat.active.equals(msg.getEnvoyeur())) {
                            System.out.println(
                                    "\r" + Couleur.BLEU + msg.getId() + Couleur.RESET + ":" +
                                            msg.getMessage());
                            System.out.print(">");
                        }
                        break;

                    case "501_REP":
                        Message<byte[]> msgFichier = (Message<byte[]>) p.contenu;
                        byte[] fichier = msgFichier.getMessage();
                        Files.write(Paths.get("CLIENT/" + client.getTel() + "/image_recu.jpg"), fichier);
                        break;
                }
            }
        } catch (

        Exception e) {
            /* ... */ }
    }
}