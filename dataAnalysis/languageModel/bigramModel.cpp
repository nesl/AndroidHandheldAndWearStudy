#ifndef BIGRAM_MODEL_H
#define BIGRAM_MODEL_H

#include <stdio.h>
#include <map>
#include <vector>
#include <string>
#include <string.h>
#include <stdint.h>

using namespace std;

class BigramModel {
	public:
		// variables
		vector<string> idx2word;
		map<string, int> word2idx;
		vector<double> unigram;
		vector<int> wordLen;

		map<uint64_t, double> bigram;
		vector<double> bigramDefault;

		int widxDummy;
		int nrWords;

		// methods
		BigramModel(const char *fileName);
		inline double getBigramP(int idxa, int idxb);
	private:
		char *fgetsStopByBorder(char *line, int nsize, FILE *f);
		inline uint64_t cc(int a, int b);  // concatenation
};

BigramModel::BigramModel(const char *fileName) {
	FILE *fin = fopen(fileName, "r");
	char line[10000];

	while (fgetsStopByBorder(line, 10000, fin) != NULL);
	
	// unigram
	while (fgetsStopByBorder(line, 10000, fin) != NULL) {
		char tw[100];
		double tp;
		sscanf(line, "%s\t%lf", tw, &tp);
		string ts(tw);
		idx2word.push_back(ts);
		word2idx[ts] = idx2word.size() - 1;
		unigram.push_back(tp);
		wordLen.push_back(strlen(tw));
	}
	idx2word.push_back("");
	nrWords = idx2word.size();
	widxDummy = nrWords - 1;

	// bigram
	while (fgetsStopByBorder(line, 10000, fin) != NULL) {
		char twa[100], twb[100];
		double tp;
		sscanf(line, "%s\t%s\t%lf", twa, twb, &tp);
		int na = word2idx[ string(twa) ];
		int nb = word2idx[ string(twb) ];
		uint64_t bidx = cc(na, nb);
		bigram[bidx] = tp;
	}
	
	// bigram default value
	bigramDefault.resize(nrWords);
	while (fgetsStopByBorder(line, 10000, fin) != NULL) {
		char tw[100];
		double tp;
		sscanf(line, "%s\t%lf", tw, &tp);
		int idx = word2idx[ string(tw) ];
		bigram[idx] = tp;
	}

	fclose(fin);
}


double BigramModel::getBigramP(int idxa, int idxb) {
	uint64_t bidx = cc(idxa, idxb);
	if (bigram.find(bidx) != bigram.end())
		return bigram[bidx];
	else
		return bigramDefault[idxa];
}


uint64_t BigramModel::cc(int a, int b) {
	return (((uint64_t)a) << 32) | b;
}

char* BigramModel::fgetsStopByBorder(char *line, int nsize, FILE *f) {
	fgets(line, nsize, f);
	//printf("%sxxxx\n", line);
	if (strcmp(line, "==========\n") == 0)
		return NULL;
	return line;
}

#endif
