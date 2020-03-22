import pandas as pd
from src.classes.clsGeneral import General as gen

def getModel (model):
    model = model.upper()
    if model == 'B' :
        model = 'BM25'
        val = 0.75
    elif model == 'C':
        model = 'PL2'
        val = 5.1
    else:
        model = 'LMD'
        val = 310
    return [val,model]

def sortFile (targetFBTerms ,targetModel, targetCorpus):
    # CSV Line Format :
    # corpus,indexType,qryFilter,qryCount,model,maxResults,fbTerms,fbDocs,RetrievalCoefficient
    # Trec-MAP,Trec-Bref,Trec-P10,CWL-MAP,CWL-P10,CWL-RBP0.6,CWL-RBP0.8
    csvFile = r'C:\Users\kkb19103\Desktop\CSV\CSV\Per.csv'
    df = pd.read_csv(csvFile)
    criteria = (df['model'] == targetModel) & (df['corpus'] == targetCorpus) & (df['RetrievalCoefficient'] == float(targetFBTerms))
    df = df[criteria].iloc[:,[0,4,6,7,8,9]].sort_values('Trec-MAP',ascending=False)

    print(df.head(5) , len(df))
    return df.head(5)

def groupCol (df , colName):
    df2 = df.groupby(colName).count()
    df2 = df2.iloc[:, 1].sort_values(ascending=False)
    return df2

def main ():
    [i,model] = getModel('mu')
    catf = []
    for corpus in 'a,c,w'.split(','):
        corpus = gen.getCorpus(corpus)
        df = sortFile(i,model,corpus)
        if len(catf) == 0 :
            catf = df
        else:
            catf = pd.concat([catf,df])
    dfterms = groupCol(catf,'fbTerms')
    dfdocs = groupCol(catf,'fbDocs')

    print('DfTerms : \n' , dfterms)
    print('Df Docs : \n', dfdocs)
if __name__ == '__main__':
    main()