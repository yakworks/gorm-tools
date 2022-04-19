[![CircleCI](https://circleci.com/gh/9ci/rally/tree/master.svg?style=svg&circle-token=6f714429ffea67eb858f4947e97dc079841d9c41)](https://circleci.com/gh/9ci/rally/tree/master])

# Rally Grails Plugin

## Dev Quick Start

### Prerequisites

1. Credentials for repo.9ci.com and Docker hub for dock9
2. Java 8 or higher
3. Docker

### Setup

1. run `docker login` and enter credentials so build process can download nine-db for testing

2. Add credentials for repo.9ci into the ~/.gradle/gradle.properties
	```
	mavenRepoUser=YouUserName
	mavenRepoKey=YourPassword
	```

### Make

run `make check` for mysql test or `make sqlserver check`

This will make sure a fresh db is started with `db-start` and run the standard `./gradlew check`. 
To run without `make` see the next sections

### Running Docker DB

To manuall run the db

- `make db-start` or `./build.sh db-start` will fire up a mysql database
- `make sqlserver db-start` or `./build.sh db-start sqlserver` will fire up a sql-server database

Both will be accesible on 127.0.0.1 on default ports with password for root or sa as 123Foobar. These dockers images have both `rcm_9ci_dev` and `rcm_9ci_test`

### Running Tests

Once you have a db docker running you can test using the following:

- `./gradlew check` will do the full monty against mysql
- `./gradlew -DBMS=sqlserver check` will do the full monty against Sql Server

#### Grails

install the Grails version matching whats in gradle.properties using [sdkman](https://sdkman.io)

- `grails test-app`

#### Make

while gradle is the build tool behind spring/grails, make is used for docker and setting up env for testing

Target              | Description
--------------------|------------------------------------
builder-remove      | stops and removes the jdk-builder docker
builder-shell       | opens up a shell into the jdk-builder docker
builder-start       | start the docker jdk-builder if its not started yet, unless USE_DOCK_BUILDER=false
check               | makes sure db is started and runs gradlew check
clean               | removes build dir
db-down             | stop and remove the docker DOCK_DB_BUILD_NAME
db-start            | starts the DOCK_DB_BUILD_NAME db if its not started yet, unless USE_DOCK_DB_BUILDER=false
dock-remove-all     | runs `make dock-remove` for sqlserver and mysql
dock-remove         | stops/removes the builder and DB dockers
log-vars            | logs the BUILD_VARS in the build/make env
startup             | calls db-start if USE_DOCK_DB_BUILDER=true and builder-start if USE_DOCK_BUILDER=true
