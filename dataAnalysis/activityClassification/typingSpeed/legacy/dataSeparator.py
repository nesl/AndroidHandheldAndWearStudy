import sys
import os

if len(sys.argv) == 1:
    print('usage: ./' + sys.argv[0] + ' folder name')
    print('')
    print('Example: if you have a under folder')
    print('         <data>/activityClassification/typingSpeed/01020304_bo,')
    print('         step 1. rename the sensor file to sensorRaw.txt')
    print('         step 2. python3 ' + sys.argv[0] + ' 01020304_bo')
    exit()

folder = '../../data/activityClassification/typingSpeed/' + sys.argv[1] + '/'
inFilePath = folder + 'sensorRaw.txt'

if not os.path.isfile(inFilePath):
    print('file \'' + inFilePath + '\' cannot be found')
    exit()

f = open(inFilePath, 'r')
#lines = [ x.strip() for x in f.readlines() ]
lines = f.readlines()
f.close()

tasks = [
        # sensor type, file ext, # comma
        [ 1,           'acc',    6],
        [ 4,           'gyro',   6],
        [ 2,           'mag',    6],
        [ 9,           'grav',   6],
]

for task in tasks:
    outFilePath = folder + 'sensorRaw_' + task[1] + '.txt'
    fo = open(outFilePath, 'w')
    prefix = '1,' + str(task[0]) + ','
    plen = len(prefix)
    print(plen, prefix)
    for line in lines:
        if len(line.split(',')) - 1 == task[2] and line[0:plen] == prefix:
            fo.write(line)
    fo.close()

