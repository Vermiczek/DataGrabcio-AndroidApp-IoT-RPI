#!/usr/bin/python3

import json
import sys
import getopt
import linecache
import time

from sense_emu import SenseHat, ACTION_PRESSED, ACTION_HELD, ACTION_RELEASED

sense = SenseHat()

x = 0
y = 0
z = 0

class EnvData :
       def __init__ (self, Roll, Pitch, Yaw, Temperature, Pressure, Humidity, x, y):
               self.Temperature = Temperature
               self.Pressure = Pressure
               self.Humidity = Humidity
               self.Roll = Roll
               self.Pitch = Pitch
               self.Yaw = Yaw
               self.x = x
               self.y = y
              

def pushed_up(event):
    global y
    if event.action != ACTION_RELEASED:
        y = y + 1

def pushed_down(event):
    global y
    if event.action != ACTION_RELEASED:
        y = y - 1

def pushed_left(event):
    global x
    if event.action != ACTION_RELEASED:
        x = x - 1

def pushed_right(event):
    global x
    if event.action != ACTION_RELEASED:
        x = x + 1
        

        
def write_data_to_file():

    with open('chartsdata.json', 'w+') as outfile:
        obj_data = EnvData(Roll , Pitch, Yaw, Temperature, Pressure, Humidity, x, y)
        result = json.dumps(obj_data.__dict__)
        outfile.write(result)

while 1:
    orientation_degrees = sense.get_orientation_degrees()
    Roll=orientation_degrees["roll"]
    Pitch=orientation_degrees["pitch"]
    Yaw=orientation_degrees["yaw"]
    Temperature = sense.get_temperature()
    Pressure = sense.get_pressure()
    Humidity = sense.get_humidity()
    sense.stick.direction_up = pushed_up
    sense.stick.direction_down = pushed_down
    sense.stick.direction_left = pushed_left
    sense.stick.direction_right = pushed_right
    write_data_to_file()
    
    time.sleep(0.1)
