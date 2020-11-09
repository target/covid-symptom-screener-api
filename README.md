# Covid Symptom Screener - API

## Appropriate Use and Disclaimer

This project and code are presented as example only. It was produced as a point-in-time effort to help the state of the Minnesota, and is not in use at Target. This code is not production ready, and should be updated according to specific user concerns of those who wish to fork and use it.

Target provides this source as an example of a pro-bono effort, but provides no guarantee of it being secure, etc. and is not liable for any damages incurred.

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
