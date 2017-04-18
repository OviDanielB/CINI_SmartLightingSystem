'use strict';

var AWS = require('aws-sdk');
var https = require('https');
var amqp = require('amqplib/callback_api');

// Receive tuple from IoT
exports.handler = function (event, context, callback) {

    var tuple = JSON.stringify(event, null, 2);

    console.log('Received event: ', tuple);

    // Produce on RabbitMQ
    sendToRabbitMQ(tuple);

    callback(null, e);  // Echo back the first key value
    //callback('Something went wrong');
}


function sendToRabbitMQ(tuple) {
    var SERVER = "34.251.178.42";
    var PORT = '5672';
    var NAME_QUEUE = 'storm';

    console.log("Contact amqp://guest:guest@" + SERVER + ':' + PORT + '/');

    amqp.connect('amqp://guest:guest@' + SERVER + ':' + PORT + '/', function (err, conn) {
        if (err) {
            console.log('Error in connect', err);
        } else {
            conn.createChannel(function (err, ch) {
                if (err) {
                    console.log('Error in createChannel', err);
                } else {
                    ch.assertQueue(NAME_QUEUE, {durable: false});
                    // Note: on Node 6 Buffer.from(msg) should be used
                    ch.sendToQueue(NAME_QUEUE, Buffer.from(tuple));
                    console.log(" [x] Sent %s", tuple);
                }
            });
            setTimeout(function () {
                conn.close();
                process.exit(0)
            }, 500);
        }
    });
}