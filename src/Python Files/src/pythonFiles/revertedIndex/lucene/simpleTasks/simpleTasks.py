import src.pythonFiles.dedicatedProcess.XMLTopicsCreator as xml

def removeDashes (path):
    f = open(path, 'r')
    f2 = open(path.replace('core', 'coreNew'), 'w')
    outline = f.readline()
    f2.write(outline)
    for line in f:
        parts = line.split()
        parts[2] = parts[2].replace('-', '')
        outline = ' '.join(parts) + '\n'
        f2.write(outline)
    f.close()
    f2.close()
    print('Done')

def extractBaseQueries (inFile , outFile , df):
    f = open(inFile,'r',encoding='utf-8')
    f2 = open(outFile,'w',encoding='utf-8')
    outLine = 'qryID\tqry\n'
    f2.write(outLine)
    seq = 1
    for line in f:
        parts = line.split('\t')
        numDf = int(parts[1])
        if (numDf > df):
            outLine = '%d\t%s\n' % (seq , parts[0])
            f2.write(outLine)
            seq += 1
    f.close()
    f2.close()

def main():
    mainFolder = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\LUCENE\anserini\revertedIndex'
    path = mainFolder + r'\prior\Old\WA-Df-500K.sts'
    df = 15
    outPath = mainFolder +  '\WA-BaseQueries-Df-%d.qry' % df
    extractBaseQueries(path,outPath,15)
    xml.generateXMLTopics(outPath,outPath.replace('-' + str(df),'-%dXML' % df))
    print('Done')

if __name__ == '__main__':
    main()