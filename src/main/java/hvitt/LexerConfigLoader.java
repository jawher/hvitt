package hvitt;

import java.io.Reader;

public class LexerConfigLoader {

    public static final String TOKEN = "TOKEN";
    public static final String SODEF = "SODEF";
    public static final String EODEF = "EODEF";
    public static final String OR = "OR";
    public static final String LITERAL = "LITERAL";
    public static final String REGEX = "REGEX";

    private static final class LexerConfigParser {
        private final Lexer lexer;
        private Token token;

        private LexerConfigParser(Lexer lexer) {
            this.lexer = lexer;
        }

        public void parse(LexerConfig metaCfg) {
            boolean done = false;
            while (!done) {
                eatWhite();
                if (found(TOKEN)) {
                    String tokenKey = token.text;
                    eatWhite();
                    expect(SODEF);
                    rule(tokenKey, metaCfg);
                    eatWhite();
                    while (found(OR)) {
                        rule(tokenKey, metaCfg);
                    }
                    eatWhite();
                    expect(EODEF);
                } else {
                    expect("EOF");
                    done = true;
                }
            }
        }

        private void rule(String tokenKey, LexerConfig metaCfg) {
            eatWhite();
            if (found(LITERAL)) {
                metaCfg.addLiteralRule(tokenKey, soulOf(token.text));
            } else if (found(REGEX)) {
                metaCfg.addRegexRule(tokenKey, soulOf(token.text));
            }
        }

        private String soulOf(String literal) {
            if (literal.length() > 2) {
                return literal.substring(1, literal.length() - 1);
            } else {
                return literal;
            }

        }

        private void eatWhite() {
            while (found("INDENT") || found("DEINDENT") || found("NEWLINE")) ;
        }

        private boolean found(String key) {
            Token tk = lexer.peek();
            if (tk.key.equals(key)) {
                lexer.pop();
                token = tk;
                return true;
            } else {
                return false;
            }
        }

        private void expect(String key) {
            if (!found(key)) {
                throw new RuntimeException("Was expecting " + valueOfToken(key) + " but got " + lexer.peek() + msg());
            }
        }

        private String msg() {
            String line = lexer.getRawLine();
            return "\n" + line + "\n" + nSpaces(lexer.peek().col - 1) + "^";
        }

        private String valueOfToken(String key) {
            if (SODEF.equals(key)) {
                return ":";
            } else if (EODEF.equals(key)) {
                return ";";
            } else if (OR.equals(key)) {
                return "|";
            } else {
                return key;
            }
        }

        private String nSpaces(int n) {
            StringBuilder res = new StringBuilder();
            for (int i = 0; i < n; i++) {
                res.append(" ");//not very solid: watch out for when the line contains tabs
            }
            return res.toString();
        }

    }

    public static void loadInto(Reader cfgFile, LexerConfig cfg) {
        LexerConfig metaCfg = new LexerConfig();

        metaCfg.addRegexRule(TOKEN, "[a-zA-Z][a-zA-Z0-9_]*");
        metaCfg.addLiteralRule(SODEF, ":");
        metaCfg.addLiteralRule(EODEF, ";");
        metaCfg.addLiteralRule(OR, "|");
        metaCfg.addRegexRule(LITERAL, "'(\\\\.|[^'])*?'");
        metaCfg.addRegexRule(REGEX, "/(\\\\.|[^/])*?/");

        Lexer l = new HvittLexer(cfgFile, metaCfg);
        LexerConfigParser p = new LexerConfigParser(l);
        p.parse(cfg);
    }

    public static LexerConfig load(Reader cfgFile) {
        LexerConfig cfg = new LexerConfig();
        loadInto(cfgFile, cfg);
        return cfg;
    }

}
