from src.classes.clsRetrievabilityCalculator import cslRetrievabilityCalculator as rc
from src.classes.general import General as gen
import os

resFolder = ''

def getRetFile(key,b,coefficient):
    # CSV Line Format :
    # corpus,indexType,qryFilter,qryCount,model,maxResults,fbTerms,fbDocs,RetrievalCoefficient
    # WA-BM25-UI-300K-C100-gb0.5-b0.2.ret
    global resFolder
    parts = key.split(',')
    corpus = parts[0][:2].upper()
    model = parts[4]
    modelc = gen.getModelCoefficient(model)
    result = '%s\%s-%s-UI-300K-C100-gb%s-%s%s.ret' % (resFolder,corpus,model,b,modelc,coefficient)
    return result

def getResFileName (key,coefficient):
    # Get File Name Given CSV
    # CSV Line Format :
    # corpus,indexType,qryFilter,qryCount,model,maxResults,fbTerms,fbDocs,RetrievalCoefficient

    # File Name : WA-BM25-UI-50-C1000-RM3-fbdocs05-fbterms10-b0.3.res
    # beta File Name AQ-BM25-UI-50-C1000-AX-fbdocs20-fbterms30-b0.75-beta0.25.res

    # CO-BM25-UI-300K-C100-AX-fbdocs25-fbterms30-b0.75.res
    global resFolder
    parts = key.split(',')
    fbdocs = parts[7]
    fbterms = parts[6]
    exp = parts[2]
    corpus = parts[0][:2].upper()
    if fbdocs == '5':
        fbdocs = '05'
    if fbterms == '5':
        fbterms = '05'
    if (fbdocs == '0'):
        # WA-BM25-BI-50-C1000-b0.1.res
        index = parts[1][0].upper() + 'I'
        qry = parts[3]
        model = parts[4]
        c = 'C' + parts[5]
        if (coefficient == '1' or coefficient == '5'):
            coefficient += '.0'
        coefficient = gen.getModelCoefficient(model) + coefficient
        result = '-'.join([corpus,model,index,qry,c,coefficient])
    else :
        # WA-BM25-UI-50-C1000-RM3-fbdocs05-fbterms10-b0.3.res
        beta = parts[3]
        if beta == '0.5':
            beta += '0'
        model = parts[4]
        modelc = gen.getModelCoefficient(model)
        result =  '%s\%s-%s-UI-300K-C100-%s-fbdocs%s-fbterms%s-%s%s-beta%s.res' % \
                  (resFolder,corpus,model,exp,fbdocs,fbterms,modelc,coefficient,beta)
        # result = '%s\%s-%s-UI-300K-C100-%s-fbdocs%s-fbterms%s-%s%s.res' % \
        #          (resFolder, corpus, model, exp, fbdocs, fbterms, modelc, coefficient)
    return result


def iterateByCSV(csvPath):
    # Iterate Given CSV file and extract CWL Values then add results to the CSV File
    # CSV Line Format :
    # corpus,indexType,qryFilter,qryCount,model,maxResults,fbTerms,fbDocs,RetrievalCoefficient
    # RetrievabilityB , G , ZeroRValuesCount , rSum

    csvLines = []
    update = False
    f = open(csvPath,'r')
    # Add Header
    csvLines.append(f.readline())
    lineCtr = 0

    # Iterate CSV Lines
    for line in f:
        lineCtr += 1
        # Filter Lines
        if requiredLine(line , lineCtr) :
            # print(lineCtr,line)
            # continue
            parts = line.rsplit(',',5)
            key = parts[0]
            b = parts[1]
            coefficient = parts[2]
            c = 100
            resFile = getResFileName(key,coefficient)
            # outFile = getRetFile (key,b,coefficient)
            outFile = ''
            [G , zeroCtr , rSum] = rc.calculate(resFile , b , c , outFile)
            update = True
            line = ','.join([key,
                                b,
                                coefficient,
                                str(G),
                                str(zeroCtr),
                                str(rSum)]) + '\n'
            print ('Updated Line ' + str(lineCtr) + ' : ' + line.replace('\n','') )
        csvLines.append(line)
    if (update):
        f = open(csvPath,'w')
        for line in csvLines:
            f.write(line)
        f.close()
        print ('File ' + csvPath + ' is done')

def getCSVLine(file):
    # Sample File Name
    # AQ-BM25-UI-300K-C100-RM3-fbdocs05-fbterms10-b0.0.res
    # CSV Line Format :
    # corpus,indexType,qryFilter,qryCount,model,maxResults,fbTerms,fbDocs,RetrievalCoefficient
    # RetrievabilityB , G , ZeroRValuesCount , rSum

    switcher = {
        'A' : 'Aquaint',
        'C': 'Core17',
        'W':'WAPO'
    }
    parts = file.rsplit('-', 5)
    corpus = switcher.get(parts[0][0].upper())
    expansion = parts[1]
    fbdocs = parts[2].replace('fbdocs', '')
    fbterms = parts[3].replace('fbterms', '')
    model = parts[0].split('-',2)[1]
    modelc = gen.getModelCoefficient(model)
    coefficient = parts[4].replace(modelc, '')
    beta = parts[5].replace('beta','').replace('.res', '')
    if (coefficient == '0.0' or coefficient == '1.0'):
        coefficient = coefficient[0]
    prefix = 'UnigramIndex,%s,%s,%s,100' % (expansion ,beta, model)
    result = ','.join([corpus, prefix, fbterms, fbdocs,'bVal',coefficient]) + ','
    for i in range(2):
        result += ' ,'
    result += ' '
    result = result.replace('bVal','0',1) + '\n' + result.replace('bVal','0.5',1)
    return result

def appendToCSV(csvPath,resFolder):
    files = os.listdir(resFolder)
    f = open(csvPath,'a')
    lineCtr = 0
    for file in files:
        if os.path.isfile(resFolder + '/' + file):
            line = getCSVLine(file)
            if (requiredLine(line,0)):
                lineCtr += 1
                print(lineCtr  , line)
                f.write(line + '\n')
    f.close()

def requiredLine(line , lineCtr):
    # CSV Line Format :
    # corpus,indexType,qryExpansion,qryCount,model,maxResults,fbTerms,fbDocs,RetrievalCoefficient
    # RetrievabilityB , G , ZeroRValuesCount , rSum
    # WAPO,UnigramIndex,combinedQuery,50,BM25,1000,5,10,0,0,0,0,0.512,0.733,0.783,0.755
    parts = line.split(',')
    # corpus = line[0].upper()
    # fbterms = float(parts[6])
    # fbdocs = parts[7]
    # model = parts[4]
    G = parts[10]
    # exp = parts[2]
    result =  G == ''
    return result

def main():
    global resFolder
    dir = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\2nd Experiment - RM3'
    csvPath = dir + '\CSV\Ex2Ret.csv'
    # resFolder = dir + '\AllRes\AXD\Bias Measurement'
    resFolder = r'D:\Backup 16-12-2020\2nd Experiment - RM3\AllRes\Beta FbDocs20 & FbTerms30\Ret'

    # appendToCSV(csvPath,resFolder)
    iterateByCSV(csvPath)

if __name__ == '__main__':
    main()
