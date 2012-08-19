/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.neo4j.jsontoneo4j;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.impl.util.FileUtils;


/**
 *
 * @author rafael
 */
public class Graph {
    
    private String DB_PATH;
    String greeting;
    private GraphDatabaseService graphDb;    
    private String DIR; 
    private Index<Node> idIndex;
    private String ID_KEY;
    
    
    public Graph(String dir, String db_path, boolean newDb, String idKey){
        DIR = dir;
        DB_PATH = db_path;
        if(newDb){
            clearDb();
        }
        // START SNIPPET: startDb
        //graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
        graphDb = new EmbeddedGraphDatabase( DB_PATH );   
        ID_KEY = idKey;
        idIndex = graphDb.index().forNodes(ID_KEY);
        registerShutdownHook( graphDb );
        // END SNIPPET: startDb      
    }
    
    
   private enum relationshipTypes implements RelationshipType{
       relation
   }
                        
   private Node getNodeById (String id){
        IndexHits<Node> hits = idIndex.get(ID_KEY, id);
        Node node = hits.getSingle();
        hits.close();
        return node;
   }
   
   private void makeRelationship(Node x, Node y, RelationshipType rt){
       Transaction tx = graphDb.beginTx();
       try{
           x.createRelationshipTo(y, rt);
           tx.success();     
       } catch (Exception ex){
           System.out.println(ex.getMessage());
       } finally {
           tx.finish();
       }  
   }
      
    
   private void clearDb(){
        try
        {
            FileUtils.deleteRecursively( new File( DB_PATH ) );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }
    
    void shutDown(){
        System.out.println();
        System.out.println( "Shutting down database ..." );
        // START SNIPPET: shutdownServer
        graphDb.shutdown();
        // END SNIPPET: shutdownServer
    }
    
     // START SNIPPET: shutdownHook
    private static void registerShutdownHook( final GraphDatabaseService graphDb ){
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running example before it's completed)
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }
    // END SNIPPET: shutdownHook
    
    private Node createUpdateIndexNode(String id, Map<String, Object> properties){       
        IndexHits<Node> hits = idIndex.get(ID_KEY, id);
        Node node = hits.getSingle();
        hits.close();           
        Transaction tx = graphDb.beginTx();
        try {
            if(node==null){
                node = graphDb.createNode();               
                node.setProperty(ID_KEY, id);           
                idIndex.add(node, ID_KEY, id); 
            }           
            for(Entry<String, Object> entry: properties.entrySet()){
                String key = entry.getKey();
                Object value = entry.getValue();
                if(value!=JSONObject.NULL && key!=JSONObject.NULL) {
                    node.setProperty(key, value);
                }                
            }
            tx.success();       
        } catch (Exception ex){
            System.out.println("createUpdateIndexNode:" + ex.getMessage());            
        } finally {
            tx.finish();
        }
        return node;
    }
    
     private Node createNotIndexNode(){                   
        Node node = null;
        Transaction tx = graphDb.beginTx();
        try {            
            node = graphDb.createNode();                                            
            tx.success();       
        } catch (Exception ex){
            System.out.println("createNotIndexNode:" + ex.getMessage());            
        } finally {
            tx.finish();
        }
        return node;
    }
     
     private void deleteNode(Node node){
        Transaction tx = graphDb.beginTx();
        try {            
            node.delete();            
            tx.success();       
        } catch (Exception ex){
            System.out.println(ex.getMessage());            
        } finally {
            tx.finish();
        }
     }
     
     private void deleteRelationship(Relationship rel){
        Transaction tx = graphDb.beginTx();
        try {            
            rel.delete();            
            tx.success();       
        } catch (Exception ex){
            System.out.println(ex.getMessage());            
        } finally {
            tx.finish();
        }
     }
    
    public void startJSONArray(JSONArray array){
        for(int i=0; i<array.length();i++){
            try {
                JSONObject object = (JSONObject) array.get(i);
                analyseJSONObject(object);                
            } catch (JSONException ex) {
                Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, null, ex);
            }            
        }        
    }
    
    public void analyseJSONArray(JSONArray array, Node parentNode, String key){
        for(int i=0; i<array.length();i++){
            try {
                JSONObject object = (JSONObject) array.get(i);
                Node n2 = analyseJSONObject(object);
                if (n2!=null) {
                    makeRelationship(parentNode, n2, DynamicRelationshipType.withName(key));
                }
            } catch (JSONException ex) {
                Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, null, ex);
            }            
        }        
    }
    
    private Node analyseJSONObject(JSONObject object){
        String id = null;
        Node node = null;       
        Map<String,Object> properties = new HashMap<String,Object>();
        try {
            id = (String) object.get(ID_KEY);
            node = createUpdateIndexNode(id, properties);
           // System.out.println(id);
        } catch (JSONException ex) {
            node = createNotIndexNode();
        }
        
        Iterator keys = object.keys();
        while(keys.hasNext()){
            String key = (String) keys.next();
            try {
                JSONObject o2 = (JSONObject) object.get(key);
                Node n2 = analyseJSONObject(o2);
                if(node != null && n2!=null){
                    if(n2.hasProperty(ID_KEY)){
                        makeRelationship(node, n2, DynamicRelationshipType.withName(key));
                    } else{                        
                        for(Relationship rel: n2.getRelationships(Direction.OUTGOING)){
                            makeRelationship(node, rel.getEndNode(), DynamicRelationshipType.withName(key));
                            deleteRelationship(rel);
                        }
                        deleteNode(n2);
                    }
                }
            } catch (Exception ex) {
                JSONArray array;
                try {
                    array = (JSONArray) object.get(key);
                    for(int i=0; i<array.length();i++){
                    try {
                        JSONObject o2 = (JSONObject) array.get(i);
                        Node n2 = analyseJSONObject(o2);
                        if (node != null && n2!=null) {
                            if(n2.hasProperty(ID_KEY)){
                                makeRelationship(node, n2, DynamicRelationshipType.withName(key));
                            } else{                        
                                for(Relationship rel: n2.getRelationships(Direction.OUTGOING)){
                                    makeRelationship(node, rel.getEndNode(), DynamicRelationshipType.withName(key));
                                    deleteRelationship(rel);
                                }
                                deleteNode(n2);
                            }
                        }
                        } catch (JSONException ex2) {
                            Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, null, ex);
                        }            
                    }  
                } catch (Exception ex1) {
                    try {
                        properties.put(key, object.get(key));
                    } catch (JSONException ex2) {
                        Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, null, ex2);
                    }
                }
            }                        
        }
        if(id!=null) {
            node = createUpdateIndexNode(id, properties);
        }
        return node;
    }
   
}
