#!/usr/bin/python
import cgi
import cgitb; cgitb.enable()  # for troubleshooting
import json
from sense_hat import SenseHat

sense = SenseHat()

print("Content-Type: text/html")
print("")

args = cgi.FieldStorage()

for key in args:
	led = json.loads(args.getfirst(key))
	sense.set_pixel(led[0], led[1], led[2], led[3], led[4])
	
print('OK')