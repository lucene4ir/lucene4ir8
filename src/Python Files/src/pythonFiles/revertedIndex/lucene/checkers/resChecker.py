import pandas as pd
import src.classes.general as gen


def getQryDict(path):
    f = open(path,'r',encoding='utf-8')
    qryDict = {}
    f.readline()
    for line in f:
        parts = line.replace('\n','').split('\t')
        qryDict[int(parts[0])] = parts[1]
    f.close()
    return qryDict

def checkSequence(path):
    baseFile = 'BaseScore.res'
    qryFile = 'WA-BaseQueries-500K.qry'
    missingFile = 'missingQueries1.qry'
    path += '//' + baseFile
    f = open(path,'r')
    path = path.replace(baseFile,missingFile)
    outf = open(path,'w')
    outline = 'qryID\tqry\n'
    outf.write(outline)
    seq = 1
    path = path.replace(missingFile,qryFile)
    qryDict = getQryDict(path)

    lineCtr = 0
    for line in f:
        qryID = int(line.split()[0])
        if (qryID > seq):
            for i in range(seq,qryID):
                print('Line %s is Missing' % i)
                qry = qryDict[i]
                outline = '%s\t%s\n' % (qryID,qry)
                outf.write(outline)
                lineCtr += 1
            seq = qryID + 1
        elif (qryID == seq):
            seq += 1
    print('Total Missing Lines ' , lineCtr)
    f.close()
    outf.close()
    print('Done')

def displayNumberOfQueries(path):
    hdr = gen.getResHeader()
    df = pd.read_csv(path, ' ', names=hdr)
    df = df.groupby('qryID',as_index=False).min()
    # f = len(df.groupby('qryID'))
    df = None
    # print(f, 'Done')

if __name__ == '__main__':
    path = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\LUCENE\anserini\revertedIndex\prior'
    # displayNumberOfQueries(path)
    checkSequence(path)