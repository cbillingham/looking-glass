express = require 'express'
http = require 'http'
io = require 'socket.io'
mayaCommand = require './maya-control'
THREE = require('three-js')()

MAYA_PORT = "7505"

CAMERA = 'camera1'

camera = 
  rotation: null
  translation: null

app = express()
server = http.Server(app)
io = io(server, { serveClient: false })

clients = {}

maya = new mayaCommand(MAYA_PORT)

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
    quat = new THREE.Quaternion(pose.rotation[0],pose.rotation[1],pose.rotation[2],pose.rotation[3]).normalize()
    euler = new THREE.Euler().setFromQuaternion(quat)
    pose.rotation = euler.toArray().splice(0,3)
    maya.updatePose(CAMERA, pose)

  socket.on 'error', (error) ->
    console.log error

module.exports = server.listen 7500, () ->
  console.log "looking-glass listening on *:7500"