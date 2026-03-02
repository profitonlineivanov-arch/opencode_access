const http = require('http');
const WebSocket = require('ws');

const PORT = 8096;
const PC_WS_PORT = 8097;

let pcWs = null;
let requestId = 0;
const pendingRequests = {};

const wss = new WebSocket.Server({ port: PC_WS_PORT });

wss.on('connection', (ws) => {
    console.log('PC connected');
    pcWs = ws;
    
    ws.on('message', (data) => {
        try {
            const response = JSON.parse(data.toString());
            const reqId = response.id;
            if (pendingRequests[reqId]) {
                const { res } = pendingRequests[reqId];
                delete pendingRequests[reqId];
                
                res.writeHead(response.status, response.headers);
                res.end(response.body);
            }
        } catch (e) {
            console.log('Error parsing PC response:', e.message);
        }
    });
    
    ws.on('close', () => {
        console.log('PC disconnected');
        pcWs = null;
        Object.values(pendingRequests).forEach(({ res }) => {
            res.writeHead(502);
            res.end('PC disconnected');
        });
    });
});

const server = http.createServer((req, res) => {
    if (!pcWs || pcWs.readyState !== WebSocket.OPEN) {
        res.writeHead(502);
        res.end('PC not connected. Run p4oc-connect.bat on your PC!');
        return;
    }
    
    const reqId = ++requestId;
    const headers = {};
    for (const [k, v] of Object.entries(req.headers)) {
        headers[k] = v;
    }
    
    pendingRequests[reqId] = { res };
    
    const msg = JSON.stringify({
        id: reqId,
        method: req.method,
        path: req.url,
        headers: headers
    });
    
    pcWs.send(msg);
    
    setTimeout(() => {
        if (pendingRequests[reqId]) {
            delete pendingRequests[reqId];
            res.writeHead(504);
            res.end('Gateway Timeout');
        }
    }, 30000);
});

server.listen(PORT, '0.0.0.0', () => {
    console.log('P4OC Server listening on port ' + PORT);
    console.log('PC WebSocket on port ' + PC_WS_PORT);
    console.log('Waiting for PC connection...');
});
