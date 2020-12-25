import pingouin as pg
import pandas as pd
import src.pythonFiles.plotters.csvPlotterGeneral as gen

def getValues (df , exp):
    criteria = df['qryExpansion'] == exp
    dfSub = df[criteria]
    dfSub = dfSub['Trec-MAP']
    return dfSub

def main():
    # We have all combination of fbTerms and fbDocs 5:5:50
    # Given constant fbTerms (priority ) or zero and constant fbDocs
    # etract all relative values then make TTest
    terms = 10
    docs = 0
    exTitle = 'qryExpansion'
    corpus = 'Aquaint'
    csvfile = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\2nd Experiment - RM3\CSV\Ex2Per.csv'
    df = pd.read_csv(csvfile)
    criteria = {
        "RetrievalCoefficient":0.75,
        "corpus":corpus
    }
    if (terms > 0 ):
        criteria['fbTerms'] = terms
    else:
        criteria['fbDocs'] = docs
    criteria = gen.getCriteria(df,criteria)
    criteria &= (  ((df[exTitle] == 'AX') & (df['beta'] == 0.4) )|
                   ((df[exTitle] == 'RM3') & (df['beta'] == 0.5)) )

    df = df[criteria]
    x = getValues(df,'AX')
    y = getValues(df, 'RM3')
    res = pg.ttest(x,y)
    print(res)
if __name__ == '__main__':
    main()