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
import org.json.*;
import java.util.*;
import com.mongodb.*;
import com.mongodb.util.JSON;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoClient;
import com.mongodb.client.*;
/**
 *
 * @author giovanni
 */



public class MongoTools extends MongoClient{
    private DB db;
    
    
    public MongoTools(String s){
       super(new MongoClientURI(s));
    }

    
    public void setDatabase(String database){
        this.db = super.getDB(database);
    }
    
    public DB getDatabase(){
        return this.db;
    }

   
    
    
    public void insertInCollection(String collection,JSONObject json){
        DBObject jsonDoc = (DBObject) JSON.parse(json.toString());
        this.db.getCollection(collection).insert(jsonDoc); 
    }

    public void insertInCollection(String collection,String data){
        DBObject jsonDoc = (DBObject) JSON.parse(data);
        this.db.getCollection(collection).insert(jsonDoc); 
    }
    
    public DBCursor getFromCollection(String collection,String query){
       return this.db.getCollection(collection).find((DBObject) JSON.parse(query));
    }    
    
    public void removeFromCollection(String collection,String query){
       this.db.getCollection(collection).remove((DBObject) JSON.parse(query));
    }
    
    public JSONArray getJsonArrayOnceFromCollection(String collection,String arrayKey){
        DBCursor sensorType = this.getFromCollection(collection,"");
        sensorType.hasNext();
        String dd = sensorType.next().toString();
        return new JSONObject(dd).getJSONArray(arrayKey);
    }

    public MapReduceOutput mongoMapReduce(String collection,String map,String reduce,DBObject query){
    DBCollection col = db.getCollection(collection);
    MapReduceCommand cmd = new MapReduceCommand(col, map, reduce,null, MapReduceCommand.OutputType.INLINE,query);
    return col.mapReduce(cmd);
    }


}
