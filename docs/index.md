---
layout: default
nav_order: 1
title: Getting Started
---

# Samosa
{: .no_toc}

[![Total alerts](https://img.shields.io/lgtm/alerts/g/souris-dev/samosac-jvm.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/souris-dev/samosac-jvm/alerts/) [![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/souris-dev/samosac-jvm.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/souris-dev/samosac-jvm/context:java)

_Welcome, samosa lovers!_

**Samosa is a programming language written in Java and Kotlin, that runs on the JVM.**

_Note: Samosa, the programming language, is named after an Indian snack called "samosa", and is pronounced as "some-o-saa" (the part "saa" is pronounced like the word "sour", but without the "r")._

_Note: Samosa is the name of the language, and `samosac` is the name of the compiler._
## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---
## Getting Started

### Installation

There are some alternatives for installing samosa. _This section will be updated soon with the other alternatives._

#### Building from source

**Note: Ensure that you have the following installed (and in your PATH) before using the upcoming commands to build from source:**

*   **git**
*   **≥ JDK 11 (the project was developed on JDK 17, but the code is compatible with java version >= 11.)**
*   **Apache Maven 3.1 or higher version**

To download the source and build it using maven, run these in the terminal of your choice:

```
git clone https://github.com/souris-dev/samosac-jvm.git
cd samosac-jvm
mvn compile
```
Then, to build the compiler jar, use (from within the project directory):

```
mvn package
```

This will create a `samosac-1.0-full.jar` in the `target` folder. This is the compiler jar.

_Easier installation methods will be provided soon._

### Usage

**Note: Ensure that you have the JRE (minimum java version 11) installed before starting this section.**

#### Compiling your program

Type your samosa program in a file, and name it something (for example samosa.samo). Then use the .jar file of the compiler to compile it **(ensure that you have java in you PATH)**:

```
java -jar samosac-1.0-full.jar samosa.samo
```

(Replace `samosac-1.0-full.jar` with the full path to the compiler jar file, and `samosa.samo` with the name of the file you wrote your program in.)

_This section will be updated._

#### Running the program

As samosa compiles to JVM bytecode, a `.class` is generated, named as per your filename. So for the above example, a file named `SamosaSamo.class` would be created in the `./out` directory.

To run it, do this **(ensure that you have java in your PATH)**:

```
cd out
java SamosaSamo
```
