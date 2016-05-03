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

  updatePose: (camera, pose) ->
    @maya.send  "move -a -ws -wd #{pose.translation[0]*5} #{pose.translation[2]*5} #{pose.translation[1]*-5} #{camera};
                 rotate -a -os -fo #{pose.rotation[0]-1.5}rad #{pose.rotation[1]}rad #{pose.rotation[2]}rad #{camera};"

  disconnect: () ->
    @maya.disconnect()

module.exports = mayaCommand

