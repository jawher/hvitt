package hvitt;

import java.util.ArrayList;
import java.util.List;

public class LexerConfig {
    public static interface HMatcher {
    }

    public static final class RegEx implements HMatcher {
        public final String regex;

        public RegEx(String regex) {
            this.regex = regex;
        }
    }

    public static final class Literal implements HMatcher {
        public final String literal;

        public Literal(String literal) {
            this.literal = literal;
        }
    }

    public String indentKey = "INDENT";
    public String deindentKey = "DEINDENT";
    public String newLineKey = "NEWLINE";
    public String eofKey = "EOF";

    List<Pair<String, List<HMatcher>>> matchers = new ArrayList<Pair<String, List<HMatcher>>>();

    public LexerConfig addRegexRule(String key, String... regexes) {
        List<HMatcher> ms = new ArrayList<HMatcher>(regexes.length);
        for (String regex : regexes) {
            ms.add(new RegEx(regex));
        }

        matchers.add(new Pair<String, List<HMatcher>>(key, ms));
        return this;
    }

    public LexerConfig addLiteralRule(String key, String... literals) {
        List<HMatcher> ms = new ArrayList<HMatcher>(literals.length);

        for (String literal : literals) {
            ms.add(new Literal(literal));
        }
        matchers.add(new Pair<String, List<HMatcher>>(key, ms));
        return this;
    }
}
