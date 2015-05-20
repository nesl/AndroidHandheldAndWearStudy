var WebSocketServer = require("ws").Server;
var http = require("http");
var bodyParser = require('body-parser');
var fs = require('fs');
var async = require('async');
var moment = require("./moment.min.js");

var dataDir = __dirname + '/../../../../data/activityClassification/keystrokeStudy/typingData/';

var express = require("express");
var phonePort = 3334;
var wsPort = 3333;

var tokenUpdatedTime = 0;
var recentToken;

var tokenFinished = 0;  // the token which associated task is finished


var app = express();
app.use( bodyParser.urlencoded({ extended:false, limit:'50mb' }) );
//app.use(express.static(__dirname+ "/../"));
app.post('/register', function(req, res, next) {
	//console.log('receiving get request', req, res);
	console.log('receiving register request from phone, hanging there');
	
	// ref: http://stackoverflow.com/questions/5226285/settimeout-in-for-loop-does-not-print-consecutive-values
	function setMyTimeout(res, oldTokenTime) {
		setTimeout(function() {
			if (oldTokenTime == tokenUpdatedTime)
				setMyTimeout(res, oldTokenTime);
			else {
				res.send('#' + recentToken);
				console.log('response phone register request (' + (new Date()).getTime() + ')');
			}
		}, 1);
	}
	
	setMyTimeout(res, tokenUpdatedTime);  // treat current tokenUpdatedTime as old value and wait for a new one
});
app.post('/wait', function(req, res, next) {
	console.log('receiving wait request from phone, hanging there');
	//console.log('receiving get request', req);
	console.log('req', req.body);
	
	function setMyTimeout(res, targetToken) {
		setTimeout(function() {
			if (targetToken != tokenFinished)
				setMyTimeout(res, targetToken);
			else {
				res.send('#');
				console.log('response phone wait request (' + (new Date()).getTime() + ')');
			}
		}, 1);
	}
	
	setMyTimeout(res, req.body.token);
});
app.post('/data', function(req, res, next) {
	console.log('get sending data request (' + (new Date()).getTime() + ')');
	//console.log('receiving get request', req);
	console.log('req attr', Object.keys(req.body));
	var token = req.body.token;
	var data = JSON.parse(req.body.data);
	console.log('data.time', data.time);
	taskFolder = dataDir + token + '/';
	async.series([
			function(cb) {
				fs.mkdir(taskFolder, 0744, function(err) {
					if (err && err.code == 'EEXIST')
						cb(); // ignore the error if the folder already exists
					else
						cb(); // successfully created folder
				});
			}, function(cb) {
				path = taskFolder + 'watchAcc.txt';
				fs.writeFile(path, data.acc, function(err) {
					if (err) 
						console.log('unable to write ' + path, err);
					else
						cb();
				});
			}, function(cb) {
				path = taskFolder + 'watchGyro.txt';
				fs.writeFile(path, data.gyro, function(err) {
					if (err) 
						console.log('unable to write ' + path, err);
					else
						cb();
				});
			}, function(cb) {
				path = taskFolder + 'watchMag.txt';
				fs.writeFile(path, data.mag, function(err) {
					if (err) 
						console.log('unable to write ' + path, err);
					else
						cb();
				});
			}, function(cb) {
				path = taskFolder + 'watchGrav.txt';
				fs.writeFile(path, data.grav, function(err) {
					if (err) 
						console.log('unable to write ' + path, err);
					else
						cb();
				});
			}, function(cb) {
				path = taskFolder + 'timeFromWatch.txt';
				fs.writeFile(path, data.time, function(err) {
					if (err) 
						console.log('unable to write ' + path, err);
					else
						cb();
				});
			}, function(cb) {
				res.send('#');
			}
	]);
});
app.listen(phonePort);




var server = http.createServer(app);
server.listen(wsPort);
//var userId;
var wss = new WebSocketServer({server: server});
wss.on("connection", function (ws) {
	url = ws.upgradeReq.url;
	timeNow = (new Date()).getTime();
	commandLastTime = timeNow;

	url = ws.upgradeReq.url;
	console.log("websocket connection open, url:" + url + "  (at time " + timeNow + ")");
	
	strs = url.substring(1).split('/');
	cmd = strs[0];
	params = strs.slice(1);

	if (cmd == 'register') {  //register/<username>
		var token = moment().format('YYYYMMDD_HHmmss') + "_" + params[0];
		console.log('token', token);
		
		tokenUpdatedTime = timeNow;
		recentToken = token;

		ws.send(token);
		ws.close();
	}
	else if (cmd == 'finish') {  //finish/<token>
		tokenFinished = params[0];
		ws.close();
	}
	else if (cmd == 'data') {
		var token = params[0];

		ws.onmessage = function(evt) {
			rcvObj = JSON.parse(evt.data);
			//console.log('data', rcvObj);

			taskFolder = dataDir + token + '/';
			async.series([
					function(cb) {
						fs.mkdir(taskFolder, 0744, function(err) {
							if (err && err.code == 'EEXIST')
								cb(); // ignore the error if the folder already exists
							else
								cb(); // successfully created folder
						});
					}, function(cb) {
						path = taskFolder + 'typingEvent.txt';
						fs.writeFile(path, rcvObj.typing, function(err) {
							if (err) 
								console.log('unable to write ' + path, err);
							else
								cb();
						});
					}, function(cb) {
						path = taskFolder + 'timeFromDesktop.txt';
						fs.writeFile(path, rcvObj.timeAlign, function(err) {
							if (err) 
								console.log('unable to write ' + path, err);
							else
								cb();
						});
					}, function(cb) {
						path = taskFolder + 'gndText.txt';
						fs.writeFile(path, rcvObj.texts, function(err) {
							if (err) 
								console.log('unable to write ' + path, err);
							else
								cb();
						});
					}
			]);
		}
	}
});




console.log("tcp app listening on %d ", phonePort);
console.log("web socket listening on %d", wsPort);
