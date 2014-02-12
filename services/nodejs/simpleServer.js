// Load the http module to create an http server.
var http = require('http');

// Configure our HTTP server to respond with Hello World to all requests.
var server = http.createServer(function (request, response) {
  response.writeHead(200, {"Content-Type": "text/plain"});
  response.end("Hello World\n");
});

process.argv.forEach(function(val, index, array) {
  console.log(index + ': ' + val);
});


var port = process.argv[2];
// Listen on port 8000, IP defaults to 127.0.0.1

// Put a friendly message on the terminal
console.log("Starting Server at http://127.0.0.1:"+ port);
server.listen(port);

