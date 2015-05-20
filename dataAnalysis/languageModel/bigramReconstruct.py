import numpy as np
import os
import re
import math

modelName = 'output/bigramModels/regular.model.txt'
inputFile = 'input/2.txt'
outputFile = 'input/2.out.txt'


f = open(modelName, 'r')
lines = [ x.strip() for x in f.readlines() ]
f.close()

segToken = [ idx for idx, line in enumerate(lines) if line == '==========' ]

# unigram
idx2word = {}
word2idx = {}
unigram = {}
ta = segToken[0] + 1
tb = segToken[1]
for idx, line in enumerate( lines[ta:tb] ):
    (w, p) = line.split('\t')
    unigram[idx] = float(p)
    idx2word[idx] = w
    word2idx[w] = idx

# bigram
bigram = {}
ta = segToken[1] + 1
tb = segToken[2]
for line in lines[ta:tb]:
    (wa, wb, p) = line.split('\t')
    idxa = word2idx[wa]
    idxb = word2idx[wb]
    bigram[ (idxa, idxb) ] = float(p)

# default bigram
bigramDefault = {}
ta = segToken[2] + 1
tb = segToken[3]
for line in lines[ta:tb]:
    (w, p) = line.split('\t')
    idx = word2idx[w]
    bigramDefault[idx] = float(p)

# read input
f = open(inputFile, 'r')
content = f.read()
f.close()


nrWords = len(unigram) + 1   # last word as empty
lenContent = len(content)
dummyWordIdx = nrWords - 1

nonMatchPenalty = math.log10( 1.0 / nrWords )
ninf = -1000000000.0

dp = np.empty( [ (lenContent+1), nrWords ] )
dp.fill(ninf)
fromCIdx = np.empty( [ (lenContent+1), nrWords ] )  # character index
fromWIdx = np.empty( [ (lenContent+1), nrWords ] )  # word index

dp[0][dummyWordIdx] = 0.0

for ncidx in range(lenContent):
    print('ncidx: ', ncidx, ' /', lenContent)
    for nwidx in range(nrWords):
        if dp[ncidx][nwidx] == ninf:
            continue
        
        # option 1: go for penalty
        tcidx = ncidx + 1
        twidx = nwidx
        if dp[ncidx][nwidx] + nonMatchPenalty > dp[tcidx][twidx]:
            dp[tcidx][twidx] = dp[ncidx][nwidx] + nonMatchPenalty
            fromCIdx[tcidx][twidx] = ncidx
            fromWIdx[tcidx][twidx] = nwidx

        for twidx in range(nrWords-1):
            tcidx = ncidx + len( idx2word[twidx] )
            #print(tcidx, '/', lenContent, twidx, '/', nrWords)
            if tcidx <= lenContent and content[ncidx:tcidx] == idx2word[twidx]:
                tvalue = dp[ncidx][nwidx] + unigram[twidx]
                if tvalue > dp[tcidx][twidx]:
                    dp[tcidx][twidx] = tvalue
                    fromCIdx[tcidx][twidx] = ncidx
                    fromWIdx[tcidx][twidx] = nwidx
                    
endcIdx = lenContent - 1
endwIdx = 0
for widx in range(nrWords-1):
    if dp[endcIdx][widx] > dp[endcIdx][endwIdx]:
        endwIdx = widx

ansWords = []
while endcIdx > 0:
    #print('==>', endcIdx, endwIdx, idx2word[endwIdx])
    ansWords += [ idx2word[endwIdx] ]
    pcidx, pwidx = fromCIdx[endcIdx][endwIdx], fromWIdx[endcIdx][endwIdx]
    endcIdx, endwIdx = pcidx, pwidx

ansWords.reverse()
#print(ansWords)
f = open(outputFile, 'w')
f.write( ' '.join( tuple(ansWords) ) )
f.close()
