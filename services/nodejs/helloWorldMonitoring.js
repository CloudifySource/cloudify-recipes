var http = require('http');
var url = require("url");

var hits = 0;

http.createServer(function (req, res) {
  var pathname = url.parse(req.url).pathname;
  if (pathname.indexOf('/monitoring/') == 0) {
    var metric = pathname.substring('/monitoring/'.length)
    if (metric == 'hits') {
      res.writeHead(200, {'Content-Type': 'text/plain'});
      res.end("" + hits)
    }
  } else {
    hits++;
    res.writeHead(200, {'Content-Type': 'text/plain'});
    res.end('Hello World\n');
  }
}).listen(1337, '127.0.0.1');