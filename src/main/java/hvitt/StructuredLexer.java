package hvitt;

import java.util.Deque;
import java.util.LinkedList;

public class StructuredLexer implements Lexer {
    private final Lexer lexer;
    private final LexerConfig cfg;
    private final String indentUnit;

    private int lastIndent = 0;
    private Deque<Token> tokens = new LinkedList<Token>();

    public StructuredLexer(Lexer lexer, LexerConfig cfg, String indentUnit) {
        this.lexer = lexer;
        this.cfg = cfg;
        this.indentUnit = indentUnit;
    }

    public Token peek() {
        if (!tokens.isEmpty()) {
            return tokens.peek();
        } else {
            Token tk = pop();
            tokens.addFirst(tk);
            return tk;
        }
    }

    public Token pop() throws LexingException {
        if (!tokens.isEmpty()) {
            return tokens.poll();
        }
        Token tk = lexer.pop();
        if (cfg.indentKey.equals(tk.key)) {
            int iSize = indentSize(tk.text);
            if (iSize == -1 || iSize - lastIndent > 1) {
                throw new LexingException("Invalid indent", lexer.getRawLine(), tk.row, tk.col, lexer.getCurrentColIndentString());
            }
            lastIndent = iSize;
        } else if (cfg.deindentKey.equals(tk.key)) {
            int diSize = indentSize(tk.text);
            if (diSize == -1) {
                throw new LexingException("Invalid deindent", lexer.getRawLine(), tk.row, tk.col, lexer.getCurrentColIndentString());
            }
            if (lastIndent - diSize > 1) {
                //produce virtual deindents
                for (int i = 0; i < lastIndent - diSize; i++) {
                    tokens.addLast(new Token(cfg.deindentKey, genIndent(lastIndent - diSize - i - 1), tk.row, tk.col));
                }
                return tokens.poll();

            }
            lastIndent = diSize;
        }
        return tk;
    }

    private String genIndent(int size) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < size; i++) {
            res.append(indentUnit);
        }
        return res.toString();
    }

    private int indentSize(String indent) {
        int i = 0;
        int uLength = indentUnit.length();
        if ((indent.length() < uLength && !indent.isEmpty()) || indent.length() % uLength != 0) {
            return -1;
        }
        while (i < indent.length() / uLength) {
            int j = indent.indexOf(indentUnit, i * uLength);
            if (j != i * uLength) {
                return -1;
            }
            i++;
        }
        return i;
    }

    public String getRawLine() {
        return lexer.getRawLine();
    }

    public String getCurrentColIndentString() {
        return lexer.getCurrentColIndentString();
    }
}
