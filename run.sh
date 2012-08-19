#!/bin/sh

java -server -Xmx2048m -cp target/JSONtoNeo4j-1.0.jar:target/dependency/\* org.neo4j.jsontoneo4j.JsonImport $*