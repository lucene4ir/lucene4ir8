import pandas as pd
import src.classes.general as gen
import src.pythonFiles.dedicatedProcess.XMLTopicsCreator as xml
import src.pythonFiles.readers.readSingleResFile as trec
import src.classes.bash as sh

def expandQueries(fbDocs , fbTerms):
    ansFolder = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\LUCENE\anserini'
    rIndexFolder = ansFolder + r'\revertedIndex'
    [qrySource , resFile , rIndexSource ] = \
        ['WAPO50.qry' , 'firstStage.res' , 'WA-PL2-500K-rIndex.idx']

    print('Start Running with expansion fbDocs = %s , fbTerms = %s' % (fbDocs,fbTerms))
    # Process First Run
    path = '~/anserini/bash/rindexFirstRun.sh'
    sh.runBashFile(path)
    # Retrieve Base Queries
    [hdrQryID , hdrInitialQuery , hdrDocID , hdrBaseQry , hdrScore] = \
        ['qryID','initialQry','docid','baseQry','score']
    path = '\\'.join([rIndexFolder,qrySource])
    hdr = [hdrQryID,hdrInitialQuery]
    dfQry = pd.read_csv(path,names=hdr)
    path = '\\'.join([rIndexFolder,resFile])
    hdr = gen.getResHeader()
    dfRes = pd.read_csv(path,' ',names=hdr)
    dfRes = dfRes[[hdrQryID,hdrDocID]].groupby(hdrQryID).head(fbDocs)
    path = '\\'.join([rIndexFolder,rIndexSource])
    dfIndex = pd.read_csv(path,'\t')
    # Combine 3 DataFrames
    # Queries       Res File        reverted Index
    # qryID , qry   qryID , docid   docid , baseQry , score
    df = dfRes.merge(dfQry,'left',hdrQryID).merge(dfIndex,'left',hdrDocID)

    # Reset unnecessary frames
    dfRes = None
    dfIndex = None
    # Drop Duplicated Base Terms
    # df.drop_duplicates(subset=[hdrQryID, hdrBaseQry], inplace=True)
    # Sort df by Qry ID Asc & score descendingly
    df.sort_values(by=[hdrQryID,hdrScore],ascending=[True,False],inplace=True)
    # Extract top Terms
    df = df.groupby(hdrQryID).head(fbTerms)
    # group expanded terms together for each query
    df = df.groupby(hdrQryID,as_index=False).agg({hdrBaseQry: ' '.join})
    # Merge initial queries with expanded terms
    df = dfQry.merge(df,on=hdrQryID)
    dfQry = None
    df['qry'] = df[hdrInitialQuery] + ' ' + df[hdrBaseQry]
    # drop unnecessary fields
    df.drop([hdrInitialQuery,hdrBaseQry],axis=1, inplace=True)
    # Regenerate XML Queries for Anserini rerun
    path = rIndexFolder + r'\expandedTerms.qry'
    xml.generateXMLTopics(df,path)
    print('Expanded Terms Created Successfully')

def runWithExpandedTerms():
    path = '~/anserini/bash/rindexSecondRun.sh'
    sh.runBashFile(path)
    print('Running With Expansion is Completed')

def runExperiment(docs,terms):
    expandQueries(docs, terms)
    runWithExpandedTerms()
    # print Trec Results
    print('\n\n')
    trec.main()

def main():
    terms = 10
    docs = 10
    runExperiment(docs,terms)

if __name__ == '__main__':
    main()