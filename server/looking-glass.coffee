express = require 'express'
http = require 'http'
io = require 'socket.io'
mayaCommand = require './maya-control'
THREE = require('three-js')()

MAYA_PORT = ["7505","7506"]

camera = ""

app = express()
server = http.Server(app)
io = io(server, { serveClient: false })

recording = true;

clients = {}

maya = new mayaCommand(MAYA_PORT[0])

server.stop = () ->
  for userName of clients
    socket = clients[userName]
    socket.disconnect()
  server.close()

io.on 'connection', (socket) ->
  userName = ''
  socket.on 'connection name', (user) ->
    userName = user.name
    clients[userName] = socket
    console.log "#{userName} is connected"

  socket.on 'requestCameraList', () ->
    cameraList = maya.requestCameraList()
    return maya.requestCameraList

  socket.on 'disconnect', () ->
    delete clients[userName]
    console.log "#{userName} disconnected"

  socket.on 'updateCameraPose', (pose) ->
    maya.updatePose(pose)

  socket.on 'choose camera', (data) ->
    console.log "test"
    maya.setCamera(data.name)

  socket.on 'make camera', (data) ->
    maya.makeCamera(data.name)

  socket.on 'camera list', () ->
    data = {cameras:["camera1","camera2"]}
    socket.emit("new camera list", data)

  socket.on 'record', () ->
    recording = true;
    record();

  socket.on 'playback', () ->
    maya.play()

  socket.on 'stop playback', () ->
    if recording
      recording = false;
    else
      maya.stop()

  socket.on 'store', () ->
    maya.store()

  socket.on 'error', (error) ->
    console.log error

record = () ->
  interval = setInterval( () ->
    if(!recording)
        clearInterval(interval)
        return

    maya.increment(camera)

  , 500)

module.exports = server.listen 7500, () ->
  console.log "looking-glass listening on *:7500"