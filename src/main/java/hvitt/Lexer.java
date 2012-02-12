package hvitt;

public interface Lexer {
    Token peek();

    Token pop() throws UnrecognizedInput;

    String getRawLine();

    String getCurrentColIndentString();
}
