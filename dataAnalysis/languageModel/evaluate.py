import numpy
import math

inFileGnd = 'input/answer.txt'
inFileAns = 'input/7.out.txt'

f = open(inFileGnd)
lineGnd = f.read()
f.close()

f = open(inFileAns)
lineAns = f.read()
f.close()

wordsGnd = lineGnd.split()
nrCharsGnd = 0
for word in wordsGnd:
    nrCharsGnd += len(word)

wordsAns = lineAns.split()

nrWordsGnd = len(wordsGnd)
nrWordsAns = len(wordsAns)

dp = numpy.zeros( [nrWordsGnd+1, nrWordsAns+1] )
fromGnd = numpy.zeros( [nrWordsGnd+1, nrWordsAns+1], dtype=numpy.int )
fromAns = numpy.zeros( [nrWordsGnd+1, nrWordsAns+1], dtype=numpy.int  )

for i in range(1, nrWordsGnd):
    dp[i][0] = -999999
for i in range(1, nrWordsAns):
    dp[0][i] = -999999

for i in range(nrWordsGnd):
    for j in range(nrWordsAns):
        dp[i+1][j+1] = dp[i][j+1]
        fromGnd[i+1][j+1] = i
        fromAns[i+1][j+1] = j+1

        if dp[i+1][j+1] < dp[i+1][j]:
            dp[i+1][j+1] = dp[i+1][j]
            fromGnd[i+1][j+1] = i+1
            fromAns[i+1][j+1] = j
        if wordsGnd[i] == wordsAns[j] and dp[i+1][j+1] < dp[i][j] + 1:
            dp[i+1][j+1] = dp[i][j] + 1
            fromGnd[i+1][j+1] = i
            fromAns[i+1][j+1] = j


nrWordsMatch = dp[nrWordsGnd][nrWordsAns]
print('LCS ratio: ', (float(nrWordsMatch) / nrWordsGnd))

eIdxGnd = nrWordsGnd
eIdxAns = nrWordsAns
nrCharsMatch = 0
while eIdxGnd > 0 and eIdxAns > 0:
	#print(eIdxGnd, eIdxAns)
	pidxg, pidxa = fromGnd[eIdxGnd][eIdxAns], fromAns[eIdxGnd][eIdxAns]
	if pidxg == eIdxGnd - 1 and pidxa == eIdxAns - 1:
		nrCharsMatch += len(wordsGnd[eIdxGnd-1])
	eIdxGnd, eIdxAns = pidxg, pidxa

print('char match ratio: ', (float(nrCharsMatch) / nrCharsGnd))
