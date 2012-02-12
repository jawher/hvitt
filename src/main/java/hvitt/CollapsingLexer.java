package hvitt;

public class CollapsingLexer implements Lexer {
    private final Lexer lexer;
    private final LexerConfig cfg;
    private boolean first = true;
    private final boolean trim;
    private Token currentToken = null;

    public CollapsingLexer(Lexer lexer, LexerConfig cfg, boolean trim) {
        this.lexer = lexer;
        this.cfg = cfg;
        this.trim = trim;
    }

    public Token peek() {
        if (currentToken == null) {
            currentToken = pop();
        }
        return currentToken;
    }

    public Token pop() throws LexingException {
        if (currentToken != null) {
            Token res = currentToken;
            currentToken = null;
            return res;
        }
        Token tk = null;
        while (cfg.newLineKey.equals(lexer.peek().key)) {
            tk = lexer.pop();
        }
        if (tk != null && (!trim || (!first && !cfg.eofKey.equals(lexer.peek().key)))) {
            first = false;
            return tk;
        } else {
            return lexer.pop();
        }
    }

    public String getRawLine() {
        return lexer.getRawLine();
    }

    public String getCurrentColIndentString() {
        return lexer.getCurrentColIndentString();
    }
}
