package com.hdkmraf.jsontoneo4j;

import java.io.File;
import org.json.JSONArray;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
         if(args.length != 3){
            System.out.println("Usage: JSONtoNeo4j path database index");
            System.out.println("path - Where the .json or .js files are located");
            System.out.println("database - Neo4j database, it will be created inside path, if a file exists with that name it will be overwriten");
            System.out.println("index - An attribute in each JSON object that will act as index for all the nodes in the database. If the attribute is missing, the object will not be considred a node");
            return;
        }
        
        String dir = args[0];
        String dbName = args[1];
        String index = args[2];
        
        //String dir = "/home/rafael/jsontoneo4j_dump/";
        //String dbName = "graph.db";
         
        System.out.println("Starting graph");
        Graph graph = new Graph(dir, dir+dbName, true, index);        
            
        File folder = new File(dir);
        File[] listOfFiles = folder.listFiles();
        for(int i=0; i<listOfFiles.length; i++){
            if(listOfFiles[i].isFile()){
                String fileName = listOfFiles[i].getPath();
                if(fileName.endsWith(".js") || fileName.endsWith(".json")){            
                    System.out.println("Reading "+fileName);    
                    JSONArray array = Helper.stringToJSONArray(Helper.readFile(fileName));
                    graph.startJSONArray(array);
                }
            }
        }
        
        graph.shutDown();        
    }   
}
