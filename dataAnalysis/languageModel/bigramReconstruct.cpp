#include <stdio.h>
#include <vector>
#include <map>
#include <string>
#include <string.h>
#include <math.h>

#include "bigramModel.cpp"

using namespace std;

const char *modelName = "output/bigramModels/regular.model.txt";
const char *inputFile = "input/7.txt";
const char *outputFile = "input/7.out.txt";

const int MAX_INPUT_SIZE = 50000;

const double NINF = -10000000.0;

template <class T>
T** make2dArray(int na, int nb) {
	T** re = new T*[na];
	for (int i = 0; i < na; i++)
		re[i] = new T[nb];
	return re;
}


double charReplacementProb(const char *wa, const int lena, const char *wb, const int lenb) {
	if (lena != lenb)
		return NINF;
	for (int i = 0; i < lena; i++)
		if (wa[i] != wb[i])
			return NINF;
	return 0.0;
}


int main() {
	BigramModel model(modelName);

	printf("nrWords = %d\n", model.nrWords);

	char content[MAX_INPUT_SIZE];
	FILE *fin = fopen(inputFile, "r");
	fread(content, sizeof(char), MAX_INPUT_SIZE, fin);
	fclose(fin);

	int contentLen = strlen(content);

	printf("content length = %d\n", contentLen);

	const double penalityOfNonMatch = log10( 1.0 / model.nrWords / 2.0 );

	double **dp = make2dArray<double>(contentLen+1, model.nrWords);
	int **fromCIdx = make2dArray<int>(contentLen+1, model.nrWords);
	int **fromWIdx = make2dArray<int>(contentLen+1, model.nrWords);

	for (int i = 0; i < contentLen+1; i++)
		for (int j = 0; j < model.nrWords; j++)
			dp[i][j] = NINF;

	dp[0][ model.widxDummy ] = 0.0;

	int tcidx, twidx;
	double tvalue;
	for (int ncidx = 0; ncidx < contentLen; ncidx++) {
		printf("ncidx=%d (/%d)\n", ncidx, contentLen);
		for (int nwidx = 0; nwidx < model.nrWords; nwidx++) {
			if (dp[ncidx][nwidx] == NINF)
				continue;

			// option 1: go for penalty
			tcidx = ncidx + 1;
			twidx = model.widxDummy;
			tvalue = dp[ncidx][nwidx] + penalityOfNonMatch;
			if (tvalue > dp[tcidx][twidx]) {
				dp[tcidx][twidx] = dp[ncidx][nwidx] + tvalue;
				fromCIdx[tcidx][twidx] = ncidx;
				fromWIdx[tcidx][twidx] = nwidx;
			}

			// option 2: go for bigram
			for (twidx = 0; twidx < model.nrWords-1; twidx++) {
				tcidx = ncidx + model.wordLen[twidx];
				if (tcidx <= contentLen) {
					double replaceProb = charReplacementProb(
							content+ncidx, model.wordLen[twidx],
							model.idx2word[twidx].c_str(), model.wordLen[twidx]);
					//for (int i = 0; i < ncidx; i++)
					//	printf("%c", content[ncidx + i]);
					//printf(" %s %lf\n", model.idx2word[twidx].c_str(), replaceProb);
					double tvalue = dp[ncidx][nwidx] + model.unigram[twidx] + replaceProb;
					if (tvalue > dp[tcidx][twidx]) {
						dp[tcidx][twidx] = tvalue;
						fromCIdx[tcidx][twidx] = ncidx;
						fromWIdx[tcidx][twidx] = nwidx;
					}
				}
			}
		}
	}

	vector<string> ans;

	int endCIdx = contentLen - 1;
	int endWIdx = 0;
	for (int widx = 1; widx < model.nrWords-1; widx++)
		if (dp[endCIdx][widx] > dp[endCIdx][endWIdx])
			endWIdx = widx;


	while (endCIdx > 0) {
		if (endWIdx == model.widxDummy)
			ans.push_back("?");
		else
			ans.push_back( model.idx2word[endWIdx] );

		int pcidx = fromCIdx[endCIdx][endWIdx];
		int pwidx = fromWIdx[endCIdx][endWIdx];
		endCIdx = pcidx;
		endWIdx = pwidx;
	}

	FILE *fout = fopen(outputFile, "w");
	for (int i = ans.size() - 1; i >= 0; i--)
		fprintf(fout, "%s ", ans[i].c_str());
	
	fclose(fout);

	// lazy delete... I hope C++ can do GC...

	return 0;
}
