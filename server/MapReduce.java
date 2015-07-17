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

import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import java.util.ArrayList;
import java.util.List;
import static mongosensors_server.MongoSens.conf;

/**
 *
 * @author giovanni
 */
public class MapReduce {
    private final MongoClient client;
    private final String collection;
    private String tag;
    private String map;
    private String reduce;
    private String id;
    static mongosensors_server.JsonFile conf;
    
    public MapReduce(MongoClient client,String collection) throws Exception{
        this.client = client;
        this.collection = collection;
        conf = new mongosensors_server.JsonFile("config.json").read();
        this.tag = conf.getJson().get("tag_key").toString();
        this.id=conf.getJson().get("id_key").toString();

    }
    
    
    
    public String[] getInfoForTags(){     
        //LIST TAGS:TITLE
        String map ="function () {"+
        "value = { ids: [this.id] };"+
        "emit(this.tags, value);"+
        "}";
 
        String reduce="function(key, values) {"+
        "title_list = { ids: [] };"+
        "for(var i in values) {"+
        "title_list.ids = values[i].ids.concat(title_list.ids);"+
        "}"+
        "return title_list;"+
        "}";
         
        List<String> stringValue = new ArrayList<String>();
        int i = 0;
        for(DBObject o : ((MongoTools)this.client).mongoMapReduce(this.collection,map, reduce,null).results())
        {
         stringValue.add(o.toString());
         i++;
        }
        String[] array = new String[stringValue.size()];
        stringValue.toArray(array);
        return array;
    }
    
    
    public String[] getAverageValue(String sensorType,String tags,String id,double range){   
        //AVERAGE TAGS:SENROSTYPE
        DBObject  query = (DBObject) JSON.parse("{}");
        if(!tags.equals("")){query.put(this.tag,tags);}
        if(!id.equals("")){query.put(this.id, id);}
        
        map ="function () {"+
        "emit(this.tags, this['"+sensorType+"']);"+
        "}";
        
        reduce="function(key, values) {"+
        "title_list =0;"+
        "j=0;"+
        "mid = values.length/2 | 0;"+
        "values = values.sort();"+
        "for(var i in values) {"+
        "if(values[i]>(values[mid]-"+range+") && values[i]<(values[mid]+"+range+")){"+
        "title_list += values[i];"+
        "j++}"+
        "}"+
        "return title_list/j;"+
        "}";
        
        List<String> stringValue = new ArrayList<String>();
        int i = 0;
        for(DBObject o : ((MongoTools)this.client).mongoMapReduce(this.collection,map, reduce,query).results())
        {
         stringValue.add(o.toString());
         i++;
        }
        String[] array = new String[stringValue.size()];
        stringValue.toArray(array);
        return array;
    }
    
    public String[] getAverageValue(String sensorType,String tags,String id,double range,String[] time){   
        //AVERAGE TAGS:SENROSTYPE the query can define a time period
        DBObject  query = (DBObject) JSON.parse("{'Last update':{$gt:'"+time[0]+"',$lt:'"+time[1]+"'}}");
        if(!tags.equals("")){query.put(this.tag,tags);}
        if(!id.equals("")){query.put(this.id, id);}
        System.out.println(query.toString());
        
        map ="function () {"+
        "emit(this.tags, this['"+sensorType+"']);"+
        "}";
 
        reduce="function(key, values) {"+
        "title_list =0;"+
        "j=0;"+
        "mid = values.length/2 | 0;"+
        "values = values.sort();"+
        "for(var i in values) {"+
        "if(values[i]>(values[mid]-"+range+") && values[i]<(values[mid]+"+range+")){"+
        "title_list += values[i];"+
        "j++}"+
        "}"+
        "return title_list/j;"+
        "}";
        
        List<String> stringValue = new ArrayList<String>();
        int i = 0;
        for(DBObject o : ((MongoTools)this.client).mongoMapReduce(this.collection,map, reduce,query).results())
        {
         stringValue.add(o.toString());
         i++;
         
         
        }
        String[] array = new String[stringValue.size()];
        stringValue.toArray(array);
        return array;
    }
    
