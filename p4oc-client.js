const http = require('http');

const SERVER_HOST = '45.146.164.144';
const SERVER_PORT = 8096;
const OPENCODE_PORT = 4096;
const OPENCODE_HOST = 'localhost';

function connect() {
    console.log('Connecting to proxy server...');
    
    const req = http.request({
        hostname: SERVER_HOST,
        port: SERVER_PORT,
        path: '/connect',
        method: 'GET'
    }, (res) => {
        console.log('Connected to server! Waiting for requests...');
        let body = '';
        res.on('data', (chunk) => { body += chunk; });
        res.on('end', () => {
            console.log('Server response:', body);
            setTimeout(connect, 5000);
        });
    });
    
    req.on('error', (err) => {
        console.log('Error:', err.message);
        setTimeout(connect, 5000);
    });
    
    req.end();
}

connect();

// Simple HTTP proxy
const server = http.createServer((req, res) => {
    const options = {
        hostname: OPENCODE_HOST,
        port: OPENCODE_PORT,
        path: req.url,
        method: req.method,
        headers: req.headers
    };
    
    const proxyReq = http.request(options, (proxyRes) => {
        res.writeHead(proxyRes.statusCode, proxyRes.headers);
        proxyRes.pipe(res);
    });
    
    req.pipe(proxyReq);
    
    proxyReq.on('error', (err) => {
        res.writeHead(502);
        res.end(err.message);
    });
});

server.listen(4096, '127.0.0.1', () => {
    console.log('Local proxy listening on localhost:4096');
});

console.log('P4OC Client ready. Waiting for connections from phone through server...');
