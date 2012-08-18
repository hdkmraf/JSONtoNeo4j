/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hdkmraf.jsontoneo4j;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author rafael
 */
public class Helper {
    
    
    public static void waitSeconds(long seconds){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/DD HH:mm:ss");
        Date date = new Date();
        System.out.println("Waiting from "+ dateFormat.format(date));
        try {
            Thread.sleep(seconds*1000);
        } catch (InterruptedException ex) {}
    }
    
    public static void writeToFile (String file, String line, boolean newFile){
        boolean append = true;
        if(newFile) {
            append = false;
        }
        FileWriter fstream;
        try {
            fstream = new FileWriter(file, append);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(line);
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public static String readFile(String file){
        StringBuilder text = new StringBuilder();
        String NL = System.getProperty("line.separator");
        try {
            Scanner scanner = new Scanner(new FileInputStream(file));
            while (scanner.hasNextLine()){
                text.append(scanner.nextLine()).append(NL);
            }
            scanner.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return text.toString();        
    }            
    
    public static String makeRequest (String request, String proxy, int port){
        URL fullRequest;
        String response = null;
        try {
            URI uri = new URI(request);
            if (proxy==null) {
                fullRequest = uri.toURL();
            }
            else {
                fullRequest = new URL("http", proxy, port, uri.toString());
            }
            System.out.println(fullRequest);
            URLConnection ac = fullRequest.openConnection();
            HttpURLConnection httpConn = (HttpURLConnection) ac;
            if (httpConn.getResponseCode()>=500){
                return "500";
            } else if (httpConn.getResponseCode()>=400){
                return "400";
            }
            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                    ac.getInputStream()));
            String inputLine;
            response = "";
            while((inputLine = in.readLine()) != null){
                response += inputLine;
            }
            in.close();
        } catch (IOException ex) {            
            return null;
        }catch (URISyntaxException ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }
    
     public static void convertToBatchCSV(String DIR, String file){
        String NL = System.getProperty("line.separator");
        String[] lines = Helper.readFile(DIR+file).split(NL);
        String  nodesCSV = DIR+"nodes.csv";
        String relsCSV = DIR+"rels.csv";
        System.out.println("Loading "+DIR+file);
        ArrayList<String> nodes = new ArrayList<String>();
     
        Helper.writeToFile(relsCSV, "Start\tEnde\tType"+NL, false);
        for(String line: lines){
             String[] names = line.split(",");
             Helper.writeToFile(relsCSV, names[0]+"\t"+names[1]+"\tONE"+NL, false);
             if(!nodes.contains(names[0])){
                 nodes.add(names[0]);
             }
             if(!nodes.contains(names[1])){
                 nodes.add(names[1]);
             }
        }
        
        Helper.writeToFile(nodesCSV, "Node"+NL, false);
        for (String node: nodes){
            Helper.writeToFile(nodesCSV+NL, node, false);
        }                
    }    
     
    public static JSONArray stringToJSONArray(String string){
        String NL = System.getProperty("line.separator");
        JSONArray array = new JSONArray();
        if(string.startsWith("[")){
            try {
                array = new JSONArray(string);
            } catch (JSONException ex) {
                Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (string.startsWith("{")){
            String [] lines = string.split(NL);
            for(String line:lines){
                try {
                    JSONObject object = new JSONObject(line);
                    array.put(object);
                } catch (JSONException ex) {
                    Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
                }                
            }
            
        }                
        return array;
    }
}
