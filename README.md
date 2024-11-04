# idea-code-formatter-maven-plugin

[![Java CI with Maven](https://github.com/mschieder/idea-code-formatter/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/mschieder/idea-code-formatter/actions/workflows/maven.yml)

A maven plugin that uses and bundles a standalone version of the [IntelliJ IDEA command line formatter](https://www.jetbrains.com/help/idea/command-line-formatter.html).

This plugin can validate and reformat your code from the command line via Maven.

No installed IntelliJ IDEA is required, so you can use this plugin in your CI pipelines.

### Minimal config example

Validate all .java files recursively in ``src/main/java`` and ``src/test/java`` using the IntelliJ IDEAs default code format settings.

```xml

<plugin>
    <groupId>com.github.mschieder</groupId>
    <artifactId>idea-code-formatter-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### More complex example

Validate all .java and .xml files recursively in ``target/generated-sources/java``, ``src/main/java`` and ``src/main/resources`` using the code style settings file ``conf/Default.xml``.

```xml

<plugin>
    <groupId>com.github.mschieder</groupId>
    <artifactId>idea-code-formatter-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <configuration>
        <directories>
            <directory>target/generated-sources/java</directory>
            <directory>src/main/java</directory>
            <directory>src/main/resources</directory>
        </directories>
        <codestyleSettingsFile>conf/Default.xml</codestyleSettingsFile>
        <masks>
            <mask>.java</mask>
            <mask>.xml</mask>
        </masks>
        <recursive>true</recursive>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Format from the command line example

Call format for the current and all inherited submodules.

```bash
mvn com.github.mschieder:idea-code-formatter-plugin:apply
```

Use the ``-pl :___INSERT_MODULE_NAME___`` option to format a specific module.

## idea-code-formatter

This is the "smaller" (205 MB) standalone version of the [IntelliJ IDEA command line formatter](https://www.jetbrains.com/help/idea/command-line-formatter.html).

### Usage

```bash
$ java -jar idea-code-formatter-1.0.0-SNAPSHOT.jar
IntelliJ IDEA 2023.1, build IC-231.8109.175 Formatter

Usage: format [-h] [-r|-R] [-d|-dry] [-s|-settings settingsPath] [-charset charsetName] [-allowDefaults] path1 path2...
-h|-help         Show a help message and exit.
-s|-settings     A path to Intellij IDEA code style settings .xml file. This setting will be
be used as a primary one regardless to the surrounding project settings
-r|-R            Scan directories recursively.
-d|-dry          Perform a dry run: no file modifications, only exit status.
-m|-mask         A comma-separated list of file masks.
-charset         Force charset to use when reading and writing files.
-allowDefaults   Use factory defaults when style is not defined for a given file. I.e. when -s
is not not set and file doesn't belong to any IDEA project. Otherwise file will
be ignored.
path<n>        A path to a file or a directory.
```

## Building

```bash
mvn clean install
```