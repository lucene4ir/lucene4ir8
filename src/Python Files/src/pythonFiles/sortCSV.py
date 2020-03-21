import operator
def requiredLine (line,targetFBTerms , targetCorpus):
    # CSV Line Format :
    # corpus,indexType,qryFilter,qryCount,model,maxResults,fbTerms,fbDocs,RetrievalCoefficient
    # Trec-MAP,Trec-Bref,Trec-P10,CWL-MAP,CWL-P10,CWL-RBP0.6,CWL-RBP0.8
    parts = line.split(',')
    corpus = line[0].upper()
    fbterms = int(parts[6])
    # fbdocs = int(parts[7])
    # cwlMap = parts[12]
    # coefficient = float(parts[8])
    model = parts[4]
    return model == 'PL2' and fbterms == targetFBTerms and corpus == targetCorpus

def sortFile (targetFBTerms , targetCorpus):
    csvFile = r'C:\Users\kkb19103\Desktop\CSV\CSV\Per.csv'
    lines = []
    f = open(csvFile, 'r')
    f.readline()
    for line in f:
        if requiredLine(line, targetFBTerms , targetCorpus):
            line = line.replace('\n', '')
            line = line.split(',')
            lines.append(line)
    sortIndex = 9
    sort = sorted(lines, key=operator.itemgetter(sortIndex), reverse=True)
    for line in sort[:2]:
        print(','.join(line).replace(',', '\t'))

def main ():
    corpus = 'A'
    for i in range(10 ,40 ,10):
        sortFile(i,corpus)

if __name__ == '__main__':
    main()