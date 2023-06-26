public class NodoVar {
    String lexema;
     String tipo;
     int token;
     NodoVar sig= null;

    public NodoVar(String lexema, String tipo,int token) {
        this.lexema = lexema;
        this.tipo = tipo;
        this.token = token;
    }
    
    public NodoVar(String lexema,int token) {
        this.lexema = lexema;
        this.token = token;
    }
}
