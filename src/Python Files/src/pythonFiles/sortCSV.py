import pandas as pd
from src.classes import clsGeneral as gn

def sortFile (targetFBTerms , targetCorpus):
    csvFile = r'C:\Users\kkb19103\Desktop\CSV\CSV\Per.csv'
    df = pd.read_csv(csvFile)
    # header Names - Per.CSV
    # corpus-indexType-qryFilter-qryCount-model-maxResults-fbTerms
    # -fbDocs-RetrievalCoefficient-Trec-MAP
    # Trec-Bref-Trec-P10-CWL-MAP-CWL-P10-CWL-RBP0.6-CWL-RBP0.8

    targetCorpus = gn.General.getCorpus(targetCorpus)
    criteria = (df['corpus'] == targetCorpus) & (df['model'] == 'BM25') & (df['RetrievalCoefficient'] == 0.75) \
               & (df['fbTerms'] == targetFBTerms)

    df = df[criteria].iloc[:,[0,4,6,7,8,9]]
    df = df.sort_values('Trec-MAP',ascending=False).head(5)
    print(df)

def main ():
    corpus = 'A'
    for i in range(10,40,10):
        sortFile(i,corpus)

if __name__ == '__main__':
    main()