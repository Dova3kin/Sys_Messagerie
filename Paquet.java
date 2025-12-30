import java.io.Serializable;

public class Paquet implements Serializable {
    private static final long serialVersionUID = 1L;
    public String code, sender;
    public Object contenu;

    public Paquet(String code) {
        this.code = code;
    }

    public Paquet(String code, Object contenu) {
        this.code = code;
        this.contenu = contenu;
    }

    public Paquet(String code, Object contenu, String sender) {
        this.code = code;
        this.contenu = contenu;
        this.sender = sender;
    }
}
