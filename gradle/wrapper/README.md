# Gradle Wrapper

This directory contains the Gradle Wrapper configuration for Gradle 9.3.1.

## Generating Wrapper Scripts

To generate the `gradlew` and `gradlew.bat` scripts, run:

```bash
gradle wrapper --gradle-version 9.3.1
```

This will create:
- `gradlew` - Unix/Linux/Mac executable
- `gradlew.bat` - Windows executable
- `gradle/wrapper/gradle-wrapper.jar` - Wrapper JAR file

## Why Use the Wrapper?

The Gradle Wrapper ensures everyone uses the same Gradle version, eliminating "works on my machine" issues.

Once generated, use `./gradlew` instead of `gradle` for all commands.
