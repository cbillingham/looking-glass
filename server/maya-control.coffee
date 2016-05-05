net = require 'net'

class mayaPort

  constructor: (@port) ->
    @socket = new net.Socket()
    @socket.setEncoding('utf8')

  connect: ->
    @socket.connect @port
    @socket.on 'data', (data) ->
      undefined
    console.log ("connected to maya")
    return

  send: (message) ->
    @socket.write(message)

  disconnect: () ->
    @socket.close()

class mayaCommand

  constructor: (port) ->
    @maya = new mayaPort(port)
    @maya.connect()

  setCamera: (camera) ->
    @maya.send "python(\"tangoCamera(#{camera})\")"

  updatePose: (camera, pose) ->
    @maya.send "python(\"tangoUpdateRotate(#{pose.rotation[0]}, #{pose.rotation[1]}, #{pose.rotation[2]}, #{pose.rotation[3]})\")"

  disconnect: () ->
    @maya.disconnect()

module.exports = mayaCommand

