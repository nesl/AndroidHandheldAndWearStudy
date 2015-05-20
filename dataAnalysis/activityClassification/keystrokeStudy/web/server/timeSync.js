var WebSocketServer = require("ws").Server;
var http = require("http");
var express = require("express");
var phonePort = 3334;
var wsPort = 3333;
var nodeIdToLatLng = {};

var commandLastTime = 0;

var app = express();
//app.use(express.static(__dirname+ "/../"));
app.use('/abccccc', function(req, res, next) {
	console.log('come to here');
});
app.get('/', function(req, res, next) {
	//console.log('receiving get request', req, res);
	console.log('receiving request from phone, hanging there');
	var tokenTime = commandLastTime;
	//while (tokenTime == commandLastTime);  // busy waiting

	// ref: http://stackoverflow.com/questions/5226285/settimeout-in-for-loop-does-not-print-consecutive-values
	function setMyTimeout(res, tokenTime) {
		setTimeout(function() {
			if (tokenTime == commandLastTime)
				setMyTimeout(res, tokenTime);
			else {
				res.send('#');
				console.log('got here (at time ' + (new Date).getTime() + ')');
			}
		}, 1);
	}
	setMyTimeout(res, tokenTime);
	
});
app.post('/somePostRequest', function(req, res, next) {
	console.log('receiving post request', req, res);
});
app.listen(phonePort); //port 80 need to run as root

console.log("tcp app listening on %d ", phonePort);

var server = http.createServer(app);
server.listen(wsPort);

console.log("web socket listening on %d", wsPort);

var userId;
var wss = new WebSocketServer({server: server});
wss.on("connection", function (ws) {
	url = ws.upgradeReq.url;
	timeNow = (new Date()).getTime();
	commandLastTime = timeNow;
	console.log("websocket connection open, url:" + url + "  (at time " + timeNow + ")");
});
