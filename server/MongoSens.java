/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mongosensors_server;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author inna
 */
public class MongoSens {
    
    static mongosensors_server.JsonFile conf;
    static Thread collection , server;
    static ServerSocket s;
    static Socket sock;
    static int port;
    static String address;
    /**
     * @param args the command line arguments
     */
    
    public static void main(String[] args) throws Exception{
        try{
        conf = new mongosensors_server.JsonFile("config.json").read();
        address = conf.getJson().get("bind_IP").toString();
        port = Integer.parseInt(conf.getJson().get("port").toString());
        collection = new mongosensors_server.CollectThread(conf);
        collection.start();
        s = new ServerSocket(port,50,InetAddress.getByName(address));
        System.out.print("("+new GregorianCalendar().getTime()+") -> ");
        System.out.print("listening on: "+ address+":"+port+"\n");
        }
         catch(Exception e){
            System.out.print("("+new GregorianCalendar().getTime()+") -> ");
            System.out.print("error: "+e);
        }
        while(true){
        try{
        sock = s.accept();
        System.out.print("("+new GregorianCalendar().getTime()+") -> ");
        System.out.print("connection from "+sock.getInetAddress()+":");
        System.out.print(sock.getPort()+"\n");
        server = new mongosensors_server.ConsoleThread(conf,sock);
        server.start();
        }
        catch(Exception e){
            System.out.print("("+new GregorianCalendar().getTime()+") -> ");
            System.out.print("error: "+e);
            continue;
        }
        }
    }
}
   
