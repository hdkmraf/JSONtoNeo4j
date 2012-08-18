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
         if(args.length != 2){
            System.out.println("Usage: JSONtoNeo4j directory_name database_name index_value");
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
