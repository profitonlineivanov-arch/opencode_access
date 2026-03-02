const WebSocket = require('ws');
const http = require('http');

const PC_WS_URL = 'ws://45.146.164.144:8097';
const OPENCODE_PORT = 4096;
const OPENCODE_HOST = 'localhost';

function connect() {
    console.log('Connecting to proxy server (WebSocket port 8097)...');
    const ws = new WebSocket(PC_WS_URL);

    ws.on('open', () => {
        console.log('Connected to server!');
    });

    ws.on('message', (data) => {
        try {
            const req = JSON.parse(data.toString());
            
            const options = {
                hostname: OPENCODE_HOST,
                port: OPENCODE_PORT,
                path: req.path,
                method: req.method,
                headers: req.headers
            };
            
            const proxyReq = http.request(options, (proxyRes) => {
                let body = '';
                proxyRes.on('data', (chunk) => { body += chunk; });
                proxyRes.on('end', () => {
                    const response = {
                        id: req.id,
                        status: proxyRes.statusCode,
                        headers: proxyRes.headers,
                        body: body
                    };
                    ws.send(JSON.stringify(response));
                });
            });
            
            proxyReq.on('error', (err) => {
                const response = {
                    id: req.id,
                    status: 502,
                    headers: {},
                    body: err.message
                };
                ws.send(JSON.stringify(response));
            });
            
            proxyReq.end();
        } catch (e) {
            console.log('Error:', e.message);
        }
    });

    ws.on('close', () => {
        console.log('Disconnected, reconnecting in 5s...');
        setTimeout(connect, 5000);
    });

    ws.on('error', (err) => {
        console.log('Error:', err.message);
    });
}

connect();
