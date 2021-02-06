from src.classes.general import General as gen
from src.classes.CWL import cwlEval as cwl
from src.classes.trec import TrecClass as trec
import os

def getCWLInfo(resFile, gainFile):
    result = cwl.getMetricsValues(resFile,gainFile)
    # [Map, NDCG, P10, R4, R6, R8]
    return result


def getResFolder(coefficient , corpus):
    # cFolder = gen.getCorpus(corpus).upper()

    # if (fbdocs == '0'):
    #     # switcher = {
    #     #     'A': 'AQUAINT 25-02-2020',
    #     #     'C': 'CORE17 25-02-2020',
    #     #     'W': 'WAPO 26-02-2020'
    #     # }
    #     # cFolder = switcher.get(corpus)
    #     folderName = '/Desktop/AllRes'
    #     windowsFolder = r'C:/Users/kkb19103' + folderName
    #     ubuntuFolder = '~' + folderName
    # else:
        # folderName = '/out/RM3/' + cFolder
        # folderName = '/out/NewSet'
    # folderName = corpus
    ubuntuFolder = '~/betaExPer/Per'
    # ubuntuFolder = '~/axEx2Per'
    # if coefficient in '0.75-310-5.1'.split('-') :
    #     ubuntuFolder+= '/2nd\ Stage'

    return ubuntuFolder

def getFileName (csvLine):
    # Get File Name Given CSV
    # CSV Line Format :
    # corpus,indexType,qryFilter,qryCount,model,maxResults,fbTerms,fbDocs,RetrievalCoefficient
    # Trec-MAP,Trec-Bref,Trec-P10,CWL-MAP,CWL-P10,CWL-RBP0.6,CWL-RBP0.8
    # File Name : WA-BM25-UI-50-C1000-RM3-fbdocs05-fbterms10-b0.3.res

    result=''
    parts = csvLine.split(',',9)

    corpus = parts[0][:2].upper()
    expansion = parts[2]
    fbterms = parts[6]
    fbdocs = parts[7]

    if (fbdocs == '0'):
        # WA-BM25-BI-50-C1000-b0.1.res
        index = parts[1][0].upper() + 'I'
        qry = parts[3]
        model = parts[4]
        c = 'C' + parts[5]
        coefficient =  parts[8]
        if (coefficient == '1' or coefficient == '5'):
            coefficient += '.0'
        coefficient = gen.getModelCoefficient(model) + coefficient
        result = '-'.join([corpus,model,index,qry,c,coefficient])
    else :
        # AQ-BM25-UI-50-C1000-AX-fbdocs20-fbterms30-b0.75-beta0.25.res

        beta = parts[3]
        if (beta == '0.5'):
            beta+='0'
        model = parts[4]
        modelc = gen.getModelCoefficient(model)
        coefficient = parts[8].replace(modelc, '').replace('.res', '')
        if (model == 'BM25' and  (coefficient == '0' or coefficient == '1')):
            coefficient += '.0'
        result =  '%s-%s-UI-50-C1000-%s-fbdocs%s-fbterms%s-%s%s-beta%s' % (corpus,model,expansion, fbdocs,fbterms,modelc,coefficient,beta)
    return result + '.res'

def getInputSources (csvLine):
    parts = csvLine.split(',', 9)
    coefficient = parts[8]
    corpus = parts[0].upper()
    ubuntuResFolder = getResFolder(coefficient,corpus)
    corpus = corpus[0].upper()
    switcher = {
        'W': 'qrels.core18.txt',
        'C': '307-690.qrel',
        'A': 'Aquaint-AnseriniQrel.qrel'
    }
    gainFile = switcher.get(corpus, '')
    fName = getFileName(csvLine)
    ubuntuResFile = ubuntuResFolder + '/' + fName
    ubuntuGainFile = '~/trecEval/Qrels/' + gainFile
    return [ubuntuResFile ,  ubuntuGainFile ]

