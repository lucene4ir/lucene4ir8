from src.classes.clsGeneral import General as gen
from src.classes.clsCWL import cwlEval as cwl
from src.classes.clsTrec import TrecClass as trec
import os

def getCWLInfo(resFile, gainFile):
    [Map,P10,R6,R8] = cwl.cwlEval.getUsingUbuntu(resFile,gainFile)
    return [Map,P10,R6,R8]


def getResFolder(fbdocs , corpus):
    cFolder = gen.getCorpus(corpus).upper()

    if (fbdocs == '0'):
        # switcher = {
        #     'A': 'AQUAINT 25-02-2020',
        #     'C': 'CORE17 25-02-2020',
        #     'W': 'WAPO 26-02-2020'
        # }
        # cFolder = switcher.get(corpus)
        folderName = '/Desktop/AllRes'
        windowsFolder = r'C:/Users/kkb19103' + folderName
        ubuntuFolder = '~' + folderName
    else:
        # folderName = '/out/RM3/' + cFolder
        folderName = '/out/NewSet'
        windowsFolder = 'C:/Users/kkb19103/Desktop/My Files 07-08-2019/LUCENE/anserini-master' + folderName
        ubuntuFolder = '~/Anserini8' + folderName

    result = [windowsFolder,ubuntuFolder]
    return result

def getFileName (csvLine):
    # Get File Name Given CSV
    # CSV Line Format :
    # corpus,indexType,qryFilter,qryCount,model,maxResults,fbTerms,fbDocs,RetrievalCoefficient
    # Trec-MAP,Trec-Bref,Trec-P10,CWL-MAP,CWL-P10,CWL-RBP0.6,CWL-RBP0.8
    # File Name : WA-BM25-UI-50-C1000-RM3-fbdocs05-fbterms10-b0.3.res

    result=''
    parts = csvLine.split(',',9)
    fbdocs = parts[7]
    fbterms = parts[6]
    corpus = parts[0][:2].upper()

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
        # WA-BM25-UI-50-C1000-RM3-fbdocs05-fbterms10-b0.3.res

        model = parts[4]
        modelc = gen.getModelCoefficient(model)
        coefficient = parts[8].replace(modelc, '').replace('.res', '')
        # if (coefficient == '0' or coefficient == '1' or coefficient == '5'):
        #     coefficient += '.0'
        # elif coefficient == '0.15':
        #     coefficient += '0'
        result =  '%s-%s-UI-50-C1000-RM3-fbdocs%s-fbterms%s-%s%s' % (corpus,model,fbdocs,fbterms,modelc,coefficient)
    return result + '.res'


def getInputSources (csvLine):

    parts = csvLine.split(',', 8)
    fbdocs = parts[7]
    corpus = parts[0][0].upper()
    [windowsResFolder , ubuntuResFolder] = getResFolder(fbdocs,corpus)
    switcher = {
        'W': 'qrels.core18.txt',
        'C': '307-690.qrel',
        'A': 'Aquaint-AnseriniQrel.qrel'
    }
    gainFile = switcher.get(corpus, '')
    fName = getFileName(csvLine)
    windowsResFile = windowsResFolder + '/' + fName
    ubuntuResFile = ubuntuResFolder + '/' + fName
    windowsGainFile = 'C:/Users/kkb19103/Desktop/My Files 07-08-2019/Applications/Trec_Eval/trec_eval.8.1 Original/Qrels/' \
                        + gainFile
    ubuntuGainFile = '~/trecEval/Qrels/' + gainFile


    return [windowsResFile , windowsGainFile , ubuntuResFile ,  ubuntuGainFile ]

def iterateByCSV(csvPath):
    # Iterate Given CSV file and extract CWL Values then add results to the CSV File
    # CSV Line Format :
    # corpus,indexType,qryFilter,qryCount,model,maxResults,fbTerms,fbDocs,RetrievalCoefficient
    # Trec-MAP,Trec-Bref,Trec-P10,CWL-MAP,CWL-P10,CWL-RBP0.6,CWL-RBP0.8

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
            [windowsResFile,windowsGainFile,ubuntuResFile,ubuntuGainFile] = getInputSources(line)
            # print(getFileName(line) , os.path.exists(windowsResFile) )
            # continue
            parts = line.rsplit(',',7)
            key = parts[0]
            [cwlMap,cwlp10,cwlR6,cwlR8] = getCWLInfo(ubuntuResFile,ubuntuGainFile)
            [trecMap,trecBPref,trecP10] = trec.TrecClass.getTrecData(ubuntuResFile, ubuntuGainFile)
            update = True
            line = ','.join([key,
                                str(trecMap),
                                str(trecBPref),
                                str(trecP10),
                                str(cwlMap),
                                str(cwlp10),
                                str(cwlR6),
                                str(cwlR8)]) + '\n'
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
    # AQ-BM25-UI-50-C1000-RM3-fbdocs05-fbterms10-b0.0.res
    # CSV Line Format :
    # corpus,indexType,qryFilter,qryCount,model,maxResults,fbTerms,fbDocs,RetrievalCoefficient
    # Trec-MAP,Trec-Bref,Trec-P10,CWL-MAP,CWL-P10,CWL-RBP0.6,CWL-RBP0.8

    parts = file.rsplit('-', 3)
    corpus = gen.getCorpus(parts[0][0])
    fbdocs = parts[1].replace('fbdocs', '')
    fbterms = parts[2].replace('fbterms', '')
    model = parts[0].split('-',2)[1]
    modelc = gen.getModelCoefficient(model)
    coefficient = parts[3].replace(modelc, '').replace('.res', '')
    if (coefficient == '0.0' or coefficient == '1.0'):
        coefficient = coefficient[0]
    prefix = 'UnigramIndex,combinedQuery,50,%s,1000' % model
    result = ','.join([corpus, prefix, fbterms, fbdocs, coefficient]) + ','
    for i in range(6):
        result += ' ,'
    result += ' '
    return result


def appendToCSV(csvPath,resFolder):
    files = os.listdir(resFolder)
    f = open(csvPath,'a')
    ctr = 0
    for file in files:
        if os.path.isfile(resFolder + '/' + file):
            line = getCSVLine(file)
            ctr += 1
            # print(str(ctr) , line)
            f.write(line + '\n')
    f.close()



def requiredLine(line , lineCtr):
    # CSV Line Format :
    # corpus,indexType,qryFilter,qryCount,model,maxResults,fbTerms,fbDocs,RetrievalCoefficient
    # Trec-MAP,Trec-Bref,Trec-P10,CWL-MAP,CWL-P10,CWL-RBP0.6,CWL-RBP0.8
    # WAPO,UnigramIndex,combinedQuery,50,BM25,1000,5,10,0,0,0,0,0.512,0.733,0.783,0.755
    parts = line.split(',')
    # corpus = line[0].upper()
    # model = parts[4]
    # fbterms = int(parts[6])
    # fbdocs = int(parts[7])
    cwlMap = parts[12]
    # coefficient = float(parts[8])
    return cwlMap == ' '
    # return True

def main():
    csvPath = r'C:\Users\kkb19103\Desktop\CSV\CSV\per.csv'
    # resFolder = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\LUCENE\anserini-master\out\NewSet'
    # appendToCSV(csvPath,resFolder)
    # iterateByCSV(csvPath)

if __name__ == '__main__':
    main()
