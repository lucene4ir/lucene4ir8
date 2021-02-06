import src.pythonFiles.dedicatedProcess.XMLTopicsCreator as xml
import src.classes.trec  as trec
import src.classes.bash as sh
import src.classes.general as gen

def getFile (fName):
    folder = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\LUCENE\anserini\revertedIndex\\'
    bashFolder = '~/anserini/bash/'
    bash = fName.endswith('sh')
    if (bash):
        result = bashFolder + fName
    else:
        result = folder + fName
    return result

def getTermsDictionary(path,sep):
    header = path != 'WAPO50.qry'
    path = getFile(path)
    f = open(path,'r',encoding='utf-8')
    if header :
        f.readline()
    termDict = {}
    for line in f:
        parts = line.replace('\n','').split(sep)
        termDict[parts[0]] = parts[1]
    return termDict

def evaluateResults(resFile,gainFile):
    # Use trec method in Eval Class to evaluate The Final Results File
    return  trec.getTrecData(resFile,gainFile)

def processThirdRun (path):
    sh.runBashFile(path) # Retrieve Results From ExpandedQuery File
    print('Third Stage : Final Run with Expanded terms is Done Successfully')

def getTermList (qry , originalQryWeight , fbTerms , termListDict ):
    # Extract (given fbTerms count) terms separated by space from input term List Dictionary
    # By the following steps :
    # 1- if given OriginalQryWeight > 0 add OriginalQryWeight to matching words between given
    # qry and termListDict
    # 2- If TermList is changed - re-sort it by new scores
    # 3- Extract terms from termList Dictionary , concatenate them in termList  and output termList

    changed = False
    if (originalQryWeight > 0):
        for word in qry.split():
            if (word in termListDict):
                termListDict[word] += originalQryWeight
                changed = True
    termList = ""
    i = 0
    if (changed):
        temp = sorted(termListDict.items(), key=lambda x: x[1], reverse=True)
        for word in temp:
            termList += word[0] + ' '
            i += 1
            if (i == fbTerms):
                break
    else:
        for word in termListDict.keys():
            termList += word + ' '
            i += 1
            if (i == fbTerms):
                break
    return termList

def processSecondRun(resFile , outFile , bashFile , baseQryFile, originalQryWeight,  fbTerms):
    # 2- Implement revert query and generate Expanded Terms Query File
    # Given ( result file from Revert Query path - ExpandedTerms outFile path
    # bashFile path ' Used to run Lucene Retrieval from Reverted Index - fbTerms )
    sh.runBashFile(bashFile)
    f = open(resFile,'r')
    outf = open(outFile,'w')
    outLine = 'qryID\tqry\n'
    outf.write(outLine)
    termListDict = {}
    prevQry = -1
    qryDict = getTermsDictionary('WAPO50.qry',',')
    basetermDict = getTermsDictionary(baseQryFile,'\t')
    for line in f:
        parts = line.split()
        qryid = parts[0]
        termid = parts[2]
        score = float(parts[4])
        if (prevQry == -1):
            prevQry = qryid
            maxScore = score
        if (prevQry == qryid):
            term = basetermDict[termid]
            termListDict[term] = score / maxScore
        else:
            qry = qryDict[prevQry].lower()
            termList = getTermList(qry,originalQryWeight,fbTerms,termListDict)
            outLine = prevQry + '\t' + qry + ' ' + termList + '\n'
            outf.write(outLine)
            termListDict.clear()
            maxScore = score
            term = basetermDict[termid]
            termListDict[term] = 1
            prevQry = qryid
    qry = qryDict[prevQry].lower()
    termList = getTermList(qry, originalQryWeight, fbTerms, termListDict)
    outLine = qryid + '\t' + qry + ' ' + termList + '\n'
    outf.write(outLine)
    qryDict = None
    basetermDict = None
    f.close()
    outf.close()
    xml.generateXMLTopics(outFile,outFile.replace('Terms','TermsXML'))
    print('Second Stage : Expanded Terms Created Successfully')

def processFirstRun (resFile,outFile, fbDocs):
    # Extract Document ids from Initial Res File (Standard queries) and generate base Query File
    sep = '\t'
    resF = open(resFile,'r')
    baseqryF = open(outFile,'w')
    outLine = 'qryID%sqry\n' % sep
    baseqryF.write(outLine)
    prevQry = '0'
    docidList = ""
    for line in resF:
        parts = line.split()
        qryID = parts[0]
        docid = parts[2].replace('-','')
        rank = int(parts[3])
        if (prevQry == '0'):
            prevQry = qryID
        if (prevQry == qryID and rank <= fbDocs ):
            docidList += docid + ' '
        elif(prevQry != qryID):
            outLine = '%s%s%s\n' % (prevQry, sep, docidList)
            baseqryF.write(outLine)
            docidList = docid + ' '
            prevQry = qryID
    outLine = '%s%s%s\n' % (prevQry, sep, docidList)
    baseqryF.write(outLine)
    baseqryF.close()
    resF.close()
    xml.generateXMLTopics(outFile,outFile.replace('Qry','QryXML'))
    print('First Stage : Document Queries Created Successfully')

def runExperiment(fbDocs,fbTerms , bias):
    print('Start Running with expansion fbDocs = %s , fbTerms = %s' % (fbDocs, fbTerms))
    # Process First Run
    if bias :
        resFile = 'BaseScore'
    else:
        resFile = 'firstStage.res'
    resFile = getFile(resFile)
    outFile = getFile('revertQry.qry')
    processFirstRun(resFile , outFile , fbDocs)
    resFile = getFile('SecondStage.res')
    outFile = getFile('expandedTerms.qry')
    bashFile = getFile('rindexSecondRun.sh')
    baseQryFile = 'WA-BaseQueries-Df-10.qry'
    originalQryWeight = 0
    processSecondRun(resFile, outFile, bashFile,baseQryFile,originalQryWeight, fbTerms)
    bashFile = getFile('rindexThirdRun.sh')
    processThirdRun(bashFile)
    resFile = '~/anserini/revertedIndex/FinalRun.res'
    gainFile = gen.getGainFile('w')
    result = evaluateResults(resFile,gainFile)
    # resFile = 'FBDocs = %d , FbTerms = %d - Results [map , bpref , P.10 , ndcg] [%s]\n' % (fbDocs,fbTerms, ','.join(result) )
    resFile = '%d %d [%s]\n' % (fbDocs,fbTerms, ','.join(result) )
    return resFile

def main():
    terms = 5
    docs = 10
    outLine = ""
    # for docs in range(5,35,5):
    outLine += runExperiment(docs,terms,False)
    print(outLine)
if __name__ == '__main__':
    main()