# Dynamo #

[![Build Status](https://api.travis-ci.org/mozvip/dynamo.svg?branch=master)](https://travis-ci.org/mozvip/dynamo)

### What is this repository for? ###

This is the main development repository for Dynamo.
Dynamo is an highly experimental attempt at creating a single application that has the main functionnalities of SickBeard, CouchPotato, Headphones and other similar download automation apps.

It is coded in Java 8.

### How do I get set up? ###

If you want to build yourself, Maven is the easiest way.

the "dynamo" project is the parent pom.

cd into this project folder then


```
mvn install 
```
Dynamo is built in a single jar

* Dependencies

The only requirement is Java 8. Everything else is obtained through Maven dependencies.

* Database configuration

Dynamo uses 3 embbeded h2 databases :
* one for the application data : dynamo.mv.db
* one for the http-cache (can grow big!) : httpclient-cache.mv.db
* one for the "core" data (mostly technical stuff like logs) : core.mv.db

Both are automatically created (with liquibase) on the first run, in the current folder.

The file that contains the application database is dynamo.mv.db.

License
=======

    Copyright 2015 mozvip@gmail.com

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

