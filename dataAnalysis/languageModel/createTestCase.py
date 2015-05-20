import numpy as np
import os
import re
import math
import random


inputFile  = 'input/answer.txt'
outputFile = 'input/7.txt'

probMiss = 0.1
probExtra = 0.0
probReplace = 0.0

# change the shift keys to non-shift version
keyFrom = '~!@#$%^&*()_+|{}:"<>?'
keyTo   = '`1234567890-=\\[];\',./'
keyMaps = dict(zip(keyFrom, keyTo))


probMissThres = probMiss
probExtraThres = probMissThres + probExtra
probReplaceThres = probExtraThres + probReplace

f = open(inputFile)
content = f.read()
f.close()

for key, value in keyMaps.items():
    #print(key, value)
    content = content.replace(key, value)

content = content.lower()
#words = re.split(' |,|.|;|\'|\[|\]|\n|\r', content)
words = re.split(' |,|;|\'|\[|\]|\n|\r|\.|\\\\|-|=|/|\t', content)
words = [ x for x in words if x.strip() != '' ]
processedContent = ''.join( tuple(words) )

noiseContent = ''
for c in processedContent:
	r = random.random()
	if r < probMissThres:
		pass
	elif r < probExtraThres:
		noiseContent += c
		noiseContent += str(unichr(random.randint(0, 25) + 97))
		while random.random() < probExtra:
			noiseContent += str(unichr(random.randint(0, 25) + 97))
	elif r < probReplaceThres:
		noiseContent += str(unichr(random.randint(0, 25) + 97))
	else:
		noiseContent += c
		
f = open(outputFile, 'w')
f.write(noiseContent)
f.close()
