'''
Input Format :
QryID - Qry

Output Format
---------------------------------------
<top>
<num> Number: QryID </num>
<title>
Query</title>
<desc>

</desc>
<narr>

</narr>
</top>

-----------------------------------
'''
def generateXMLTopics(sourceFile , destFile):
    sep = '\t'
    XMLFormat =    '<top>\n' + \
                  '<num> Number: #qryid </num>\n'  + \
                  '<title>\n#query\n</title>\n' + \
                  '<desc>\n\n</desc>\n<narr>\n\n</narr>\n</top>\n'

    en = 'utf-8'
    # fSource = open(sourceFile,'r',encoding=en)
    # fDest = open(destFile,'w',encoding=en)
    fSource = open(sourceFile, 'r')
    fDest = open(destFile, 'w')
    fSource.readline()
    for line in fSource:
        [qryid,qry] = line.replace('\n','').split(sep)
        outLine = XMLFormat.replace('#qryid',qryid).replace('#query',qry)
        fDest.write(outLine)
    fSource.close()
    fDest.close()
    print('Generating XML Queries is Done')

if __name__ == '__main__':
    path = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\LUCENE\anserini\out'
    sourceFile = path + '\WA-BaseQueries-100K.qry'
    destFile = path + '\WA-BaseQueries-100KXML.qry'

    generateXMLTopics(sourceFile,destFile)