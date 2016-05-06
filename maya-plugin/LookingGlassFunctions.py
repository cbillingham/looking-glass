# Maya Looking Glass functions
# Loaded into Maya's Python script interpreter
#
# Note: Using global variables to store Maya objects is extremely hacky
#       Needs to be packaged as a Python module

import Maya.cmds as cmds
import Maya.api.OpenMaya as om

def tangoCamera(name):
    global tango
    global tangoName
    tangoName = name
    obj = cmds.ls(name)
    selectionList = om.MSelectionList()
    selectionList.add( obj[0] )    
    obj = selectionList.getDependNode( 0 )
    tango = om.MFnTransform( obj )
    
def tangoSetKeyFrame():
    global tangoName
    cmds.keyframe(tangoName)

def tangoStore():
    global tangoT
    global tangoR
    global tango
    tangoT = tango.translation(om.MSpace.kObject)
    tangoR = tango.rotation(asQuaternion=True)

def tangoUpdateTranslate(x,y,z):
    global tangoT
    global tango
    tango.setTranslation(tangoT + om.MVector(x,y,z),om.MSpace.kObject)

def tangoUpdateRotate(x,y,z,w):
    global tangoR
    global tango
    tango.setRotation(tangoR * om.MQuaternion(-x,-y,-z,w),om.MSpace.kObject)
