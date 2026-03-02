const WebSocket = require('ws');
const http = require('http');

const SERVER_URL = 'ws://45.146.164.144:8096';
const OPENCODE_PORT = 4096;
const OPENCODE_HOST = 'localhost';

function connect() {
    console.log('Connecting to proxy server...');
    const ws = new WebSocket(SERVER_URL);

    ws.on('open', () => {
        console.log('Connected to server!');
    });

    ws.on('message', (data) => {
        const requestStr = data.toString();
        const lines = requestStr.split('\r\n');
        if (lines.length < 1) return;
        
        const requestLine = lines[0];
        if (!requestLine.includes(' ')) return;
        
        const [method, path, proto] = requestLine.split(' ');
        const headers = {};
        
        for (let i = 1; i < lines.length; i++) {
            const line = lines[i];
            if (!line) break;
            const idx = line.indexOf(': ');
            if (idx > 0) {
                headers[line.substring(0, idx)] = line.substring(idx + 2);
            }
        }
        
        const options = {
            hostname: OPENCODE_HOST,
            port: OPENCODE_PORT,
            path: path,
            method: method,
            headers: headers
        };
        
        const req = http.request(options, (res) => {
            let responseData = '';
            res.on('data', (chunk) => { responseData += chunk; });
            res.on('end', () => {
                let responseHeaders = 'HTTP/' + res.statusCode + ' ' + res.statusMessage + '\r\n';
                for (const [k, v] of Object.entries(res.headers)) {
                    responseHeaders += k + ': ' + v + '\r\n';
                }
                responseHeaders += '\r\n' + responseData;
                ws.send(responseHeaders);
            });
        });
        
        req.on('error', (err) => {
            ws.send('HTTP/502 Bad Gateway\r\n\r\n' + err.message);
        });
        
        const bodyMatch = requestStr.match(/\r\n\r\n([\s\S]*)$/);
        if (bodyMatch && bodyMatch[1] && method === 'POST') {
            req.write(bodyMatch[1]);
        }
        req.end();
    });

    ws.on('close', () => {
        console.log('Disconnected, reconnecting in 5s...');
        setTimeout(connect, 5000);
    });

    ws.on('error', (err) => {
        console.log('Error: ' + err.message);
    });
}

connect();
