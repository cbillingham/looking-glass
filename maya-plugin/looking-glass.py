from flask import Flask
from flask_socketio import SocketIO, emit

import eventlet
eventlet.monkey_patch()

app = Flask(__name__)
socketio = SocketIO(app, async='eventlet')


@socketio.on('connect')
def test_connect():
  print("there was a connection")
  emit('response', {'data': 'Connected'})

@socketio.on('disconnect')
def test_disconnect():
  print("disconnected!")

if __name__ == '__main__':
  socketio.run(app, debug=False, host='0.0.0.0', port=3000)