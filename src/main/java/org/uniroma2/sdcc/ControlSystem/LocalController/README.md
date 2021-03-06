This package contains a *node.js* application representing the local controller placed on a lamp. To communicate with the AWS IoT service you need to put the root-CA.crt certificate in the ./certs directory. You can download the certificate from you AWS IoT console.

To install dependencies move into LocalController directory:
```
$ cd {$HELIOS_HOME}/src/java/main/org/uniroma2/sdcc/ControlSystem/LocalController
$ npm install
```

To install *device.js*:
``` 
$ node device.js
```

Options to start device are:
```
  -g, --aws-region=REGION          AWS IoT region\n' +
  -i, --client-id=ID               use ID as client ID\n' +
  -H, --host-name=HOST             connect to HOST (overrides --aws-region)\n' +
  -p, --port=PORT                  connect to PORT (overrides defaults)\n' +
  -P, --protocol=PROTOCOL          connect using PROTOCOL (mqtts|wss)\n' +
  -r, --reconnect-period-ms=VALUE  use VALUE as the reconnect period (ms)\n' +
  -K, --keepalive=VALUE            use VALUE as the keepalive time (seconds)\n' +
```
If not specified the default value are used. For the region
The default AWS region is *eu-west-1*.
The default communication protocol is MQTTS.

You can start many *device.js* processes in the same terminal using the *start.sh* script:
```
$ chmod +777 start.sh
$ ./start.sh
```
You must have [pm2](http://pm2.keymetrics.io/) NodeJs Process Manager tool installed on your machine. 
Install it using *npm*:
```
$ npm install pm2 -g
```
To stop the NodeJs process(es):
```
$ pm2 stop device
```
