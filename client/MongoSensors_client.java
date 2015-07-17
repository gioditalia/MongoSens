/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mongosensors_client;

import java.net.Socket;
import javax.swing.JOptionPane;

/**
 *
 * @author inna
 */
public class MongoSensors_client {

    static mongosensors_client.JsonFile conf;
    static String address;
    static int port;
    static Socket s;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception{
        try{
        conf = new mongosensors_client.JsonFile("config.json").read();
        address = conf.getJson().get("IP").toString();
        port = Integer.parseInt(conf.getJson().get("port").toString());
        s = new Socket(address, port);
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new mongosensors_client.JFrame(s).setVisible(true);
            }
        });
         }
        catch(Exception e){
            JOptionPane.showMessageDialog(null, e.toString(), "Error",
                                    JOptionPane.ERROR_MESSAGE);
        System.exit(1);
        }
    }
    
}
