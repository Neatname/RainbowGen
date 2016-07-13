var width;
var height;
var pauseInd = true;
var pixelCount = 0;
var buffered = false;

var c = null;
var context = null;

var gotWholeImage = false;

var websocket;
var uri;
if (window.location.hostname === "localhost"){
	uri = "ws://localhost:8080/imagesocket";
} else {
	uri = "wss://rainbowgen-nmiles.herokuapp.com/imagesocket";
}

function getImage() {
	c = document.getElementById("myCanvas");
	width = c.offsetWidth;
	height = c.offsetHeight;
	c.setAttribute("width", width);
	c.setAttribute("height", height);
	context = c.getContext("2d");
	connect();
}

function connect(){
	if ('WebSocket' in window) {
		websocket = new WebSocket(uri);
	} else if ('MozWebSocket' in window) {
		websocket = new MozWebSocket(uri);
	} else {
		alert('WebSocket is not supported by this browser.');
		return;
	}
	websocket.onopen = function(){
		//console.log('sending "' + "new " + width + " " + height + " " + 50 + '"');
		websocket.send("new " + width + " " + height + " " + 50);
	}

	websocket.onclose = function() {
		setTimeout( function(){
				if (!gotWholeImage){
				alert("Something went wrong, sorry :(");
			}
		}, 200);
	}

	websocket.onmessage = function(event) {
		var data = event.data;
		switch (data){
			case "generated":
				//console.log("Got generated message");
				break;
			case "done":
				//console.log("Got done message");
				gotWholeImage = true;
				break;
			default:
				//console.log("Got a chunk");
				addChunk(data);
		}
	}
}

var chunks = [];

function addChunk(data){
	chunks.push(data);
	buffered = true;
	if (!playing){
		playing = true;
		step(performance.now());
	}
}

var playing = false;
function play() {
	if (playing){
		return;
	}
	pauseInd = false;
	playing = true;
	step(performance.now());
}

var pixelsThisFrame;
var ppf = 1000;
var chunkCounter = 0;
var chunkIndex = 0;
var x;
var y;
var color;
function step(timestamp) {
	if (pauseInd || !buffered) {
		playing = false;
		return;
	}
	pixelsThisFrame = 0;
	outerLoop:
	for (; pixelsThisFrame <= ppf && chunkCounter < chunks.length; chunkCounter++){
		var end = chunks[chunkCounter].length;
		for (;chunkIndex < end; chunkIndex += 12, pixelsThisFrame++){
			x = parseInt(chunks[chunkCounter].substring(chunkIndex, chunkIndex + 3), 16);
			y = parseInt(chunks[chunkCounter].substring(chunkIndex + 3, chunkIndex + 6), 16);
			color = "#" + chunks[chunkCounter].substring(chunkIndex + 6, chunkIndex + 12);
			context.fillStyle = color;
			//console.log("Adding " + x + " " + y + " " + color);
			//console.log("adding pixel");
			context.fillRect(x, y, 1, 1);
			if (pixelsThisFrame >= ppf){
				break outerLoop;
			}
		}
		chunkIndex = 0;
	}
	
	window.requestAnimationFrame(step);
}

function pause() {
	pauseInd = true;
}

function reset() {
	pause();
	window.requestAnimationFrame(resetCont);
}

function resetCont() {
	c = document.getElementById("myCanvas");
	context = c.getContext("2d");
	context.fillStyle = "#FFFFFF";
	context.fillRect(0, 0, width, height);
	chunkCounter = 0;
	chunkIndex = 0;
}