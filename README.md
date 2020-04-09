# Kelvin API

This is the services component for the Temperature Aggregation platform.

## Running tests

You can run tests using Gradle wrapper:

```bash
./gradlew clean build
```

Note that this will leverage Docker for running Postgres as a dependency for functional tests.

## Building

The build is run with the same command:
```bash
./gradlew clean build
```

The final JAR file is output to `build/libs/kelvin-api.jar`.

The intended process is to build out a Docker container:
```bash
./gradlew clean build
cp build/libs/kelvin-api.jar docker/.
docker build -t kelvin-api:latest
```
