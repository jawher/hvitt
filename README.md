Hvitt
=====

A whitespace-sensitive lexer written in Java. There is no fancy code generation involved <del>nor a configuration file in a nearly but note quite a (((E|A)?B)|G)NF syntax</del>.
`hvitt` is used  simply via its Java api to configure a lexer with a set of rules and running it on an input to generate a stream of tokens.

I'm not excluding though the idea of extending `hvett` to accept the lexing rules via a text file in a nearly but note quite a (((E|A)?B)|G)NF syntax, so you don't need to worry about Java-specific escaping codes.

# Y U name iz hvitt ?

`hvitt` is the color White in Icelandic, or so Google translate tells me. I started out with "whitespace sensitive" and tried translating it in different languages and picked `hvitt`: it looks weird (which is good) and it'll confuse people as to how to pronounce it (which is also good).

# Using this library

Here's a quick sample of how `hvitt` can be used to lex an input into tokens based on a simple rules set:

Given this input (a gcd implementation in Python, a well known whitespace-sensitive language):

```python
def gcd(a, b):
    while b:
        a, b = b, a%b
    return a

gcd(7, 21)
```

We start by defining the lexing rules the `hvitt` lexer will use to divide the input into tokens:

## Java configuration
`hvitt` lexing rules can be configured using a java api, as shown in the following example:

```java
LexerConfig cfg = new LexerConfig();

// we start by defining the keywords, a list of string literals
cfg.addLiteralRule("KEYWORD", "def", "while", "return");

// Here we define the names (of variables, functions and classes) using a regular expression
cfg.addRegexRule("NAME", "[a-zA-Z][a-zA-Z0-9_]*");

// We also define the numbers using a regular expression
cfg.addRegexRule("NUMBER", "-?[0-9]+");

// We finally define symbols using a list of string literals
cfg.addLiteralRule("SYMBOL", "(", ")", ":", "=", "%", ",", "%");
```
To define a lexing rule, we need to provide a key that'll identify the class of the tokens (a number, a symbol, etc.) and a list of literals and/or regular expressions describing the kind of inputs the token represents.

## File configuration
`hvitt` lexing rules can also be defined using an external text configuration file in nearly but note quite a (((E|A)?B)|G)NF syntax. The same rule set configured in the example above can be represented in a file:

    KEYWORD: 'def' | 'while' | 'return';
    NAME: /[a-zA-Z][a-zA-Z0-9_]*/;
    NUMBER: /-?[0-9]+/;
    SYMBOL: '(' | ')' | ':' | '=' | '%' | ',' | '%';

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

Once the rules are defined, a lexer can be created by providing a reader pointing to the input to be lexed and the rules:

```java
Lexer l = new Lexer(new InputStreamReader(Python.class.getResourceAsStream("input")), cfg);
```

It's then simply a matter of iterating on the input tokens generated by the lexer until the end of input is reached, which is signalled by a token with a special key "EOF" (this value is configurable):

```java
Token tk;
do {
    tk = l.pop();
    System.out.println(tk);
} while (!"EOF".equals(tk.key));
```

Executing this program will generate the following output (where each line represents a token by its key, the input bit that it represents and the row:col where it appears:

    KEYWORD('def') @ 1:1
    NAME('gcd') @ 1:5
    SYMBOL('(') @ 1:8
    NAME('a') @ 1:9
    SYMBOL(',') @ 1:10
    NAME('b') @ 1:12
    SYMBOL(')') @ 1:13
    SYMBOL(':') @ 1:14
    NEWLINE('') @ 2:1
    INDENT('    ') @ 2:1
    KEYWORD('while') @ 2:5
    NAME('b') @ 2:11
    SYMBOL(':') @ 2:12
    NEWLINE('') @ 3:1
    INDENT('        ') @ 3:1
    NAME('a') @ 3:9
    SYMBOL(',') @ 3:10
    NAME('b') @ 3:12
    SYMBOL('=') @ 3:14
    NAME('b') @ 3:16
    SYMBOL(',') @ 3:17
    NAME('a') @ 3:19
    SYMBOL('%') @ 3:20
    NAME('b') @ 3:21
    NEWLINE('') @ 4:1
    DEINDENT('    ') @ 4:1
    KEYWORD('return') @ 4:5
    NAME('a') @ 4:12
    NEWLINE('') @ 5:1
    NEWLINE('') @ 6:1
    DEINDENT('') @ 6:1
    NAME('gcd') @ 6:1
    SYMBOL('(') @ 6:4
    NUMBER('7') @ 6:5
    SYMBOL(',') @ 6:6
    NUMBER('21') @ 6:8
    SYMBOL(')') @ 6:10
    EOF('$') @ 6:11

Notice the token keys INDENT, DEINDENT and NEWLINE (that we didn't have to define in the lexer rules) and that are being automatically generated by the lexer whenever the indentation changes (INDENT and DEINDENT) or when the lexer goes to the next line.

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
