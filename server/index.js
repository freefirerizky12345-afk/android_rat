const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const cors = require('cors');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());

const server = http.createServer(app);
const io = new Server(server, {
    cors: {
        origin: "*",
        methods: ["GET", "POST"]
    }
});

let devices = new Map();

io.on('connection', (socket) => {
    console.log('New connection:', socket.id);

    socket.on('register', (data) => {
        const { deviceId, deviceName, type } = data;
        devices.set(socket.id, { deviceId, deviceName, type, lastSeen: new Date() });
        console.log(`Device registered: ${deviceName} (${deviceId})`);
        io.emit('device_list', Array.from(devices.values()));
    });

    socket.on('disconnect', () => {
        const device = devices.get(socket.id);
        if (device) {
            console.log(`Device disconnected: ${device.deviceName}`);
            devices.delete(socket.id);
            io.emit('device_list', Array.from(devices.values()));
        }
    });

    socket.on('command', (data) => {
        const { targetId, cmd } = data;
        console.log(`Sending command to ${targetId}: ${cmd}`);
        // Logika untuk mengirim perintah ke socket tertentu berdasarkan deviceId
        // Di sini kita simplifikasi kirim ke semua buat demo awal
        io.emit('execute', { cmd });
    });

    socket.on('data_update', (data) => {
        console.log('Data received from device:', data);
        io.emit('broadcast_update', data);
    });
});

const PORT = process.env.PORT || 4000;
server.listen(PORT, () => {
    console.log(`BEKEKKE-MDM Server running on port ${PORT} ⚡`);
});
