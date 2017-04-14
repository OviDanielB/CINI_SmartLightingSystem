//app deps
const deviceModule = require('aws-iot-device-sdk');
const cmdLineProcess = require('./lib/cmdline');
const momentRandom = require('moment-random');
var AWS = require('aws-sdk');
var async = require("async");

module.exports = cmdLineProcess;

var iot = new AWS.Iot({region: 'us-west-2', apiVersion: '2015-05-28'});

var lifetimeDate = momentRandom();  // streetLamp lifetime random generated
const delay = 10000;                // publish data every delay time
var thingName;
var thingID;

generate();

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
        if (err) console.log(err, err.stack); // an error occurred
        else {
            console.log(data);           // successful response
            createCertificate();
        }
    });
}

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

// called when creation of certificate and keys returns
var credential;

function handleData(KeyandCert) {

    // attach policy to certificate
    var params = {
        policyName: 'my-device-Policy', /* required */
        principal: KeyandCert.certificateArn  /* required */
    };
    iot.attachPrincipalPolicy(params, function (err, data) {
        if (err) console.log(err, err.stack); // an error occurred
        else     console.log(data);           // successful response

        // attach certificate to thing
        params = {
            principal: KeyandCert.certificateArn, /* required */
            thingName: thingName /* required */
        };
        iot.attachThingPrincipal(params, function (err, data) {
            if (err) console.log(err, err.stack); // an error occurred
            else     console.log(data);           // successful response

            credential = KeyandCert;
            iot_connection(process.argv.slice(2));

        });
    });

}

function randomInt(low, high) {
    return Math.floor(Math.random() * (high - low) + low);
}

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
    const device = deviceModule.device({
        privateKey: prvtKey,
        clientCert: cltCert,
        region: 'us-west-2',
        caPath: './certs/root-CA.crt',
        clientId: args.clientId,
        baseReconnectTimeMs: args.baseReconnectTimeMs,
        keepalive: args.keepAlive,
        protocol: args.Protocol,
        port: args.Port,
        host: args.Host,
        debug: args.Debug
    });

    // subscrive topic intensity to receive value to set
    device.subscribe('intensity');

    // callback every delay seconds
    timeout = setInterval(function () {

        device.publish('data', JSON.stringify(
            generateState()
        ));

    }, delay);


    // generate json data
    function generateState() {
        var consumption = Math.round(Math.random(0, 1) * 100) / 100;
        return {
            "ID": thingID,
            "state": 1,
            "lampModel": "LED",
            "address": {
                "name": "Via Cambridge",
                "number": 23,
                "numberType": "CIVIC"
            },
            "lightIntensity": 0.7,
            "consumption": consumption,

            "lifetime": {
                "date": {"year": lifetimeDate.year(), "month": lifetimeDate.month(), "day": lifetimeDate.day()},
                "time": {
                    "hour": lifetimeDate.hour(),
                    "minute": lifetimeDate.minute(),
                    "second": lifetimeDate.second(),
                    "nano": 0
                }
            },
            "timestamp": new Date().getTime(),
            "naturalLightLevel": 0.7
        }
    }


    device
        .on('connect', function () {
            console.log('connect');
        });
    device
        .on('close', function () {
            console.log('close');
        });
    device
        .on('reconnect', function () {
            console.log('reconnect');
        });
    device
        .on('offline', function () {
            console.log('offline');
        });
    device
        .on('error', function (error) {
            console.log('error', error);
        });
    device
        .on('message', function (topic, payload) {
            console.log('message', topic, payload.toString());
            intensity = payload;
        });

}

function iot_connection() {
    if (require.main === module) {
        cmdLineProcess('connect to the AWS IoT service and publish/subscribe to topics using MQTT',
            process.argv.slice(2), device_work);
    }
}