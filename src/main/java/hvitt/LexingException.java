package hvitt;

public class LexingException extends RuntimeException {
    public final String indent;
    public final String line;
    public final int row;
    public final int col;


    public LexingException(String error, String line, int row, int col, String indent) {
        super(error + " at (" + row + ", " + col + "):\n" + line + "\n" + indent + "^");
        this.line = line;
        this.col = col;
        this.row = row;
        this.indent = indent;
    }
}
