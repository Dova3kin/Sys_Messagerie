import java.util.ArrayList;
import java.util.Scanner;

public class Chat {
    private Client c;
    private String id;

    public Chat(Client c) {
        this.c = c;
        id = (c.getPrenom() != "") ? c.getPrenom() : c.getTel();
    }

    public void openChat() {
    }

    public void createChat() {
        ArrayList<Client> clientsSansChat = new ArrayList<>(); // Liste des clients avec qui le client "c" n'a jamais
                                                               // parlé
        c.sendPaquet("300");
        try {
            int timeout = 0;
            while (c.getAllClient() == null & timeout < 30) {
                Thread.sleep(100);
                timeout++;
            }

            if (c.getAllClient() != null) {
                for (Client c : c.getAllClient()) {
                    if (!this.c.getConv().contains(c.getTel()) && !c.getTel().equals(this.c.getTel())) {
                        clientsSansChat.add(c);
                    }
                }
                System.out.println("=====Liste Utilisateur=====");
                int i = 1;
                for (Client client : clientsSansChat) {
                    System.out.println(i++ + " : " + client);
                }
                System.out.println(":R -> Retour");
                System.out.print(">");
                c.setAllClient(null);

                Scanner scanner = new Scanner(System.in);
                String input = "";
                input = scanner.nextLine();
                if (!input.equals(":R")) {
                    String destinataire = clientsSansChat.get(Integer.parseInt(input) - 1).getTel();
                    System.out.println("Chat avec " + destinataire);
                    while (!input.equals(":R")) {
                        System.out.print(">");
                        input = scanner.nextLine();
                        c.sendPaquet("500", new Message(destinataire, id, input));

                    }
                }
            }

        } catch (Exception e) {
            System.out.println("createChat error : " + e.getMessage());
        }
    }

}
