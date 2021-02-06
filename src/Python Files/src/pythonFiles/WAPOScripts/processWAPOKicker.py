import pandas as pd
import src.classes.clsRetrievabilityCalculator as rc
import matplotlib.pyplot as plt
import src.pythonFiles.plotters.csvPlotterGeneral as pltGen
import src.classes.general as gen

def extractDocMap(path):
    # df = pd.read_csv(path, '\t')
    df = pd.read_csv(path)
    # df = df.iloc[:, [1, 2]]
    df = df.iloc[:, [2, 3 , 7]]
    df.rename(columns={df.columns[0]: 'docid', df.columns[1]: 'length'},inplace=True)
    return df


def getMainDf(src,docPath, resPath):
    if (src == ''):
        docMap = extractDocMap(docPath)
        resMap = rc.calculate_res_map(resPath, 0, 100)
        df = rc.mergeMaps(docMap, resMap)
        docMap = None
        resMap = None
    else:
        df = pd.read_csv(src)
    return df


def getAverageDf(corpus , df):
    if (corpus.upper() == 'A'):
        df['type'] = df['docid'].str[:3]
        df = df.groupby('type').agg({'r': ['mean']})
        # df.columns = df.columns.droplevel(0)
        # df.rename({'mean': 'averageR'}, inplace=True)
    else:
        df = df.groupby('kicker').agg({'r': ['mean']})
    df.columns = df.columns.droplevel(0)
    return df


def saveFigure(corpus,exp,min,max,xValues, yValues):
    [fFamily, fSize, fWeight] = pltGen.getFont()
    font = {'family': fFamily,
            'weight': fWeight,
            'size': fSize}

    plt.rc('font', **font)

    corpus = gen.General.getCorpus(corpus)
    exp = gen.General.getQryExpansion(exp)
    if ( exp[0] == 'B'):
        title = "%s - %s\nFbdocs = 0 - FbTerms = 0" % (corpus,exp)
    else:
        title = "%s - %s\nFbdocs = 30 - FbTerms = 30" % (corpus,exp)
    plt.title(title)
    plt.xlabel("Document Length")
    plt.ylabel('Cumulative Retrievability - B = 0 - C = 100')
    if (min > 0):
        min -= 1
    max += 10
    plt.xticks(range(min, max, 10))
    plt.plot(xValues, yValues)
    plt.show()
    # outFile = '%s-%s.png' % (corpus[:3],exp)
    # plt.savefig(outFile)

def getDocBucket(df, min, max):
    header = 'length'
    criteria = (df[header] >= min) & (df[header] <= max)
    df = df[criteria].sort_values(header)
    xValues = df[header]
    yValues = df['r']
    return [xValues,yValues]

def outputDf (df , resPath):
    sep = '-'
    path = resPath.rsplit('\\',1)[1]
    parts = path.split(sep)
    # AQ-BM25-UI-300K-C100-AX-fbdocs30-fbterms30-b0.75.res
    path = parts[0] + sep + sep.join(parts[5:8]) + '.sts'
    path = path.replace('fbdocs','').replace('fbterms','')
    df.to_csv(path,index=False)

def getDocGroupCounts(docPath):
    df = extractDocMap(docPath)
    df['type'] = df['docid'].str[:3]
    df = df.groupby('type').count()
    df.drop(['length'],axis=1)
    print ('File : ' , docPath.rsplit('\\',1)[1] )
    print(df)
    # df.columns = df.columns.droplevel(0)
    # df.rename({'mean': 'averageR'}, inplace=True)
    return df

def getInput(corpus,exp):
    # docPath = r'C:\Users\kkb19103\Desktop\TempFiles\AQ-Doclengths.sts'
    # docPath = r'C:\Users\kkb19103\Desktop\TempFiles\CO-Doclengths.sts'
    mainFolder = r'C:\Users\kkb19103\Desktop\TempFiles' + '\\'
    # resPath += 'AQ-BM25-UI-300K-C100-AX-fbdocs30-fbterms30-b0.75.res'
    # resPath += 'AQ-BM25-UI-300K-C100-RM3-fbdocs30-fbterms30-b0.75.res'
    # resPath += 'AQ-BM25-UI-300K-C100-Baseline-fbdocs0-fbterms0-b0.75.res'
    # resPath += 'CO-BM25-UI-300K-C100-AX-fbdocs30-fbterms30-b0.75.res'
    # resPath += 'CO-BM25-UI-300K-C100-RM3-fbdocs30-fbterms30-b0.75.res'

    # AQ-AX-30-30.sts - AQ-RM3-30-30.sts - AQ-Baseline-0-0.sts
    # outFile = 'AQ-RM3-30-30.sts'
    exp = exp.upper()
    corpus = gen.General.getCorpus(corpus)[:2]
    switcher = {
        'B':'Baseline',
        'A': 'AX',
        'R':'RM3'
    }
    exp = switcher.get(exp)

    docFile = corpus + '-Doclengths.sts'
    if (exp[0] == 'B' ):
        resFile = '%s-BM25-UI-300K-C100-%s-fbdocs0-fbterms0-b0.75.res' % (corpus,exp)
        stsFile = '%s-%s-0-0.sts' % (corpus,exp)
    else:
        resFile = '%s-BM25-UI-300K-C100-%s-fbdocs30-fbterms30-b0.75.res' % (corpus,exp)
        stsFile = '%s-%s-30-30.sts' % (corpus, exp)
    docFile = mainFolder + docFile
    resFile = mainFolder + resFile
    return [docFile,resFile,stsFile]


def main(corpus,exp):
    [docFile, resFile, stsFile] = getInput(corpus, exp)
    # stsFile = ''
    # getDocGroupCounts(docFile)
    df = getMainDf(stsFile, docFile, resFile)
    # outputDf(df, resFile)
    df = getAverageDf(corpus, df)
    # max = 100
    # min = 0
    # for i in range(1):
    #     [xValues, yValues] = getDocBucket(df, min, max)
    #     saveFigure(corpus,exp,min,max,xValues, yValues)
    #     max+=100
    #     min = max - 99
    print(df)
    print('Done')
    fName = stsFile.replace("sts",'csv')
    df.to_csv(fName)

if __name__ == '__main__':
    corpus = 'w'
    exp = 'b'
    for exp in 'r b a'.split():
        main(corpus,exp)