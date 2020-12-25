import os

mainPath = ''

def changeFileName (fName):
    # Given  : WA-BM25-UI-RM3-5-50-C1000-b0.05.res
    # Return : WA-BM25-UI-50-C1000-RM3-fbdocs5-fbterms10-b0.05.res
    # WA-BM25-UI-50-C1000-RM3-
    parts = fName.rsplit('-',4)
    fbdocs = parts[1]
    if (fbdocs == '5'):
        fbdocs = '05'
    coefficient = parts[4].replace('b','').replace('.res','')
    result = 'WA-BM25-UI-50-C1000-RM3-fbdocs%s-fbterms10-b%s.res' % (fbdocs,coefficient)
    return result
def getCsvLine(fName):
    # WA-BM25-UI-RM3-5-50-C1000-b0.05.res
    # WAPO, UnigramIndex, combinedQuery, 50, BM25, 1000, 45, 0.8

    # WA-BM25-UI-50-C1000-RM3-fbdocs05-fbterms20-b0.0.res
    # corpus,indexType,qryFilter,qryCount,model,maxResults,fbTerms,fbDocs,RetrievalCoefficient,
    # Trec-MAP,Trec-Bref,Trec-P10,CWL-MAP,CWL-P10,CWL-RBP0.6,CWL-RBP0.8

    prefix = 'WAPO,UnigramIndex,combinedQuery,50,BM25,1000'
    parts = fName.rsplit('-',3)
    fbdocs = parts[1].replace('fbdocs','')
    fbterms = parts[2].replace('fbterms','')
    coefficient = parts[3].replace('b','').replace('.res','')
    result = ','.join([prefix,fbterms,fbdocs,coefficient]) + ','
    for i in range(6):
        result += ' ,'
    result += ' '
    return result

def processFile(file):
    # Given Full File Path
    # newPath = file.replace('BM25','LMD')
    # os.rename(mainPath + '\\' + file, mainPath + '\\' + newPath)
    if (file.__contains__('s5-')):
        print(file)


def iterateFolder (path):
    files = os.listdir(path)
    for file in files:
        item = path + '\\' + file
        if (os.path.isfile(item)):
            if (item.__contains__('s5-')):
                newItem = item.replace('s5-','s05-')
                os.rename(item, newItem)
                print(item)



def main():
    mainPath = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\2nd Experiment - RM3\AllRes\AXD\Bias Measurement'
    if (os.path.isdir(mainPath)):
        iterateFolder(mainPath)

if __name__ == '__main__':
    main()