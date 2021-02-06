# Functions for Reverted Index Construction
import pandas as pd
import src.classes.general as gen
import src.pythonFiles.dedicatedProcess.XMLTopicsCreator as xml
import src.classes.bash as sh

def extractQueries(dfPath , outpath):
    '''
    Extract Queries From Anserini Df Format File
    Input Format
    ----------------
    query   df  totalDocuments  average
    Output
    ----------
    qryID   query
    '''
    df = pd.read_csv(dfPath, sep='\t', names=['baseQry', 'df', 'count', 'avg'])
    df.reset_index(inplace=True)
    df['qryID'] = df.index + 1
    df = df[['qryID','baseQry']]
    df.to_csv(outpath,sep='\t',index=False)
    print ('Step 1 : Creating Base Queries Df is Complete')


def constructRevertedIndex (basePath , scorePath , revertIndexPath,top):
    '''
    Given BaseQueries path + score res file path
    Merge Both and get the reverted index ( top queries for each document )
    in the Following Form :
    docid - Base qry - score
    '''
    print ('Creating Reverted Index')

    f = open(basePath,'r',encoding="utf8")
    line = f.readline()
    # Save all Queries
    qryDict = {}
    for line in f:
        parts = line.replace('\n','').split('\t')
        [qryID , qry] = parts
        qryDict[qryID] = qry

    # Append Res Data [qryID,docid,score]
    f = open(scorePath,'r',encoding="utf8")
    line = f.readline()
    resDict = {}
    sep = '\t'
    for line in f:
        parts = line.split()
        [qryID,docid,score] = parts[::2]
        qry = qryDict[qryID] # Retrieve query From ID (Join)
        if (docid in resDict):
            resDict[docid][qry] = score
        else:
            resDict[docid] = {qry:score}

    # Sort and Extract Top Documents From resDict then output lines
    qryDict = None
    hdr = sep.join(['docid','baseQry','score'])
    lines = [hdr]
    for key in resDict:
        docid = key
        val = resDict[key]
        # Sorting By Score
        val = dict(sorted(val.items(), reverse=True , key=lambda item: item[1]))
        # Extracting head Values
        temp = {}
        for qry in list(val)[:top]:
            score = val[qry]
            temp[qry] = score
            line = sep.join([docid,qry,score])
            print(len(lines) , 'Line Added :' , line)
            lines.append(line)
        resDict[key] = temp

    resDict = None
    val = None
    temp = None
    f =  open(revertIndexPath,'w',encoding="utf8")
    for line in lines:
        f.write(line + '\n')

    # dfBase = pd.read_csv(basePath,sep='\t')
    # dfScore = pd.read_csv(scorePath,sep=' ',names=gen.getResHeader())
    # df = dfScore.merge(dfBase,how='left',on='qryID')
    # dfScore = None
    # dfBase = None
    #df = df[['docid','baseQry','score']]
    #df.sort_values(['docid', 'score'], ascending=False, inplace=True)
    # df = df.groupby('docid').head(top)
    #df.to_csv(revertIndexPath,sep='\t',index=False)
    print('Reverted Index Completed')

def testRevertedIndex(rIndexPath):
    df = pd.read_csv(rIndexPath,sep=' ',names=gen.getResHeader())
    col = 'docid'
    count = df.groupby(col)[col].count()
    print(count)

if __name__ == '__main__':
    folder = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\LUCENE\anserini\revertedIndex'
    # WA-Df-500K.sts - WA-BaseQueries-100K.qry
    basePath = folder +  '\WA-BaseQueries-500K.qry'
    scorePath = folder + r'\baseScore.res'
    revertIndexPath = folder + r'\WA-PL2-500K-rIndex.idx'
    dfPath= folder + r'\WA-Df-500K.sts'
    outpath = folder + r'\WA-BaseQueries-500K.qry'
    shFile = '~/anserini/bash/getBaseRes.sh'
    # extractQueries(dfPath,outpath)
    # xml.generateXMLTopics(outpath,outpath.replace('500K','500KXML'))
    # sh.runBashFile(shFile) # Takes Long Time
    # Run Score
    constructRevertedIndex(basePath,scorePath,revertIndexPath,50)
    # testRevertedIndex(folder + r'\firstStage.res')