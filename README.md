![looking-glass-logo](/images/logo.png)

Looking Glass is an Autodesk Maya scripted plugin and Android application that allow a Maya user to use their Project Tango mobile device as a virtual camera capture device. The application shows a live view of the current camera frame as displayed in the Maya viewport. Users are be able to record camera motion in the application by playing the animation scene in real time. The app allows the Maya camera to be controlled by the position and rotation of the device. For example, if the user wants to pan the Maya camera to the left they can turn their device to the left to control the camera in Maya.

Please check out the [Looking Glass Wiki](https://github.com/cbillingham/looking-glass/wiki) for development information.

![looking-glass-diagram](/images/diagram.png)  
**Figure 1:** The application works by transferring camera data from Project Tango to Autodesk Maya

## Purpose
The primary purpose of Looking Glass is to allow individuals and small companies the ability to easily record realistic camera movements without the hassle of expensive and complicated motion capture rooms or other motion tracking technology. It brings the virtual camera directly to your device. Looking Glass aims to create an open-source base for virtual camera capture through mobile devices.

## How to Use It
A user loads the Maya plugin, which launches a websocket server on the host machine. Launching
the app on Project Tango tablet brings up the connection screen. After connecting with the host server, the user can manage cameras from the main menu. After selecting a camera, the camera record view allows the user to toggle transformation data, play through the animated keyframes, activate position tracking, and record keyframes based on the positional tracking.

## Development Status
Currently, Looking Glass will connect to Maya through a Python script loaded into Maya's console. This was a fast prototype to confirm that the implementation would work. With this current version, connecting to cameras and controlling movement work at high frame-rates. However, because Maya is a single-threaded application, recording is slow through the Python implementation. The next step in development is to program an integrated C++ Maya plugin that takes advantage of the MPx::ThreadedDeviceNode class.

### Punch List
* C++ Maya plugin
  - Makes use of MPx::ThreadedDeviceNode for maximum integration
* Redesign current record UI view
  - Include timeslider feature
* Implement live-streaming from Maya viewport
  - Start with VLC screen capture prototype, then work towards integrated video streamer
