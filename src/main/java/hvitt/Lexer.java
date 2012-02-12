package hvitt;

public interface Lexer {
    Token peek();

    Token pop() throws LexingException;

    String getRawLine();

    String getCurrentColIndentString();
}
