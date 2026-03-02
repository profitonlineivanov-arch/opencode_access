const WebSocket = require('ws');
const http = require('http');
const net = require('net');

const SERVER_URL = 'ws://45.146.164.144:8096';
const OPENCODE_PORT = 4096;
const OPENCODE_HOST = 'localhost';

console.log('P4OC Client starting...');
console.log('Connecting to: ' + SERVER_URL);

const ws = new WebSocket(SERVER_URL);

ws.on('open', () => {
    console.log('Connected to server!');
});

ws.on('message', (data) => {
    // Parse as HTTP request
    const requestStr = data.toString();
    const [requestLine, ...headerLines] = requestStr.split('\r\n');
    const [method, path, proto] = requestLine.split(' ');
    
    if (!method) return;
    
    // Forward to OpenCode
    const options = {
        hostname: OPENCODE_HOST,
        port: OPENCODE_PORT,
        path: path,
        method: method,
        headers: {}
    };
    
    // Parse headers
    headerLines.forEach(line => {
        const [key, value] = line.split(': ');
        if (key && value) {
            options.headers[key] = value;
        }
    });
    
    const req = http.request(options, (res) => {
        let responseData = '';
        res.on('data', (chunk) => {
            responseData += chunk;
        });
        res.on('end', () => {
            // Send response back through WebSocket
            const response = `HTTP/${res.statusCode} ${res.statusMessage}\r\n${Object.entries(res.headers).map(([k,v]) => k + ': ' + v).join('\r\n')}\r\n\r\n${responseData}`;
            ws.send(response);
        });
    });
    
    req.on('error', (err) => {
        const errorResponse = 'HTTP/502 Bad Gateway\r\n\r\n' + err.message;
        ws.send(errorResponse);
    });
    
    // Get body if POST
    const bodyMatch = requestStr.match(/\r\n\r\n([\s\S]*)/);
    if (bodyMatch && bodyMatch[1]) {
        req.write(bodyMatch[1]);
    }
    req.end();
});

ws.on('close', () => {
    console.log('Disconnected, reconnecting in 5s...');
    setTimeout(() => {
        location.reload();
    }, 5000);
});

ws.on('error', (err) => {
    console.log('Error: ' + err.message);
});

console.log('Waiting for connections from phone...');
