var WebSocketServer = require("ws").Server;
var http = require("http");
var express = require("express");
var port = 3333;
var nodeIdToLatLng = {};

var app = express();
//app.use(express.static(__dirname+ "/../"));
app.use('/abccccc', function(req, res, next) {
	console.log('come to here');
});
app.get('/', function(req, res, next) {
	console.log('receiving get request', req, res);
});
app.post('/somePostRequest', function(req, res, next) {
	console.log('receiving post request', req, res);
});
//app.listen(port); //port 80 need to run as root

console.log("app listening on %d ", port);

var server = http.createServer(app);
server.listen(port);

console.log("http server listening on %d", port);

var userId;
var wss = new WebSocketServer({server: server});
wss.on("connection", function (ws) {
	url = ws.upgradeReq.url;
	console.log("websocket connection open, url:" + url);
});
