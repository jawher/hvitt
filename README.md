Hvitt
=====

A whitespace-sensitive lexer written in Java. There is no fancy code generation involved <del>nor a configuration file in a nearly but note quite a (((E|A)?B)|G)NF syntax</del>.
`hvitt` is used  simply via its Java api to configure a lexer with a set of rules and running it on an input to generate a stream of tokens.

I'm not excluding though the idea of extending `hvett` to accept the lexing rules via a text file in a nearly but note quite a (((E|A)?B)|G)NF syntax, so you don't need to worry about Java-specific escaping codes.

# Y U name iz hvitt ?

`hvitt` is the color White in Icelandic, or so Google translate tells me. I started out with "whitespace sensitive" and tried translating it in different languages and picked `hvitt`: it looks weird (which is good) and it'll confuse people as to how to pronounce it (which is also good).

# Whistespace-sensitive, what is that ?

Usually, lexer throw away whitespace tokens (spaces, tabs, newlines) as they are not relevant to the parser nor to the language semantics.

Some languages however are whitespace-sensitive, like Python or Roy for example, where whitespace is used to eliminate block delimiters (`{` and `}` usually in C-like languages).
In Python and Roy, a block is started by increasing the indentation, and closed by decreasing it. For example:

```python
while b:
    b -= 1
print 'result'
print b

```

In the previous code snippet, a tab was used in the second line to indicate that `b -= 1` is part of the while body,
whereas the following instruction, `print b`, is not because it has the same indentation as the while.

In order to correctly parse such languages, the lexer should generate tokens for the whitespace-related information to be used by the following stage, the parser, to make sense of the code structure.
That's where `hvitt` comes in. Basically, it's like classic parser, except that it generates 3 special tokens:

* NEWLINE: whenever a newline is encountered such as the new line's indentation is the same as the previous'.
* INDENT: whenever a newline is encountered such as the new line's indentation is greater than the previous'.
* DEINDENT: whenever a newline is encountered such as the new line's indentation is lesser than the previous'.

For the previous python snippet, besides the tokens corresponding to the different keywords (while, print), identifiers (b), literals (1, 'result') and operators and symbols (-=, :),
`hvitt` generates and `INDENT` token after the `:` symbol and before `b`, a `DEINDENT` token after `1` and before the first `print`
and a `NEWLINE` token after `'result'` and before the second `print`.

`hvitt` doesn't generate tokens for all whitespace, for example, the space between `while` and `b` is ignored. If you want to parse a language that needs that, may god help you.

Also, `hvitt` generates a single token for newlines with increased (`INDENT`) or decreased (`DEINDENT) indentation. So in practice, you'll never
see a `NEWLINE` token followed by `INDENT` or `DEINDENT`, like some other whitespace-sensitive or aware lexers do, unless there really is a blank line in between.

# Using this library

Here's a quick sample of how `hvitt` can be used to lex an input into tokens based on a simple rules set:

Given this input (a gcd implementation in Python):

```python
\# gcd implementation
def gcd(a, b):
    while b:
        a, b = b, a%b
    return a

gcd(7, 21)
```

## Configuration
We start by defining the lexing rules the `hvitt` lexer will use to divide the input into tokens:
### Java configuration
`hvitt` lexing rules can be configured using a java api, as shown in the following example:

```java
LexerConfig cfg = new LexerConfig();

// we start by defining the keywords, a list of string literals
cfg.addLiteralRule("KEYWORD", "def", "while", "return");

// Here we define the names (of variables, functions and classes) using a regular expression
cfg.addRegexRule("NAME", "[a-zA-Z][a-zA-Z0-9_]*");

// We also define the numbers using a regular expression
cfg.addRegexRule("NUMBER", "-?[0-9]+");

// We define symbols using a list of string literals
cfg.addLiteralRule("SYMBOL", "(", ")", ":", "=", "%", ",", "%", ">");

// We can instruct hvitt to ignore comments by using a special token key "IGNORE" (the key can be customized using cfg.ignoreKey)
cfg.addRegexRule("IGNORE", "#.*");
```
To define a lexing rule, we need to provide a key that'll identify the class of the tokens (a number, a symbol, etc.) and a list of literals and/or regular expressions describing the kind of inputs the token represents.

### File configuration
`hvitt` lexing rules can also be defined using an external text configuration file in nearly but note quite a (((E|A)?B)|G)NF syntax. The same rule set configured in the example above can be represented in a file:

    KEYWORD: 'def' | 'while' | 'return';
    NAME: /[a-zA-Z][a-zA-Z0-9_]*/;
    NUMBER: /-?[0-9]+/;
    SYMBOL: '(' | ')' | ':' | '=' | '%' | ',' | '%' | '>';
    IGNORE: /#.*/;

A rule can be defined by specifying the token key (`KEYWORD` in the first line for example), a colon `:`, a list of literals and regular expressions seperated by a pipe `|` and closed with a semi-colon `;`.

A literal must be enclosed in an apostrophes `'`, like in `'while'`, whereas a regular expression, like in Javascript, is enclosed between 2 `/`, like in `/[0..9]+/`.

Besides that, the file format is pretty liberal: you can place in as many whitespace (including tabs and carriage returns) as you like, define multiple rules in the same line, mix literals and regular expressions in the same rule, etc.

You can also split a token definition in multiple rules, and its definition will be augmented instead of being replaced. For example:

    KEYWORD: 'def';
    KEYWORD: 'while';
    KEYWORD: 'return';
    NAME: /[a-zA-Z][a-zA-Z0-9_]*/;

To create a lexer configuration from a rules definition file, you use the `LexerConfigLoader` class:

    LexerConfig cfg = LexerConfigLoader.load(reader);

Where `reader` is a `java.io.Reader` pointing to the config file.

> It is to be noted that the `hvitt`'s rules definition file is parsed using `hvitt` as a lexer and a hand-written recursive-descent parser (well, it doesn't go beyond 2 recursion levels). In other words, the lexer is used to lex its tokens defintion file.
Yep, that's meta lexing right there, or, put another way, eating your own dog food.

