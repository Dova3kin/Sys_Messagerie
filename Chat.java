import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class Chat {
    private Client c;
    public static Boolean active = false;

    public Chat(Client c) {
        this.c = c;
    }

    public void openChat() {
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
                        active = true;
                        String destinataire = discussion.get(Integer.parseInt(input) - 1);
                        c.sendPaquet("301", destinataire);
                        System.out.println("Chat avec " + destinataire + "\n---------------------------\n\n");
                        chargerChat(destinataire);
                        while (!input.equals(":R")) {
                            System.out.print(">");
                            input = scanner.nextLine();

                            if (input.startsWith("fichier/")) {
                                byte[] fichier = Files
                                        .readAllBytes(Paths.get("CLIENT/" + c.getTel() + "/" + input.substring(8)));
                                Message<byte[]> msgFichier = new Message<>(destinataire, c.getTel(), c.getPrenom(),
                                        fichier);
                                c.sendPaquet("501", msgFichier);
                                Message<String> msg = new Message<>(destinataire, c.getTel(), c.getPrenom(),
                                        "fichier reçu");
                                c.sendPaquet("500", msg);
                                c.addMessage(destinataire, msg);

                            } else if (!input.equals(":R")) {
                                Message<String> msg = new Message<>(destinataire, c.getTel(), c.getPrenom(), input);
                                c.sendPaquet("500", msg);
                                c.addMessage(destinataire, msg);
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

                            if (input.startsWith("fichier/")) {
                                byte[] fichier = Files
                                        .readAllBytes(Paths.get("CLIENT/" + c.getTel() + "/" + input.substring(8)));
                                Message<byte[]> msgFichier = new Message<>(destinataire, c.getTel(), c.getPrenom(),
                                        fichier);
                                c.sendPaquet("501", msgFichier);
                                Message<String> msg = new Message<>(destinataire, c.getTel(), c.getPrenom(),
                                        "fichier reçu");
                                c.sendPaquet("500", msg);
                                c.addMessage(destinataire, msg);

                            } else if (!input.equals(":R")) {
                                Message<String> msg = new Message<>(destinataire, c.getTel(), c.getPrenom(), input);
                                c.sendPaquet("500", msg);
                                c.addMessage(destinataire, msg);
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
        ArrayList<Message<?>> chat = c.getConv().get(destinataire);
        for (Message<?> msg : chat) {
            if (msg.getMessage() instanceof String) {
                if (msg.getEnvoyeur().equals(destinataire))
                    System.out.println(Couleur.BLEU + msg.getId() + Couleur.RESET + ": " + msg.getMessage());
                else {
                    if (msg.getMessage().equals("Fichier reçu"))
                        System.out.println(">Fichier envoyé");
                    else
                        System.out.println(">" + msg.getMessage());
                }
            } else {
                try {
                    Files.write(Paths.get("CLIENT/" + c.getTel() + "image_recu.jpg"), (byte[]) msg.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
