const http = require('http');
const WebSocket = require('ws');

const PORT = 8096;
const WS_PORT = 8097;

const server = http.createServer((req, res) => {
    res.writeHead(200, { 'Content-Type': 'text/plain' });
    res.end('P4OC Server Running\n');
});

const wss = new WebSocket.Server({ server });

let pcWs = null;
let phoneWs = null;

wss.on('connection', (ws, req) => {
    const ip = req.socket.remoteAddress;
    console.log('Client connected: ' + ip);
    
    if (!pcWs) {
        pcWs = ws;
        console.log('PC connected');
    } else if (!phoneWs) {
        phoneWs = ws;
        console.log('Phone connected');
    }
    
    ws.on('message', (data) => {
        // Forward to the other client
        if (ws === pcWs && phoneWs && phoneWs.readyState === WebSocket.OPEN) {
            phoneWs.send(data);
        } else if (ws === phoneWs && pcWs && pcWs.readyState === WebSocket.OPEN) {
            pcWs.send(data);
        }
    });
    
    ws.on('close', () => {
        if (ws === pcWs) {
            pcWs = null;
            console.log('PC disconnected');
        } else if (ws === phoneWs) {
            phoneWs = null;
            console.log('Phone disconnected');
        }
    });
});

server.listen(PORT, '0.0.0.0', () => {
    console.log('P4OC Server listening on port ' + PORT);
});
