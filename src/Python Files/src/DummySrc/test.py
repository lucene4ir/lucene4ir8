import pandas as pd
import src.pythonFiles.dedicatedProcess.XMLTopicsCreator as xml
if __name__ == '__main__':
    path = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\LUCENE\anserini\revertedIndex\WAPO50.qry'
    df = pd.read_csv(path,names=['qryID','qry'])
    path = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\LUCENE\anserini\revertedIndex\new.qry'
    xml.generateXMLTopics(df,path)
    print('Done')
