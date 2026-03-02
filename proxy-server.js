const WebSocket = require('ws');
const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 8096;
const PC_PORT = 4096;
const PC_HOST = 'localhost';

// HTTP server to check status
const server = http.createServer((req, res) => {
    res.writeHead(200, { 'Content-Type': 'text/plain' });
    res.end('P4OC Proxy Server Running\n');
});

const wss = new WebSocket.Server({ server });

// Store connected clients
let pcSocket = null;
let phoneSocket = null;

console.log('P4OC Proxy Server starting on port ' + PORT);

wss.on('connection', (ws, req) => {
    const clientIP = req.socket.remoteAddress;
    console.log('New connection from: ' + clientIP);

    ws.on('message', (message) => {
        console.log('Received: ' + message.toString().substring(0, 100));
        
        // Forward message to the other side
        if (ws === pcSocket && phoneSocket && phoneSocket.readyState === WebSocket.OPEN) {
            phoneSocket.send(message);
        } else if (ws === phoneSocket && pcSocket && pcSocket.readyState === WebSocket.OPEN) {
            pcSocket.send(message);
        }
    });

    ws.on('close', () => {
        console.log('Client disconnected');
        if (ws === pcSocket) {
            pcSocket = null;
            console.log('PC disconnected');
        }
        if (ws === phoneSocket) {
            phoneSocket = null;
            console.log('Phone disconnected');
        }
    });

    ws.on('error', (err) => {
        console.log('Error: ' + err.message);
    });
});

// Connect to PC as client
function connectToPC() {
    console.log('Connecting to PC at localhost:' + PC_PORT);
    
    const ws = new WebSocket('http://localhost:4096');
    
    ws.on('open', () => {
        console.log('Connected to PC!');
    });
    
    ws.on('message', (data) => {
        // Forward to phone
    });
    
    ws.on('close', () => {
        console.log('PC connection closed, reconnecting in 5s...');
        setTimeout(connectToPC, 5000);
    });
    
    ws.on('error', (err) => {
        console.log('PC connection error: ' + err.message);
    });
    
    return ws;
}

// Actually, the proxy works differently:
// 1. PC connects to server as a client
// 2. Phone connects to server as a client  
// 3. Server forwards messages between them

// Let's handle both as "clients" and bridge them
let clients = [];

wss.on('connection', (ws, req) => {
    const clientIP = req.socket.remoteAddress;
    console.log('Client connected: ' + clientIP);
    clients.push(ws);
    
    // First client = PC, Second = Phone
    // Or we can identify by message
    
    ws.on('message', (message) => {
        // Broadcast to all OTHER clients
        clients.forEach(client => {
            if (client !== ws && client.readyState === WebSocket.OPEN) {
                client.send(message);
            }
        });
    });
    
    ws.on('close', () => {
        const index = clients.indexOf(ws);
        if (index > -1) clients.splice(index, 1);
        console.log('Client disconnected. Total: ' + clients.length);
    });
});

server.listen(PORT, () => {
    console.log('P4OC Proxy Server listening on port ' + PORT);
    console.log('Waiting for connections...');
});
