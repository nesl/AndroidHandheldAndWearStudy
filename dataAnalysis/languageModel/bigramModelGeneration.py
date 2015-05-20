import numpy as np
import os
import re
import math

f = open('MASC_Folder_Structure.txt', 'r')
lines = [ x.strip()[10:] for x in f.readlines() ]
f.close()

folders = lines[10:25]  # line 11-25 in the content file

outFolder = 'output/bigramModels/'


# change the shift keys to non-shift version
keyFrom = '~!@#$%^&*()_+|{}:"<>?'
keyTo   = '`1234567890-=\\[];\',./'
keyMaps = dict(zip(keyFrom, keyTo))


# symbols of interest
#soi = '1234567890qwertyuiopasdfghjklzxcvbnm '  # symbol of interest
soi = '12345qwertasdfgzxcvb 67890yuiophjklnm'  # symbol of interest
slen = len(soi)
symMap = {}

wordCnt = {}
bigramCnt = {}
hasNextWordCnt = {}


for i in range(len(soi)):
    symMap[ soi[i] ] = i;

for folder in folders:
    folderPath = 'data/written/' + folder + '/'
    cmd = 'ls ' + folderPath + '*.txt > /tmp/t'
    os.system(cmd)

    f = open('/tmp/t')
    files = [ x.strip() for x in f.readlines() ]
    f.close()
    
    for theFile in files:
        print('parse file: ' + theFile)

        f = open(theFile)
        content = f.read()
        f.close()

        for key, value in keyMaps.items():
            #print(key, value)
            content = content.replace(key, value)

        content = content.lower()
        #words = re.split(' |,|.|;|\'|\[|\]|\n|\r', content)
        words = re.split(' |,|;|\'|\[|\]|\n|\r|\.|\\\\|-|=|/|\t', content)
        words = [ x.strip() for x in words ]
        words = [ x for x in words if x != '' ]

        for w in words:
            if not w in wordCnt:
                wordCnt[w] = 1
            else:
                wordCnt[w] += 1
            if wordCnt[w] == ' ':
                print('space detect')
                exit()

        for wbi in zip( words[:-1], words[1:] ):
            #print(wa, wb)
            if not wbi in bigramCnt:
                bigramCnt[wbi] = 1
            else:
                bigramCnt[wbi] += 1

            if not wbi[0] in hasNextWordCnt:
                hasNextWordCnt[ wbi[0] ] = 1
            else:
                hasNextWordCnt[ wbi[0] ] += 1

        #print(content)
        #print(words)
        #exit()

    #fo = open('output/entireSet/' + folder + '.csv', 'w')
    #for row in stat:
    #    fo.write( ",".join( tuple(map(str, row)) ) + '\n')


nrAlphabetOnly = 0
for w in wordCnt:
    if w.isalpha():
        nrAlphabetOnly += 1
        

print('total unique word counts:', len(wordCnt), '(', nrAlphabetOnly, ')')
print('total bigram size:', len(bigramCnt))


totalWordCnt = 0
for key in wordCnt:
    totalWordCnt += wordCnt[key]
    

print('total word instances:', totalWordCnt)

f = open(outFolder + 'regular.model.txt', 'w')
f.write('==========\n')  # unigram

for key in wordCnt:
    v = math.log10( float(wordCnt[key]) / totalWordCnt )
    f.write(key + '\t' + str(v) + '\n')

f.write('==========\n')  # bigram
for key in bigramCnt:
    wa = key[0]
    wb = key[1]
    total = hasNextWordCnt[wa] + len(wordCnt)
    v = math.log10( float(bigramCnt[key] + 1.0) / float(total) )
    f.write(wa + '\t' + wb + '\t' + str(v) + '\n')

f.write('==========\n')  # default bigram
for key in wordCnt:
    if not key in hasNextWordCnt:
        total = 0
    else:
        total = hasNextWordCnt[key]
    total += len(wordCnt)
    v = math.log10( 1.0 / total )
    f.write(key + '\t' + str(v) + '\n')

f.write('==========\n')  # end token 

f.close()
