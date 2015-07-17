/*
 * Copyright (C) 2014 Giovanni D'Italia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package mongosensors_server;



import com.mongodb.util.JSON;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.Timer;
import java.net.URL;
import java.util.Iterator;

/**
 *
 * @author giovanni
 */

public class CollectThread extends Thread{
    
    private MapReduce mapReduceOBJ;
    private String[] senso;
    
    private String db;
    private String tagsColl;
    private String tag;
    private String id;
    private String sourcecollection;
    private String collection;
    private String discardColl;
    private String typeColl;
    private String lastupdate;
    private String battery;
    private JSONArray location;
    private JSONArray URL;
    private MongoTools client;
    private JSONObject santanderJson;
    private JSONArray santanderSensorArray;
    private SimpleDateFormat dateFormat;
    private Calendar cal;
    private int lapse;
    private int[] sensorsStatistics={0,0};
    ArrayList<String> idlist = new ArrayList<String> ();
    ArrayList<String> key = new ArrayList<String> ();
    ArrayList<String> keyURL = new ArrayList<String> ();
    ArrayList<String> tags = new ArrayList<String> ();
    ArrayList<String> currentags = new ArrayList<String> ();

    public CollectThread(JsonFile conf){
        try{
            this.client = new MongoTools("mongodb://"+conf.getJson().get("username").toString()
                    +":"+conf.getJson().get("password").toString()
                    +"@"+conf.getJson().get("IP").toString()+"/"
                    +conf.getJson().get("userdatabase").toString()
            );
            this.db = conf.getJson().get("maindatabase").toString();
            this.collection = conf.getJson().get("maincollection").toString();
            this.discardColl = conf.getJson().get("discardcollection").toString();
            this.typeColl = conf.getJson().get("typecollection").toString();
            this.tagsColl = conf.getJson().get("tagscollection").toString();
            this.tag = conf.getJson().get("tag_key").toString();
            this.id=conf.getJson().get("id_key").toString();
            this.sourcecollection=conf.getJson().get("sourceinfo").toString();
            this.URL = conf.getJson().getJSONArray("URL");
            this.lapse = Integer.parseInt(conf.getJson().get("lapse").toString());
            this.lastupdate = conf.getJson().get("update_key").toString();
            this.battery = conf.getJson().get("battery_key").toString();
            this.location = conf.getJson().getJSONArray("loc_key");
            //Creo la connessione a MongoDB
            this.client.setDatabase(this.db);
            this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.cal = new GregorianCalendar();
            try{
                JSONArray tagsSensor = this.client.getJsonArrayOnceFromCollection(
                this.tagsColl,this.tag);
                for(int i = 0;i<tagsSensor.length();i++){
                    this.tags.add(tagsSensor.get(i).toString());
                }
            }
            catch(Exception e){this.tags.add("None");}
        }
        catch(Exception e){
            System.out.print("("+new GregorianCalendar().getTime()+") -> ");
            System.out.print("error: "+e+"\n");
        }
        }



    @Override
    public void run(){
        while(true){
            this.updateDate();
            this.client.removeFromCollection(this.sourcecollection, "{}");
            try{
                //Connect to the url
                for(int z=0;z<this.URL.length();z++){
                JsonUtils santanderJsonRaw = new JsonUtils().read(new URL(this.URL.get(z).toString()));
                Iterator k=santanderJsonRaw.getJson().keys();
                this.keyURL.clear();
                while( k.hasNext() ) {
                    Object s = k.next();
                    if(!s.toString().equals("info"))
                    this.keyURL.add(s.toString());
                }
                //extract the array and the list of types of sensors
                for(int g=0;g<this.keyURL.toArray().length;g++){
                    this.printLog("get "+this.keyURL.toArray()[g].toString()+
                                  " from: "+this.URL.get(z).toString());
                JSONArray santanderJsonArray = santanderJsonRaw.getArray(this.keyURL.toArray()[g].toString());
                this.santanderSensorArray = this.client.getJsonArrayOnceFromCollection(this.typeColl,"type");
                for(int i=0;i<santanderJsonArray.length();i++){
                    try{
                        //For each string array " markers " reformat the string JSON
                        this.santanderJson = (JSONObject) santanderJsonArray.get(i);
                        this.reformatJsonString();
                        //enter the modified string in JSON  in mongoDB collection 
                        if(!this.issueControl()){ //check some problems
                            this.createTagCollection();
                            this.client.insertInCollection(this.collection, this.santanderJson);
                            this.idlist.add(this.santanderJson.get(this.id).toString());
                            this.sensorsStatistics[0]++;
                        }
                        //put the strings of sensors ignored in the collection of waste
                        else{
                            this.client.insertInCollection(this.discardColl, this.santanderJson);
                            this.sensorsStatistics[1]++;
                        }
                    }
                    catch(Exception e) {
                            this.sensorsStatistics[1]++;
                    }
                }
            }
            JSONObject jobj = new JSONObject();
            jobj.put("URL",this.modifyURL(z));
            jobj.put("ids",new JSONArray(this.idlist.toArray()));
            jobj.put("tags",new JSONArray(this.currentags.toArray()));
            try{
            jobj.put("info",santanderJsonRaw.getJson().get("info"));
            }
            catch(Exception e){}
            
            this.client.insertInCollection(this.sourcecollection, jobj);
            this.idlist.clear();
            this.currentags.clear();
            }
            this.printLog(" collected properly: "+this.sensorsStatistics[0]+
                          " ignored: "+this.sensorsStatistics[1]+"\n");//print some info
            this.resetStatistics();
            //wait for "lapse" seconds
            Thread.sleep(lapse*1000);
            }
            catch(Exception e){
                System.out.print(e);
                break;
            }
        }
    }

