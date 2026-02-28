import socketio
import time
import random
import platform
import uuid

# Configuration
SERVER_URL = "http://localhost:4000"
DEVICE_ID = str(uuid.uuid4())[:8]
DEVICE_NAME = f"Android_{platform.system()}_{DEVICE_ID}"

sio = socketio.Client()

@sio.event
def connect():
    print("⚡ Connected to BEKEKKE-C2 Server")
    sio.emit('register', {
        'deviceId': DEVICE_ID,
        'deviceName': DEVICE_NAME,
        'type': 'android_sim'
    })

@sio.event
def disconnect():
    print("❌ Disconnected from server")

@sio.on('execute')
def on_execute(data):
    print(f"🔥 RECEIVED COMMAND: {data['cmd']}")
    # Simulasikan eksekusi
    time.sleep(1)
    sio.emit('data_update', {
        'deviceId': DEVICE_ID,
        'status': 'Command Executed',
        'result': f"Successfully ran: {data['cmd']}"
    })

def send_telemetry():
    while True:
        if sio.connected:
            telemetry = {
                'deviceId': DEVICE_ID,
                'battery': random.randint(10, 100),
                'ram_usage': random.randint(30, 90),
                'cpu_temp': random.randint(35, 60),
                'location': {'lat': -6.2088, 'lng': 106.8456} # Jakarta
            }
            sio.emit('data_update', telemetry)
        time.sleep(5)

if __name__ == '__main__':
    try:
        sio.connect(SERVER_URL)
        send_telemetry()
    except Exception as e:
        print(f"Error: {e}")
