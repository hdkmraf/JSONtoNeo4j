JSONtoNeo4j
===========
This tool allows you to generate a Neo4j graph from .json and .js files.
It will read all the .json and/or .js files in a path.
You have to specify a property as index for Neo4j, JSON objects with that property will be imported as nodes.
Objects without it will be ignored.
The database will be reinitialized (deleted) every time you run the importer.

Building
--------
<pre> ./build.sh
</pre>

Running
-------
<pre>./run.sh path database index
</pre>