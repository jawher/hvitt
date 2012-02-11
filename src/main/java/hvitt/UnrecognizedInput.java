package hvitt;

public class UnrecognizedInput extends RuntimeException {
    public final String indent;
    public final String line;
    public final int row;
    public final int col;


    public UnrecognizedInput(String line, int row, int col, String indent) {
        super("Unrecognized input at ("+row+", "+col+"):\n"+line+"\n"+indent+"^");
        this.line = line;
        this.col = col;
        this.row = row;
        this.indent = indent;
    }
}
