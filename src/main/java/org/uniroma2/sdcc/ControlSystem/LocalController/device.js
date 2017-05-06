

const deviceModule = require('aws-iot-device-sdk');
const cmdLineProcess = require('./lib/cmdline');
const momentRandom = require('moment-random');
const AWS = require('aws-sdk');
const fs = require("fs");

module.exports = cmdLineProcess;

var iot = new AWS.Iot({region: 'eu-west-1', apiVersion: '2015-05-28'});

var lifetimeDate = momentRandom();                          // streetLamp lifetime random generated
var delay = 10000;                                          // publish data every delay time
var thingName;                                              // device name
var thingID;                                                // thing id
var intensity = (Math.round(Math.random() * 1000)) / 10;    // light intensity in percentage
var street = "VIA STRADA"+(Math.round(Math.random()*100));
var cellID;
// cellID -1 with probability 0.5
var n1 = randomInt(1000, 31000);
if (n1 <= (1000 + (30000*0.5)))
    cellID = n1;
else
    cellID = -1;

// state ON (true) with probability 0.8
var n2 = randomInt(0,1);
var state = (n2 <= 0.8);

var street_number;
var number_type;
var n = randomInt(1, 25000);
street_number = n;
// civic street number if random value smaller than 300, else km street number
if (n <= 300)
    number_type = "CIVIC";
else
    number_type = "KM";

var models = ["CFL","LED", "UNKNOWN"];
var model = models[Math.round(Math.random()*10)%3];

var clientTokenUpdate;




/**
 * Create an IoT Thing.
 *
 * @param thingID generated id.
 */
function createThing(thingID) {

    thingName = 'my-device-' + thingID;
    var params = {
        thingName: thingName, /* required */
        attributePayload: {
            attributes: {
                id: thingID
            },
            merge: true
        },
        thingTypeName: 'streetlamp'
    };
    iot.createThing(params, function (err, data) {
        if (err) console.log(err, err.stack);               // an error occurred
        else {
            console.log('\ndevice thing created\n');        // successful response
            createCertificate();
        }
    });
}

/**
 * Generate Id for device thing.
 * If id does'n exist yet it is used to attach certificate.
 */
var found = false;
function generate() {
    var id = randomInt(0, 1000);
    var params = {
        attributeName: 'id',
        attributeValue: id.toString(),
        maxResults: 1
    };

    list(params);
}

/**
 * List all device with attribute specified to test if already exists or not.
 *
 * @param params id to search by
 */
function list(params) {

    iot.listThings(params, function (err, data) {
        if (err) console.log(err, err.stack); // an error occurred
        if (data.things.length == 0 && found == false) {
            found = true;
            thingID = params.attributeValue;
            createThing(params.attributeValue);
        } else if (data.things.length != 0 && found == false) {
            generate();
        }
    });
}

/**
 * Create a certificate for the Thing created.
 */
function createCertificate() {
// create certificate
    params = {
        setAsActive: true
    };
    iot.createKeysAndCertificate(params, function (err, data) {
        if (err) console.log(err, err.stack); // an error occurred
        else  handleData(data);           // successful response

    });
}

/**
 * Called when creation of certificate and keys returns
 */
var credential;
function handleData(KeyandCert) {

    // attach policy to certificate
    var params = {
        policyName: 'my-device-policy', /* required */
        principal: KeyandCert.certificateArn  /* required */
    };
    iot.attachPrincipalPolicy(params, function (err, data) {
        if (err) console.log(err, err.stack); // an error occurred
        else     console.log('\ncertificate created and attached to policy and thing\n');           // successful response

        // attach certificate to thing
        params = {
            principal: KeyandCert.certificateArn, /* required */
            thingName: thingName /* required */
        };
        iot.attachThingPrincipal(params, function (err, data) {
            if (err) console.log(err, err.stack); // an error occurred

            credential = KeyandCert;
            iot_connection(process.argv.slice(2));

        });
    });

}

/**
 * Device subscribe to topic "control" and publish on topic "data".
 * Communication takes place via MQTTS protocol.
 */
