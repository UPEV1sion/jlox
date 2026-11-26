# jlox

`jlox` is a Java implementation of the [Lox programming language](https://craftinginterpreters.com/the-lox-language.html), following the tree-walk interpreter approach described in [Crafting Interpreters](https://craftinginterpreters.com/).

The interpreter prioritizes simplicity and clarity over execution performance. It evaluates programs by traversing their abstract syntax trees directly.

For an alternative implementation focused on execution performance, see my [clox repository](https://codeberg.org/UPEV1sion/clox), which implements a bytecode virtual machine in C.

## Features

- Lexical analysis and parsing
- Abstract syntax tree representation
- Tree-walk interpretation
- Runtime environments and variable scoping
- [Add other implemented language features here]

## Getting Started

### Prerequisites

- Java 21 or later
- Maven

### Building

```bash
mvn clean install
```

### Running

```bash
java -jar target/jlox-*.jar [files...]
```

When invoked without file arguments, `jlox` starts an interactive REPL.

For an introduction to the Lox language and its syntax, see [The Lox Language](https://craftinginterpreters.com/the-lox-language.html).
