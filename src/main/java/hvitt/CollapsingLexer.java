package hvitt;

public class CollapsingLexer implements Lexer {
    private static enum State {
        S, NL
    }

    private final Lexer lexer;
    private final LexerConfig cfg;
    private boolean first = true;
    private final boolean trim;
    private Token currentToken = null;
    private State state = State.S;

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

        while (true) {
            switch (state) {
                case S:
                    if (cfg.newLineKey.equals(lexer.peek().key)) {
                        state = State.NL;
                    } else {
                        first = false;
                        return lexer.pop();
                    }
                case NL:
                    Token tk = null;
                    while (cfg.newLineKey.equals(lexer.peek().key)) {
                        tk = lexer.pop();
                    }

                    if (tk == null) {
                        state = State.S;
                        first = false;
                        return lexer.pop();
                    } else {
                        if ((trim && first)
                                || (trim && cfg.eofKey.equals(lexer.peek().key))
                                || (cfg.indentKey.equals(lexer.peek().key)
                                || cfg.deindentKey.equals(lexer.peek().key))) {
                            state = State.S;
                            first = false;
                            return lexer.pop();
                        } else {
                            state = State.S;
                            first = false;
                            return tk;
                        }
                    }
            }
        }
    }

    public String getRawLine() {
        return lexer.getRawLine();
    }

    public String getCurrentColIndentString() {
        return lexer.getCurrentColIndentString();
    }
}
