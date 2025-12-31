import java.io.Serializable;

public class Message implements Serializable {
    private String destinataire, envoyeur;
    private Object message;

    public Message(String destinataire, String envoyeur, Object message) {
        this.destinataire = destinataire;
        this.envoyeur = envoyeur;
        this.message = message;
    }

    public String getDestinataire() {
        return destinataire;
    }

    public Object getMessage() {
        return message;
    }

    public String getEnvoyeur() {
        return envoyeur;
    }
}
