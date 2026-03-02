const http = require('http');

const PORT = 8096;
const TUNNEL_PORT = 8096;

const server = http.createServer((req, res) => {
    const proxyReq = http.request({
        hostname: '127.0.0.1',
        port: TUNNEL_PORT,
        path: req.url,
        method: req.method,
        headers: req.headers
    }, (proxyRes) => {
        res.writeHead(proxyRes.statusCode, proxyRes.headers);
        proxyRes.pipe(res);
    });

    proxyReq.on('error', (err) => {
        console.error('Tunnel error:', err.message);
        res.writeHead(502);
        res.end('PC not connected. Run p4oc-connect.bat on your PC!');
    });

    req.pipe(proxyReq);
});

server.listen(PORT, '0.0.0.0', () => {
    console.log('P4OC Proxy running on port ' + PORT);
    console.log('Waiting for SSH tunnel from PC...');
});
