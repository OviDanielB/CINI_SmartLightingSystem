#!/usr/bin/env node

var http = require('http');
var fs = require('fs');

var street;

var server = http.createServer( function(req, res) {

    console.dir(req.param);

    if (req.method == 'GET') {

        var n = 15000; // number of (cellId, percentage) data

        var cellID = randomInt(1000, 16000);

        var cell_list = '[';

        for (var i=0; i<n; i++) {
            cell_list += '{ "cellID": ' + cellID
                    + ', "occupationPercentage": ' + Math.round(Math.random()*1000)/10
                    + ', "timestamp": ' + new Date().getTime()
                + '}';
            if (i < n-1) {
                cell_list += ', ';
            }
        }
        cell_list += ']';

        console.log(cell_list);

        res.writeHead(200, {'Content-Type': 'application/json'});
        res.end(cell_list);
    }
    else
        console.log("Not a GET Request");
});

port = 3000;
host = '127.0.0.1';
server.listen(port, host);
console.log('Listening at http://' + host + ':' + port);

function randomInt(low, high) {
    return Math.floor(Math.random() * (high - low) + low);
}