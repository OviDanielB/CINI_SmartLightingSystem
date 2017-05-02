'use strict';

var AWS = require('aws-sdk');
var https = require('https');
var amqp = require('amqplib/callback_api');

// Receive tuple from IoT
exports.handler = function(event, context, callback) {
	
	var tuple = JSON.stringify(event, null, 2);

    console.log('Received event: ', tuple);

	// Produce on RabbitMQ
    sendToRabbitMQ(tuple);
    // allows for using callbacks as finish/error-handlers
  context.callbackWaitsForEmptyEventLoop = false;

}


function sendToRabbitMQ(tuple) {
	var ENDPOINT = 'helios-elb-297511228.eu-west-1.elb.amazonaws.com:5672';
	var NAME_QUEUE = 'storm';


	var result;
	amqp.connect('amqp://'+ENDPOINT, function(err, conn) {
			if (err) {
				console.log('Error in connecting to amqp://'+ENDPOINT , err);
			} else {
				  conn.createChannel(function(err, ch) {
				    if (err) {
				    	console.log('Error in createChannel', err);
				    } else {
					    ch.assertQueue(NAME_QUEUE, {durable: false});
					    // Note: on Node 6 Buffer.from(msg) should be used
					    ch.sendToQueue(NAME_QUEUE, Buffer.from(tuple));
					    console.log(" [x] Sent %s", tuple);
					}
				  });
				  setTimeout(function() { conn.close(); process.exit(0) }, 500);
			}
	}
	);
}