    public String[] getAverageValue(String sensorType,String tags,double range,double[] loc,double radius){   
        //AVERAGE TAGS:SENROSTYPE the query can define geographical area
        DBObject  query;
        radius = radius/6378160.0;
        if(!tags.equals("")){
        query = (DBObject) JSON.parse("{loc: { $geoWithin: { $centerSphere: [["+loc[0]+","+loc[1]+"],"+radius+"] } },'"+this.tag+"':'"+tags+"'}");
        }
        else{
        query = (DBObject) JSON.parse("{loc: { $geoWithin: { $centerSphere: [["+loc[0]+","+loc[1]+"],"+radius+"]}}}");
        }
        
        map ="function () {"+
        "emit(this.tags, this['"+sensorType+"']);"+
        "}";
 
        reduce="function(key, values) {"+
        "title_list =0;"+
        "j=0;"+
        "mid = values.length/2 | 0;"+
        "values = values.sort();"+
        "for(var i in values) {"+
        "if(values[i]>(values[mid]-"+range+") && values[i]<(values[mid]+"+range+")){"+
        "title_list += values[i];"+
        "j++}"+
        "}"+
        "return title_list/j;"+
        "}";
        
        List<String> stringValue = new ArrayList<String>();
        int i = 0;
        for(DBObject o : ((MongoTools)this.client).mongoMapReduce(this.collection,map, reduce,query).results())// <!>
        {
         stringValue.add(o.toString());
         i++;
        }
        String[] array = new String[stringValue.size()];
        stringValue.toArray(array);
        return array;
    }

    public String[] getAverageValue(String sensorType,String tags,double range,String[] time,double[] loc,double radius){   
        //AVERAGE TAGS:SENROSTYPE the query can define a time period and geographical area
        DBObject  query;
        radius = radius/6378160.0;
        if(!tags.equals("")){
        query = (DBObject) JSON.parse("{'Last update':{$gt:'"+time[0]+"',$lt:'"+time[1]+"'},loc: { $geoWithin: { $centerSphere: [["+loc[0]+","+loc[1]+"],"+radius+"] } },'"+this.tag+"':'"+tags+"'}");
        }
        else{
        query = (DBObject) JSON.parse("{'Last update':{$gt:'"+time[0]+"',$lt:'"+time[1]+"'},loc: { $geoWithin: { $centerSphere: [["+loc[0]+","+loc[1]+"],"+radius+"] } }}");
        }
        
        map ="function () {"+
        "emit(this.tags, this['"+sensorType+"']);"+
        "}";
 
        reduce="function(key, values) {"+
        "title_list =0;"+
        "j=0;"+
        "mid = values.length/2 | 0;"+
        "values = values.sort();"+
        "for(var i in values) {"+
        "if(values[mid]-"+range+"<values[i] && values[mid]+"+range+">values[i]){"+
        "title_list += values[i];"+
        "j++}"+
        "}"+
        "return title_list/j;"+
        "}";
        
        List<String> stringValue = new ArrayList<String>();
        int i = 0;
        for(DBObject o : ((MongoTools)this.client).mongoMapReduce(this.collection,map, reduce,query).results())
        {
         stringValue.add(o.toString());
         i++;
        }
        String[] array = new String[stringValue.size()];
        stringValue.toArray(array);
        return array;
    }
    
    public String[] getBUSInfo(String title,String[] time){     
        //POSITIONS ID:LOC
        String map ="function () {"+
        "value =  {loc : [this.loc]} ;"+
        "emit("+this.id+", value);"+
        "}";
 
        String reduce="function(key, values) {"+
        "title_list = { loc: [] };"+
        "for(var i in values) {"+
        "title_list.loc = values[i].loc.concat(title_list.loc);"+
        "}"+
        "return title_list;"+
        "}";
        
        DBObject  query = (DBObject) JSON.parse("{'Last update':{$gt:'"+time[0]+"',$lt:'"+time[1]+"'},'title':'"+title+"'}");
        List<String> stringValue = new ArrayList<String>();
        int i = 0;
        for(DBObject o : ((MongoTools)this.client).mongoMapReduce(this.collection,map, reduce,query).results())
        {
         stringValue.add(o.toString());
         i++;
        }
        String[] array = new String[stringValue.size()];
        stringValue.toArray(array);
        return array;
    }
}
