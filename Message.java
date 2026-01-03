import java.io.Serializable;

public class Message<T> implements Serializable {
    private String destinataire, envoyeur, id;
    private T message;

    public Message(String destinataire, String envoyeur, String id, T message) {
        this.destinataire = destinataire;
        this.envoyeur = envoyeur;
        this.id = id;
        this.message = message;
    }

    public String getDestinataire() {
        return destinataire;
    }

    public T getMessage() {
        return message;
    }

    public String getEnvoyeur() {
        return envoyeur;
    }

    public String getId() {
        return (!id.equals("")) ? id : envoyeur;
    }

}
