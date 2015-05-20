import numpy as np

f = open('raw.txt')
lines = [ x for x in f.readlines() if x != '\n' ]
f.close()

content = "".join(lines)

keyFrom = '~!@#$%^&*()_+|{}:"<>?'
keyTo   = '`1234567890-=\\[];\',./'

keyMaps = dict(zip(keyFrom, keyTo))
#print(keyMaps)

#content = 'abcdefghijklmnopqrstuvwxyz'

for key, value in keyMaps.items():
    #print(key, value)
    content = content.replace(key, value)

content_bk = content

fo = open('processed.txt', 'w')

n = 0
while n < len(content):
    if content[n] == '\n' or (content[n] == ' ' and n > 80):
        fo.write( '\t\"' + content[:n] + '\",\n' )
        content = content[(n+1):]
        n = 0
    else:
        n += 1

fo.close()


#soi = '1234567890qwertyuiopasdfghjklzxcvbnm '  # symbol of interest
soi = '12345qwertasdfgzxcvbn 67890yuiophjklm'  # symbol of interest
slen = len(soi)
symMap = {}

for i in range(len(soi)):
    symMap[ soi[i] ] = i;

stat = [ [0 for x in range(slen)] for x in range(slen) ]
print(stat)

content = content_bk.lower()
for i in range(len(content) - 1):
    a = content[i]
    b = content[i+1]
    print(a, b)
    if a in soi and b in soi:
        ai = symMap[a]
        bi = symMap[b]
        stat[ai][bi] += 1

print(stat)

fo = open('adjacentMatrix.csv', 'w')
for row in stat:
    fo.write( ",".join( tuple(map(str, row)) ) + '\n')
