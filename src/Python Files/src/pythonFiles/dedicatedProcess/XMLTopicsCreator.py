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

import pandas as pd
import csv

def generateXMLTopics(sourceFile , destFile):
    if (isinstance(sourceFile,pd.DataFrame)):
        df = sourceFile
    else:
        df = pd.read_csv(sourceFile,sep='\t')
    df['XML']=    '<top>\n' + \
                  '<num> Number:'  + \
                   df.iloc[:,0].map(str) + \
                   '</num>\n' + \
                  '<title>\n' + \
                   df.iloc[:,1].map(str) + \
                  '\n</title>\n' + \
                  '<desc>\n\n</desc>\n<narr>\n\n</narr>\n</top>\n'

    df = df['XML']
    df.to_csv(destFile,index=False,header=False , quoting=csv.QUOTE_NONE , escapechar=' ')
    print('Generating XML Queries is Done')




if __name__ == '__main__':
    path = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\LUCENE\anserini\out'
    sourceFile = path + '\WA-BaseQueries-100K.qry'
    destFile = path + '\WA-BaseQueries-100KXML.qry'

    generateXMLTopics(sourceFile,destFile)