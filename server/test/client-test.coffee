require 'should'
io = require('socket.io-client')

socketURL = 'http://localhost:7500'

options =
  transports: ['websocket'],
  'force new connection': true

chatUser1 = name:'Test1'
chatUser2 = name:'Test2'
chatUser3 = name:'Test3'

describe "Looking Glass Server", ->
  before ->
    @server = require "../looking-glass"
  after ->
    @server.stop()

  it "should keep track of new users", (done) ->

    client1 = io.connect(socketURL, options)
    client1.on "connect", (data) ->
      client1.emit "connection name", chatUser1
      client2 = io.connect(socketURL, options)
      client2.emit "connection name", chatUser2
    client1.on "testClients", (clients) ->
      clients.should.containEql("Test1")
      clients.should.containEql("Test2")
    done()

  it "should print out the list of cameras", (done) ->
    
    client1 = io.connect(socketURL, options)
    client1.emit "requestCameraList", (result) ->
      console.log result.cameras
    done()