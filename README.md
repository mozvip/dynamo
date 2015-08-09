# README #

### What is this repository for? ###

This is the main development repository for Dynamo.
Dynamo is an attempt at creating a single application that has the main functionnalities of SickBeard, CouchPotato, Headphones, ...

It is coded in Java 7.

### How do I get set up? ###

### Builds are automatically uploaded here : http://ns331806.ip-37-59-55.eu/ ###

If you want to build yourself, Maven is the easiest way, you can create an Eclipse workspace too if you want.

the "dynamo" project is the parent pom.

cd into this project folder then


```
mvn clean
mvn install -DskipTests=true
```
Dynamo is built in a single jar that you can run with:


```
java -jar {your-maven-repository}\dynamo\dynamo-web\0.0.1-SNAPSHOT\dynamo-web-0.0.1-SNAPSHOT.jar
```

It is recommended however to run Dynamo with more available memory and the server JVM tuning:

```
java -Xmx512m -server -jar {your-maven-repository}\dynamo\dynamo-web\0.0.1-SNAPSHOT\dynamo-web-0.0.1-SNAPSHOT.jar
```

Dynamo is then available at http://localhost:8081/welcome.jsf

* Configuration

The port is configurable in the app ( use the configuration menu ).
All configuration is done there.

You should start with "Plugins Configuration", select the plugins you want to use there there, save, and then continue with the rest of the configuration items.

* Dependencies

The only requirement is Java 7. Everything else is obtained through Maven dependencies.

* Database configuration

Dynamo uses 2 embbded h2 databases : one for the application data and one as a simple http cache. Both are automatically created (with liquibase) on the first run, in the current folder.

The file that contains the application database is dynamo.mv.db.
