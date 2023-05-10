#https://docs.openmv.io/
# Face Eye Detection Example
#
# This script uses the built-in frontalface detector to find a face and then
# the eyes within the face. If you want to determine the eye gaze please see the
# iris_detection script for an example on how to do that.

import sensor, time, image
from machine import UART


# Function to check if any serial data receive from Arduino
def available():
    return uart.any()

# Function to read serial data from host
def read_byte():
    return uart.readchar()


# Function to process each byte received from Arduino
def parse_byte(byte):
    print("Serial Input: ", byte)

def UpdateHost(value):
    uart.write()
    uart.writechar(255)
    uart.writechar(1)
    uart.writechar(value//253)               # Write high byte.  Since we're using 255 and 254 as markers - filters these out too
    uart.writechar(value - 253*(value//253))   # Write low byte
    uart.writechar(254)    # end of transmission marker

# Reset sensorS
sensor.reset()

# Sensor settings
sensor.set_contrast(1)
sensor.set_gainceiling(16)
sensor.set_framesize(sensor.HQVGA)
sensor.set_pixformat(sensor.GRAYSCALE)

# Load Haar Cascade
# By default this will use all stages, lower satges is faster but less accurate.
face_cascade = image.HaarCascade("frontalface", stages=25)
eyes_cascade = image.HaarCascade("eye", stages=24)
print(face_cascade, eyes_cascade)

# Setup comm link
uart = UART(3, 115200, timeout_char=1000)	# Setup communications with Arduino - be sure speeds match


# FPS clock
clock = time.clock()


while (True):
    clock.tick()

    # Capture snapshot
    img = sensor.snapshot()

    # Find a face !
    # Note: Lower scale factor scales-down the image more and detects smaller objects.
    # Higher threshold results in a higher detection rate, with more false positives.
    objects = img.find_features(face_cascade, threshold=0.5, scale_factor=1.5)

    # Draw faces
    for face in objects:
        img.draw_rectangle(face)
        UpdateHost(1)
        # Now find eyes within each face.
        # Note: Use a higher threshold here (more detections) and lower scale (to find small objects)
        eyes = img.find_features(eyes_cascade, threshold=0.5, scale_factor=1.2, roi=face)
        for e in eyes:
            eye = img.find_eye(e)
            img.draw_rectangle(eye)
            UpdateHost(0)


    # Print FPS.
    # Note: Actual FPS is higher, streaming the FB makes it slower.
    print(clock.fps())

    #for looping in range(available()): # Check for Serial input and process data from Arduino
        #parse_byte(read_byte())