    private boolean issueControl(){
        try{
            if((!this.santanderJson.has(this.id)) ||
                (!this.santanderJson.has(this.tag)) ||
                this.santanderJson.length()<5){
                return true;
            }
            else{
            String jsonDate = this.santanderJson.get(this.lastupdate).toString();
            Date date = this.dateFormat.parse(jsonDate);
            if(date.before(this.cal.getTime())){
                return true;
            }
            if(Double.parseDouble(this.santanderJson.get(this.battery).toString())==0){
                return true;
            }
           }
        }
        catch(Exception e){
            return false;
        }
        return false;
    }

    private void resetStatistics(){
    for(int i=0;i<this.sensorsStatistics.length;i++){sensorsStatistics[i]=0;}
    }
    private void updateDate(){
        this.cal = new GregorianCalendar();
        this.cal.set(Calendar.MINUTE, Integer.parseInt(new SimpleDateFormat("mm").format(this.cal.getTime()))-((this.lapse/60)*2));
    }
    
    private void printLog(String string){
        System.out.print("("+new GregorianCalendar().getTime()+") -> ");
        System.out.println(string);
    }

    private void reformatJsonString(){
        try{
        Double[] loc = {
            Double.parseDouble(this.santanderJson.get(this.location.get(0).toString()).toString()),
            Double.parseDouble(this.santanderJson.get(this.location.get(1).toString()).toString())
        };
        this.santanderJson.put("loc", loc);
        this.santanderJson.remove(this.location.get(0).toString());
        this.santanderJson.remove(this.location.get(1).toString());
        }
        catch(Exception e){}
        
        //Start - Essential part of the operation with the network of santander
        try{
        String content = this.santanderJson.get("content").toString();
        this.santanderJson.remove("content");
        ContentParser contentObj = new ContentParser(content);

        //Inserisco l'informazione last update se presente
        if(content.indexOf(this.lastupdate)> -1){
            this.santanderJson.put(this.lastupdate, contentObj.parseLastUpdate());
        }
        if(content.indexOf(this.battery)> -1){
            Double bat=Double.parseDouble(contentObj.parse(this.battery));
            this.santanderJson.put(this.battery, bat);
        }
         //Aggiungo nella stringa JSON il tipo di sensore(se presente) e il relativo valore
        for(int j=0;j<this.santanderSensorArray.length();j++){
           
            //Cerco nel campo content ogni tipo di sensore presente nel DB
            String sensorType = this.santanderSensorArray.get(j).toString();
            if(content.indexOf(sensorType)> -1){
                String value = contentObj.parse(sensorType);
                //Aggiungo nella stringa JSON il tipo di sensore(se presente) e il relativo valore
                this.santanderJson.put(sensorType, Double.parseDouble(value));
            }   
        }
        }
        catch(Exception e){}
        //End - Essential part of the operation with the network of santander

       
        Iterator k=this.santanderJson.keys();
        this.key.clear();
        while( k.hasNext() ) {
        this.key.add(k.next().toString());
        }
        for (int z = 0; z < this.key.toArray().length; z++) {
        int flag=0;
            for (int j = 0; j < this.santanderSensorArray.length(); j++) {
                String sensorType = this.santanderSensorArray.get(j).toString();
                if(this.key.toArray()[z].toString().equals(sensorType) || 
                        this.key.toArray()[z].toString().equals("loc") ||
                        this.key.toArray()[z].toString().equals(this.id)  ||
                        this.key.toArray()[z].toString().equals(this.tag)  ||
                        this.key.toArray()[z].toString().equals(this.lastupdate)||
                        this.key.toArray()[z].toString().equals(this.battery)){
                        flag = 1;
                }
            }
            if(flag!=1){
            this.santanderJson.remove(this.key.toArray()[z].toString());
            }
        }   
    }
    
    private void createTagCollection(){
                try{
            String tag = this.santanderJson.get(this.tag).toString();
            int f = 0;
            for(int i=0;i<this.tags.toArray().length;i++){
            if(tag.equals(this.tags.toArray()[i].toString()))f=1;
        }
            if(f!=1){
                this.tags.add(tag);
                this.client.removeFromCollection(this.tagsColl,"{}");
                this.client.insertInCollection(this.tagsColl,new JSONObject().put(this.tag, new JSONArray(this.tags)));
            }
            f=0;
            for(int i=0;i<this.currentags.toArray().length;i++){
            if(tag.equals(this.currentags.toArray()[i].toString()))f=1;
            }
            if(f!=1){this.currentags.add(tag);System.out.println(tag);}
        }
        catch(Exception e){}
    }

    private String modifyURL(int index) {
        return this.URL.get(index).toString().replace(".", "<dot>");
        
    }
}
