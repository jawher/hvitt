package hvitt;

public class UnrecognizedInput extends RuntimeException {
    public final String line;
    public final int row;
    public final int col;


    public UnrecognizedInput(String line, int row, int col) {
        super("Unrecognized input");
        this.line = line;
        this.col = col;
        this.row = row;
    }
}
