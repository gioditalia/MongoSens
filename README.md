# MongoSens
Java software that collects sensor data in json format and collects in a database MongoDB

#Let's start
The key part of the software is mongosens the server.<br/>
Also crucial is the configuration file: config.json<br/>
![alt tag](https://github.com/inna92/MongoSens/blob/master/server/config.png)<br/>
The first part is used to access the database.<br/>
  - "IP":"111.222.333.444"
  - "username":"admin"
  - "password":"secretpass"
  - "userdatabase":"admin"<br/><br/>

The second part configures collections used.<br/>
  - "maindatabase":"mongosens"
  - "maincollection":"sens"
  - "typecollection":"type"
  - "discardcollection":"discard"
  - "tagscollection":"info"
  - "sourceinfo":"sourcecollection"
  - "URL":["http://maps.smartsantander.eu/getdata.php","http://some.url"]<br/><br/>

It defines the key fundamentals for the sensors and the control of errors.<br/>
  - "tag_key":"tags"
  - "update_key":"Last update"
  - "battery_key":"Battery level"
  - "loc_key":["longitude","latitude"]
  - "id_key":"id"<br/><br/>

Finally sets the time of collection and minimal configuration to respond to client.<br/>
  - "lapse":1800
  - "port":8888
  - "bind_IP":"127.0.0.1"<br/><br/>


