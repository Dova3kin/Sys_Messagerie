import java.util.ArrayList;
import java.util.Scanner;

public class Chat {
    Client c;

    public Chat(Client c) {
        this.c = c;
    }

    public void openChat() {
    }

    public void createChat() {
        ArrayList<Client> clientsSansChat = new ArrayList<>(); // Liste des clients avec qui le client "c" n'a jamais
                                                               // parlé
        c.sendRequest("300");
        try {
            @SuppressWarnings("unchecked")
            ArrayList<Client> clients = (ArrayList<Client>) c.getObjectResponse().readObject();
            for (Client c : clients) {
                if (!this.c.getConv().contains(c.getTel()) && !c.getTel().equals(this.c.getTel())) {
                    clientsSansChat.add(c);
                }
            }

            System.out.println("=====Liste Utilisateur=====");
            int i = 1;
            for (Client client : clientsSansChat) {
                System.out.println(i++ + " : " + client);
            }
            System.out.println(i + " : " + "Retour");

            Scanner scanner = new Scanner(System.in);
            String input = "";
            input = scanner.nextLine();
            if (!input.equals(i + "")) {
                Client recepteur = clientsSansChat.get(Integer.parseInt(input) - 1);
                System.out.println("Chat avec " + recepteur);
                while (!input.equals(":QUIT")) {
                    if (c.getResponse() == null) {
                        input = scanner.nextLine();
                        c.sendRequest("500", recepteur, input);
                    } else {
                        System.out.println(recepteur.getPrenom() + " : " + c.getResponse());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("createChat error : " + e.getMessage());
        }
    }

}
