import java.util.ArrayList;
import java.util.Scanner;

public class Chat {
    private Client c;
    private String id;
    public static Boolean active = false;

    public Chat(Client c) {
        this.c = c;
        id = (c.getPrenom() != "") ? c.getPrenom() : c.getTel();
    }

    public void openChat() {
        active = true;
        c.sendPaquet("300");
        try {
            int timeout = 0;
            while (c.getAllClient() == null && timeout < 30) {
                Thread.sleep(100);
                timeout++;
            }

            if (c.getAllClient() != null) {
                Scanner scanner = new Scanner(System.in);
                String input = "";

                while (!input.equals(":R")) {
                    System.out.println("=====Liste Utilisateur=====");
                    int i = 1;
                    ArrayList<String> discussion = new ArrayList<>();
                    for (Client client : this.c.getAllClient()) {
                        if (c.getConv().containsKey(client.getTel())) {
                            discussion.add(client.getTel());
                            System.out.println(i++ + ":" + client);
                        }
                    }

                    System.out.println(":R -> Retour");
                    System.out.print(">");

                    input = scanner.nextLine();
                    if (!input.equals(":R")) {
                        String destinataire = discussion.get(Integer.parseInt(input) - 1);
                        c.sendPaquet("301", destinataire);
                        System.out.println("Chat avec " + destinataire + "\n---------------------------\n\n");
                        chargerChat(destinataire);
                        while (!input.equals(":R")) {
                            System.out.print(">");
                            input = scanner.nextLine();
                            if (!input.equals(":R")) {
                                Message msg = new Message(destinataire, id, input);
                                c.sendPaquet("500", msg);
                                c.getConv().get(destinataire).add(msg);
                            }
                        }
                        input = "";
                    }
                }
                c.setAllClient(null);
            }
        } catch (Exception e) {
            System.out.println("Open Chat error : ");
            e.printStackTrace();
        } finally {
            active = false;
        }

    }

    public void createChat() {
        active = true;
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
                Scanner scanner = new Scanner(System.in);
                String input = "";

                while (!input.equals(":R")) {
                    System.out.println("=====Liste Utilisateur=====");
                    int i = 1;
                    for (Client c : c.getAllClient()) {
                        if (!this.c.getConv().containsKey(c.getTel()) && !c.getTel().equals(this.c.getTel())) {
                            System.out.println(i++ + " : " + c);
                            clientsSansChat.add(c);
                        }
                    }

                    System.out.println(":R -> Retour");
                    System.out.print(">");

                    input = scanner.nextLine();
                    if (!input.equals(":R")) {
                        String destinataire = clientsSansChat.get(Integer.parseInt(input) - 1).getTel();
                        System.out.println("Chat avec " + destinataire + "\n---------------------------\n\n");
                        c.addConv(destinataire);
                        while (!input.equals(":R")) {
                            System.out.print(">");
                            input = scanner.nextLine();
                            if (!input.equals(":R")) {
                                Message msg = new Message(destinataire, id, input);
                                c.sendPaquet("500", msg);
                                c.getConv().get(destinataire).add(msg);
                            }
                        }
                        input = "";
                    }
                }
                c.setAllClient(null);
            }
        } catch (Exception e) {
            System.out.println("createChat error : " + e.getMessage());
        } finally {
            active = false;
        }
    }

    public void chargerChat(String destinataire) {
        ArrayList<Message> chat = c.getConv().get(destinataire);
        for (Message msg : chat) {
            if (msg.getEnvoyeur().equals(destinataire))
                System.out.println(Couleur.BLEU + msg.getEnvoyeur() + Couleur.RESET + ": " + msg.getMessage());
            else
                System.out.println(">" + msg.getMessage());
        }
    }

}
