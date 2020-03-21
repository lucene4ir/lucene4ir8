import clsCWL as cwl
import clsTrec as trec
import clsRetrievabilityCalculator as rc
import pandas as pd

# ~/trecEval/trec_eval ~/trecEval/Qrels/qrels.core18.txt ~/Desktop/WA-BM25-BI-50-C1000-b0.1.res > ~/Desktop/WA-BM25-BI-50-C1000-b0.1.trec
# WA-BM25-BI-50-C1000-b0.1.res

def displayPerformance ():
    resFile = r'~/Anserini8/out/CO-BM25-UI-50-C1000-RM3-fbdocs5-fbterms50-b0.75.res'
    # 'W': 'qrels.core18.txt',
    # 'C': '307-690.qrel',
    # 'A': 'Aquaint-AnseriniQrel.qrel'
    gainFile = r'~/trecEval/Qrels/307-690.qrel'

    print('Core17 - FbDocs = 05 , FbTerms = 50')
    a = cwl.cwlEval.getUsingUbuntu(resFile, gainFile)
    print('CWL Results [MAP - P10 - R6 - R8] : ', a)
    a = trec.TrecClass.getTrecData(resFile, gainFile)
    print('Trec Results [MAP - BPref - P10] : ', a)

def testPandas():
    rows = []
    for i in range(1,6,1):
        row = [*range(i,i+5,1)]
        rows.append(row)


    a = 'a b c d e'.split(' ')
    df = pd.DataFrame(rows, columns=a )
    criteria = df['a'] > 2
    df2 = len(df[criteria])
    # df2 = df[df.iloc[0 , 1 , 2],df.columns[0:4]]
    print(df2)
    # df3 = df.join(df2.set_index('a') ,on='a' ,how='inner', lsuffix='_dfA' , rsuffix='_dfB')
    # df3 = pd.merge(df,df2,how='right', on='a',suffixes=['_df','_df2'])



def main():
    #testPandas()
    [a,b,c] = rc.cslRetrievabilityCalculator.calculate('',0,5,'')


if __name__ == '__main__':
    main()
