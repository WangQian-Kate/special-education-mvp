const http = require('http');
const fs = require('fs');
const path = require('path');

const inputPath = path.join(__dirname, '..', 'mock', 'ai001-aligned-input.json');
const body = fs.readFileSync(inputPath, 'utf-8');

const options = {
  hostname: 'localhost',
  port: 3001,
  path: '/api/ai/report',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json; charset=utf-8',
    'Content-Length': Buffer.byteLength(body, 'utf-8')
  }
};

console.log('Sending request to AI server...');
const startTime = Date.now();

const req = http.request(options, (res) => {
  let data = '';
  res.on('data', (chunk) => { data += chunk; });
  res.on('end', () => {
    const elapsed = Date.now() - startTime;
    console.log('Status: ' + res.statusCode);
    console.log('Time: ' + elapsed + 'ms');
    console.log('Response:');
    console.log(data);
  });
});

req.on('error', (err) => {
  console.error('Error:', err.message);
});

req.write(body);
req.end();
