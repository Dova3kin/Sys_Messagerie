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
                        }
                        break;

                    case "300_REP": // Réponse à la demande de liste
                        ArrayList<Client> liste = (ArrayList<Client>) p.contenu;
                        client.setAllClient(liste); //
                        break;

                    case "MSG_RECU": // Un vrai message de chat
                        System.out.println("\n" + p.contenu);
                        break;
                }
            }
        } catch (Exception e) {
            /* ... */ }
    }
}