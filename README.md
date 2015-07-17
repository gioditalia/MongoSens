# MongoSens
Software that collects sensor data in JSON format and collects in a database MongoDB

#Let's start
The key part of the software is mongosens the server.
Also crucial is the configuration file: config.json
![alt tag](https://github.com/inna92/MongoSens/blob/master/server/config.png)

The first part is used to access the database.
The second part configures collections used.
It defines the key fundamentals for the sensors and the control of errors.
Finally sets the time of collection and minimal configuration to respond to client.

#Important
The software comes as an exercise with the databse MongoDB and does not provide for further developments.