## Lexing
### Stock lexer
Once the rules are defined, a lexer can be created by providing a reader pointing to the input to be lexed and the rules:

```java
Lexer lexer = new HvittLexer(reader, cfg);
```

Where `HvittLexer` is the stock lexer implementation and `reader` is a `java.io.Reader` pointing to the input to be lexed.

It's then simply a matter of iterating on the input tokens generated by the lexer until the end of input is reached, which is signalled by a token with a special key "EOF" (this value is configurable):

```java
try {
    while (!"EOF".equals(lexer.peek().key)) {
        Token tk = lexer.pop();
        System.out.println(tk);
    }
} catch (LexingException e) {
    e.printStackTrace();
}
```

Executing this program will generate the following output (where each line represents a token by its key, the input bit that it represents and the row:col where it appears:

    NEWLINE('') @ 2:1
    KEYWORD('def') @ 2:1
    NAME('gcd') @ 2:5
    SYMBOL('(') @ 2:8
    NAME('a') @ 2:9
    SYMBOL(',') @ 2:10
    NAME('b') @ 2:12
    SYMBOL(')') @ 2:13
    SYMBOL(':') @ 2:14
    INDENT('    ') @ 3:1
    KEYWORD('while') @ 3:5
    NAME('b') @ 3:11
    SYMBOL(':') @ 3:12
    INDENT('        ') @ 4:1
    NAME('a') @ 4:9
    SYMBOL(',') @ 4:10
    NAME('b') @ 4:12
    SYMBOL('=') @ 4:14
    NAME('b') @ 4:16
    SYMBOL(',') @ 4:17
    NAME('a') @ 4:19
    SYMBOL('%') @ 4:20
    NAME('b') @ 4:21
    DEINDENT('    ') @ 5:1
    KEYWORD('return') @ 5:5
    NAME('a') @ 5:12
    NEWLINE('') @ 6:1
    NEWLINE('') @ 7:1
    DEINDENT('') @ 8:1
    NAME('gcd') @ 8:1
    SYMBOL('(') @ 8:4
    NUMBER('7') @ 8:5
    SYMBOL(',') @ 8:6
    NUMBER('21') @ 8:8
    SYMBOL(')') @ 8:10

Notice the token keys `INDENT`, `DEINDENT` and `NEWLINE` (that we didn't have to define in the lexer rules) and that are being automatically generated by the lexer whenever the indentation changes (`INDENT` and `DEINDENT`) or when the lexer goes to the next line.

The stock lexer doesn't perform much magic, it simply returns tokens as they are encountered in the input. `hvitt`comes with some more lexers that do some useful filtering and input checking.

### CollapsingLexer

`CollapsingLexer` collapses consecutive `NEWLINE` tokens into 1 single token, so you don't have to handle such cases in the parser.
Actually, it is a bit smarter than that: it'll also completely filter out `NEWLINE tokens occuring just before `INDENT` or `DEINDENT tokens.

Optionally, `CollapsingParser` can be configured to trim the `NEWLINE` tokens in the beginning and end of the input.

`CollapsingLexer` is used by wrapping another lexer that does the real work generating the tokens and filters its output:

```java
Lexer hvittLexer = new HvittLexer(reader, cfg);
CollapsingLexer lexer = new CollapsingLexer(hvittLexer, cfg, true);
try {
    while (!"EOF".equals(lexer.peek().key)) {
        Token tk = lexer.pop();
        System.out.println(tk);
    }
} catch (LexingException e) {
    e.printStackTrace();
}
```

Using `CollapsingLexer` on the same pythn gcd snippet outputs the following tokens:

    KEYWORD('def') @ 2:1
    NAME('gcd') @ 2:5
    SYMBOL('(') @ 2:8
    NAME('a') @ 2:9
    SYMBOL(',') @ 2:10
    NAME('b') @ 2:12
    SYMBOL(')') @ 2:13
    SYMBOL(':') @ 2:14
    INDENT('    ') @ 3:1
    KEYWORD('while') @ 3:5
    NAME('b') @ 3:11
    SYMBOL(':') @ 3:12
    INDENT('        ') @ 4:1
    NAME('a') @ 4:9
    SYMBOL(',') @ 4:10
    NAME('b') @ 4:12
    SYMBOL('=') @ 4:14
    NAME('b') @ 4:16
    SYMBOL(',') @ 4:17
    NAME('a') @ 4:19
    SYMBOL('%') @ 4:20
    NAME('b') @ 4:21
    DEINDENT('    ') @ 5:1
    KEYWORD('return') @ 5:5
    NAME('a') @ 5:12
    DEINDENT('') @ 8:1
    NAME('gcd') @ 8:1
    SYMBOL('(') @ 8:4
    NUMBER('7') @ 8:5
    SYMBOL(',') @ 8:6
    NUMBER('21') @ 8:8
    SYMBOL(')') @ 8:10

Here's how the new output differs from the stock lexer's output:

![diff](http://i.imgur.com/ANMuP.png)

### StructuredLexer
`StructuredLexer` s another lexer wrapper that, given an indentation unit (4 spaces, tab), ensures the input respects indentation rules (new indentations are exactly one indentation unit more than the previous),
but also generates virtual tokens for multi-level deindentations.

A `StructuredLexer` is created by wrapping another lexer and by providing the indentation unit:

```java
Lexer hvittLexer = new HvittLexer(reader, cfg);
Lexer lexer = new StructuredLexer(hvittLexer, cfg, "    ");
```

For example, given this input, where `return a` is not correctly indented (the indentation increass by 2):

```Python
def stupidMax(a, b):
    if a > b:
            return a

print 5
```
The lexer fails with the following error:

    hvitt.LexingException: Invalid indent at (3, 1):
                return a
                ^

With this error fixed:

```Python
def stupidMax(a, b):
    if a > b:
        return a

print 5
```

The `StructuredLexer` returns the following list of tokens:

    KEYWORD('def') @ 1:1
    NAME('stupidMax') @ 1:5
    SYMBOL('(') @ 1:14
    NAME('a') @ 1:15
    SYMBOL(',') @ 1:16
    NAME('b') @ 1:18
    SYMBOL(')') @ 1:19
    SYMBOL(':') @ 1:20
    INDENT('    ') @ 2:1
    NAME('if') @ 2:5
    NAME('a') @ 2:8
    SYMBOL('>') @ 2:10
    NAME('b') @ 2:12
    SYMBOL(':') @ 2:13
    INDENT('        ') @ 3:1
    KEYWORD('return') @ 3:9
    NAME('a') @ 3:16
    DEINDENT('    ') @ 5:1
    DEINDENT('') @ 5:1
    NAME('print') @ 5:1
    NUMBER('5') @ 5:7

Notice the 2 `DEINDENT` tokens generated after `return a` where the stock lexer would have returned only one.


# Building

You need a Java 6 (or newer) environment and Maven 3 installed:

    Jawhers-MacBook-Air:hvitt jawher$ mvn --version
    Apache Maven 3.0.3 (r1075438; 2011-02-28 18:31:09+0100)
    Maven home: /usr/share/maven
    Java version: 1.6.0_29, vendor: Apple Inc.
    Java home: /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home
    Default locale: en_US, platform encoding: MacRoman
    OS name: "mac os x", version: "10.7.2", arch: "x86_64", family: "mac"

You should now be able to do a full build of `hvitt`:

    $ git clone git://github.com/jawher/hvitt.git
    $ cd hvitt
    $ mvn clean install

To use this library in your projects, add the following to the `dependencies` section of your
`pom.xml`:

```xml
<dependency>
  <groupId>hvitt</groupId>
  <artifactId>hvitt</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

#Troubleshooting

Please consider using [Github issues tracker](http://github.com/jawher/hvitt/issues) to submit bug reports or feature requests.


# License

See `LICENSE` for details.
