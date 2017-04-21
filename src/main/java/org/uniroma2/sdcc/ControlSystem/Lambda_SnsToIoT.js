var AWS = require('aws-sdk');

var iotdata = new AWS.IotData({endpoint: 'a1vmjjmlsn643i.iot.eu-west-1.amazonaws.com'
});

exports.handler = (event, context, callback) => {
    
    var data = JSON.stringify(event.Records[0].Sns.Message, null, 2);
    var name = 'my-device-'+ event.Records[0].Sns.Message.id;
    var intensity = event.Records[0].Sns.Message.intensity;
    
    var up = { "state": { "desired": { "intensity": intensity  } } };
    var upBuffer = Buffer.from(JSON.stringify(up));
    
    var params = {
        payload: upBuffer,
        thingName: name 
    };
    
    iotdata.updateThingShadow(params, function(err, data) {
         if (err) console.log(err, err.stack); // an error occurred
         else     console.log(data);           // successful response
    });

    
    callback(null, name);
};