package hvitt;

public class Token {
    public final String key;
    public final String text;
    public final int row;
    public final int col;

    public Token(String key, String text, int row, int col) {
        this.key = key;
        this.text = text;
        this.row = row;
        this.col = col;
    }

    @Override
    public String toString() {
        return key + "('" + text + "') @ " + row + ":" + col;
    }
}