function device_work(args) {

    // put private key and CA certificate in a buffer
    const prvtKey = Buffer.from(credential.keyPair.PrivateKey);
    const cltCert = Buffer.from(credential.certificatePem);

    //
    // The device module exports an MQTT instance, which will attempt
    // to connect to the AWS IoT endpoint configured in the arguments.
    // Once connected, it will emit events which our application can
    // handle.
    //
    const device = deviceModule.thingShadow({
        privateKey: prvtKey,
        clientCert: cltCert,
        region: args.region,
        caPath: './certs/root-CA.crt',
        clientId: args.clientId,
        baseReconnectTimeMs: args.baseReconnectTimeMs,
        protocol: args.Protocol,
        port: args.Port
    });


    // subscribe topic intensity to receive value to set
    //device.subscribe('control');

    // callback every delay seconds
    timeout = setInterval(function () {
        device.publish('data', JSON.stringify(
            generateState()
        ));
    }, delay);

    // generate json data
    function generateState() {
        var consumption = Math.round(Math.random(0, 1) * 100 / 100);
        return {
            "streetLamp" : {
                "ID": thingID,
                "on": state,
                "lampModel": model,
                "address": {
                    "name": street,
                    "number": street_number,
                    "numberType": number_type
                },
                "cellID": cellID,
                "lightIntensity": intensity,
                "consumption": consumption,

                "lifetime": {
                    "date": {
                        "year": lifetimeDate.year(),
                        "month": lifetimeDate.month(),
                        "day": lifetimeDate.day()},
                    "time": {
                        "hour": lifetimeDate.hour(),
                        "minute": lifetimeDate.minute(),
                        "second": lifetimeDate.second(),
                        "nano": 0
                    }
                }
            },
            "timestamp": new Date().getTime(),
            "naturalLightLevel": (Math.round(Math.random() * 1000)) / 10
        }
    }


    device.on('connect', function () {
        console.log('connect');
        device.register(thingName, function () {

            // Once registration is complete, update the Thing Shadow named
            // 'intensity' with the latest device state and save the clientToken
            // so that we can correlate it with status or timeout events.
            //
            // Thing shadow state
            var lampState = {"state": {"desired": {"intensity": intensity}}};

            clientTokenUpdate = device.update(thingName, lampState);

            //
            // The update method returns a clientToken; if non-null, this value will
            // be sent in a 'status' event when the operation completes, allowing you
            // to know whether or not the update was successful.  If the update method
            // returns null, it's because another operation is currently in progress and
            // you'll need to wait until it completes (or times out) before updating the
            // shadow.
            //
            if (clientTokenUpdate === null) {
                console.log('update shadow failed, operation still in progress');
            }
        });
    });


    device.on('status', function (thingName, stat, clientToken, stateObject) {
        console.log('received ' + stat + ' on ' + thingName + ': ' +
            JSON.stringify(stateObject));
        //
        // These events report the status of update(), get(), and delete()
        // calls.  The clientToken value associated with the event will have
        // the same value which was returned in an earlier call to get(),
        // update(), or delete().  Use status events to keep track of the
        // status of shadow operations.
        //
    });


    device.on('delta', function (thingName, stateObject) {

        var jsonState = JSON.stringify(stateObject);
        console.log('received delta on ' + thingName + ': ' + jsonState);
        intensity = stateObject.state.intensity;
    });

    device.on('timeout', function (thingName, clientToken) {
        console.log('received timeout on ' + thingName +
            ' with token: ' + clientToken);
        //
        // In the event that a shadow operation times out, you'll receive
        // one of these events.  The clientToken value associated with the
        // event will have the same value which was returned in an earlier
        // call to get(), update(), or delete().
        //
    });

    device.on('close', function () {
        console.log('close');
        device.unregister(thingName);

    });

    device.on('reconnect', function () {
        console.log('reconnect');
    });

    device.on('offline', function () {
        console.log('offline');
    });

    device.on('error', function (error) {
        console.log('error', error);
    });
    device.on('message', function (topic, payload) {
        console.log('message', topic, payload.toString());
        var json = JSON.parse(payload.toString());
        if (json.id == thingID)
            intensity = json.intensity;
    });
}

/**
 * Connect to IoT Service.
 */
function iot_connection() {
    if (require.main === module) {
        cmdLineProcess('connect to the AWS IoT service and publish/subscribe to topics using MQTT',
            process.argv.slice(2), device_work);
    }
}

/**
 *  Generate random int between two edges.
 *
 *  @param low minimum value
 *  @param high maximum value
 */
function randomInt(low, high) {
    return Math.floor(Math.random() * (high - low) + low);
}

// Send data
generate();