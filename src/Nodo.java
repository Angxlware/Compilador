



public class Nodo {
    int token, renglon;
    Nodo sig = null;
    String lexema;

    public Nodo(String lexema, int token, int renglon) {
        this.lexema = lexema;
        this.token = token;
        this.renglon = renglon;
    }
}