def iterateByCSV(csvPath):
    # Iterate Given CSV file and extract CWL Values then add results to the CSV File
    # CSV Line Format :
    # corpus,indexType,qryExpansion,qryCount,model,maxResults,fbTerms,fbDocs,RetrievalCoefficient,
    # Trec-MAP,Trec-Bref,Trec-P10,CWL-MAP,CWL-NDCG,CWL-P10,CWL-RBP0.6,CWL-RBP0.4,CWL-RBP0.8

    csvLines = []
    update = False
    f = open(csvPath,'r')
    csvLines.append(f.readline())
    lineCtr = 0

    # Iterate CSV Lines
    for line in f:
        lineCtr += 1
        # Filter Lines
        if requiredLine(line , lineCtr) :
            [ubuntuResFile,ubuntuGainFile] = getInputSources(line)
            # print(getFileName(line) , os.path.exists(windowsResFile) )
            # continue
            print('Updating' , line)
            parts = line.rsplit(',',9)
            key = parts[0]
            # CWL values [Map, NDCG, P10, R4, R6, R8]
            cwlValues = getCWLInfo(ubuntuResFile,ubuntuGainFile)
            # [trecMap, trecBPref, trecP10]
            trecValues = trec.getTrecData(ubuntuResFile, ubuntuGainFile)
            update = True
            joinResults = [str(i) for i in trecValues + cwlValues]
            line = key + ',' + ','.join(joinResults) + '\n'
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
    # AQ-BM25-UI-0.4-C1000-RM3-fbdocs05-fbterms10-b0.0.res
    # CSV Line Format :
    # Ex1
    # corpus,indexType,qryFilter,qryCount,model,maxResults,fbTerms,fbDocs,RetrievalCoefficient
    # Trec-MAP,Trec-Bref,Trec-P10,CWL-MAP,CWL-P10,CWL-RBP0.6,CWL-RBP0.8
    # Ex2 :
    # corpus,indexType,qryExpansion,beta,model,maxResults,fbTerms,fbDocs,RetrievalCoefficient,
    # Trec-MAP,Trec-Bref,Trec-P10,CWL-MAP,CWL-NDCG,CWL-P10,CWL-RBP0.6,CWL-RBP0.4,CWL-RBP0.8

    parts = file.rsplit('-', 5)
    corpus = gen.getCorpus(parts[0][0])
    expansion = parts[1]
    fbdocs = parts[2].replace('fbdocs', '')
    fbterms = parts[3].replace('fbterms', '')
    model = parts[0].split('-',2)[1]
    modelc = gen.getModelCoefficient(model)
    coefficient = parts[4].replace(modelc, '')
    beta = parts[5].replace('beta','').replace('.res','')
    if (coefficient == '0.0' or coefficient == '1.0'):
        coefficient = coefficient[0]
    prefix = 'UnigramIndex,%s,%s,%s,1000' % (expansion ,beta, model)
    result = ','.join([corpus, prefix, fbterms, fbdocs, coefficient]) + ','
    for i in range(8):
        result += ' ,'
    result += ' '
    return result

def appendToCSV(csvPath,resFolder):
    files = os.listdir(resFolder)
    f = open(csvPath,'a')
    ctr = 0
    for file in files:
        item = resFolder + '/' + file
        if os.path.isfile(item) and file.endswith('res'):
            line = getCSVLine(file)
            if requiredLine(line,ctr):
                ctr += 1
                print(str(ctr) , line)
            # f.write(line + '\n')
        # elif os.path.isdir(item):
        #     appendToCSV(csvPath, item)
    f.close()

def requiredLine(line , lineCtr):
    # CSV Line Format :
    # corpus,indexType,qryFilter,qryCount,model,maxResults,fbTerms,fbDocs,RetrievalCoefficient
    # Trec-MAP,Trec-Bref,Trec-P10,CWL-MAP,CWL-P10,CWL-RBP0.6,CWL-RBP0.8
    # WAPO,UnigramIndex,AX,50,BM25,1000,5,10,0,0,0,0,0.512,0.733,0.783,0.755
    parts = line.split(',')
    exp = parts[2]
    beta =  parts[3]
    beta = float(beta)
    # map = parts[10]
    # corpus = line[0].upper()
    # model = parts[4]
    fbterms = int(parts[6])
    # fbdocs = int(parts[7])
    # cwlMap = parts[12]
    # coefficient = float(parts[8])
    # exp = parts[2]
    # return map == ' '
    result = fbterms in [10,20] and ((exp == 'AX' and beta != 0.4) or (exp == 'RM3' and beta != 0.5))
    return result
def main():
    csvPath = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\2nd Experiment - RM3\CSV\Ex2Per.csv'
    # AQUAINT - CORE17 - WAPO
    # resFolder = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\2nd Experiment - RM3\AllRes\AXD\Performance Measurement'
    resFolder = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\2nd Experiment - RM3\AllRes\Beta FbDocs20 & FbTerms30\Per'
    # appendToCSV(csvPath,resFolder)
    iterateByCSV(csvPath)

if __name__ == '__main__':
    main()
