package hvitt;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HvittLexer implements Lexer {
    private static enum State {
        S, EOF, LN, NL
    }

    private static final class PosString {
        private final String data;
        private final int row;
        private final int col;

        private PosString(String data, int row, int col) {
            this.col = col;
            this.row = row;
            this.data = data;
        }
    }

    private static final Comparator<LexerConfig.HMatcher> HMATCHER_COMPARATOR = new Comparator<LexerConfig.HMatcher>() {
        public int compare(LexerConfig.HMatcher m1, LexerConfig.HMatcher m2) {
            return stringOf(m2).length() - stringOf(m1).length();//we want to sort from longer to shorted
        }

        private String stringOf(LexerConfig.HMatcher m) {
            if (m instanceof LexerConfig.RegEx) {
                return ((LexerConfig.RegEx) m).regex;
            } else if (m instanceof LexerConfig.Literal) {
                return ((LexerConfig.Literal) m).literal;
            } else {
                throw new IllegalArgumentException("Unknown HMatcher " + m);
            }
        }
    };

    private final LexerConfig cfg;
    private final Pattern whiteSpace;
    private List<Pair<String, Pattern>> matchers;
    private final BufferedReader reader;
    private String rawLine;
    private String line;
    private StringBuilder currentColIndentString = new StringBuilder();
    private int lastIndentLen = 0;
    private int row = 0, col = 0;
    private Token currentToken = null;
    private State state = State.S;
    private Token eofToken = null;

    public HvittLexer(Reader reader, LexerConfig cfg) {
        this.cfg = cfg;
        this.reader = new BufferedReader(reader);
        whiteSpace = makePattern("\\s+");
        matchers = new ArrayList<Pair<String, Pattern>>(cfg.matchers.size());
        for (Pair<String, List<LexerConfig.HMatcher>> e : cfg.matchers) {
            matchers.add(new Pair(e._1, makePattern(e._2)));
        }
    }

    private final Pattern makePattern(List<LexerConfig.HMatcher> hMatchers) {
        List<LexerConfig.HMatcher> ms = new ArrayList<LexerConfig.HMatcher>(hMatchers);
        Collections.sort(ms, HMATCHER_COMPARATOR);
        StringBuilder regexp = new StringBuilder();
        boolean first = true;
        for (LexerConfig.HMatcher m : ms) {
            if (first) {
                first = false;
            } else {
                regexp.append("|");
            }
            regexp.append("(?:");
            if (m instanceof LexerConfig.RegEx) {
                regexp.append(((LexerConfig.RegEx) m).regex);
            } else if (m instanceof LexerConfig.Literal) {
                regexp.append(Pattern.quote(((LexerConfig.Literal) m).literal));
            } else {
                throw new IllegalArgumentException("Unknown HMatcher " + m);
            }
            regexp.append(")");
        }
        return makePattern(regexp.toString());
    }

    private final Pattern makePattern(String regexp) {
        return Pattern.compile("^(" + regexp + ").*");
    }

    public Token peek() {
        if (currentToken == null) {
            currentToken = pop();
        }
        return currentToken;
    }

    public Token pop() throws UnrecognizedInput {
        if (currentToken != null) {
            Token res = currentToken;
            currentToken = null;
            return res;
        }

        while (true) {
            switch (state) {
                case S:
                    try {
                        rawLine = reader.readLine();
                        line = rawLine;
                        if (line == null) {
                            state = State.EOF;
                            eofToken = new Token(cfg.eofKey, "$", row, col);
                            reader.close();
                            return eofToken;
                        } else {
                            row++;
                            col = 1;
                            currentColIndentString = new StringBuilder();
                            state = State.NL;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case EOF:
                    return eofToken;
                case NL:
                    state = State.LN;
                    if (line.trim().isEmpty()) {
                        state = State.S;
                        return new Token(cfg.newLineKey, line, row, col);
                    }
                    PosString indent = chew(whiteSpace);
                    if (indent == null) {
                        indent = new PosString("", row, 1);
                    }
                    currentColIndentString.append(indent.data);
                    int indentLen = indent.data.length();
                    if (indentLen > lastIndentLen) {
                        lastIndentLen = indentLen;
                        return new Token(cfg.indentKey, indent.data, indent.row, indent.col);
                    } else if (indentLen < lastIndentLen) {
                        lastIndentLen = indentLen;
                        return new Token(cfg.deindentKey, indent.data, indent.row, indent.col);
                    } else {
                        return new Token(cfg.newLineKey, indent.data, indent.row, indent.col);
                    }
                case LN:
                    if (line.isEmpty()) {
                        state = State.S;
                    } else {
                        PosString data = null;
                        boolean ignore = false;
                        for (Pair<String, Pattern> kp : matchers) {
                            data = chew(kp._2);
                            if (data != null) {
                                currentColIndentString.append(data.data.replaceAll("[^\\s]", " "));
                                PosString white = chew(whiteSpace);
                                if (white != null) {
                                    currentColIndentString.append(white.data);
                                }
                                if (cfg.ignoreKey.equals(kp._1)) {
                                    ignore = true;
                                } else {
                                    return new Token(kp._1, data.data, data.row, data.col);
                                }
                            }
                        }
                        if (!ignore) {
                            throw new UnrecognizedInput(rawLine, row, col, currentColIndentString.toString());
                        }
                    }
                    break;
            }
        }
    }

    private PosString chew(Pattern p) {
        Matcher matcher = p.matcher(line);
        if (matcher.matches()) {
            String data = matcher.group(1);
            line = line.substring(data.length());
            PosString res = new PosString(data, row, col);
            col += data.length();
            return res;
        } else {
            return null;
        }
    }

    public String getRawLine() {
        return rawLine;
    }

    public String getCurrentColIndentString() {
        return currentColIndentString.toString();
    }
}
