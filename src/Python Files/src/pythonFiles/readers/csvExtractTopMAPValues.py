'''
Sort Csv File By

'''


import pandas as pd
import src.pythonFiles.plotters.csvPlotterGeneral as gen

# Import PyDrive and associated libraries.
# This only needs to be done once per notebook.
# from pydrive.auth import GoogleAuth
# from pydrive.drive import GoogleDrive
# from google.colab import auth
# from oauth2client.client import GoogleCredentials

# Import General
# drivePath = '/content/gdrive'
# modPath = '/My Drive/Colab Notebooks'
# from google.colab import drive
# drive.mount (drivePath )
# import sys
# sys.path.append(drivePath + modPath)
# import csvPlotterGeneral as gen

def final_sort(df,fldName):
  finalDf = df.groupby([fldName]).count()
  finalDf = finalDf.reset_index()
  finalDf = finalDf.iloc[:,range(2)].rename(columns = {'corpus':'count'})
  finalDf = finalDf.sort_values('count',ascending=False)
  print(finalDf)

def first_stage_sort(corpus,qryExpansion , best):
  gen.initializeGlobals('W', 2, '')
  # df = gen.readWindowsFile('per')
  dictCriteria = {
    'corpus':corpus ,
    'model':'BM25' ,
    'qryExpansion':qryExpansion,
    'RetrievalCoefficient-!': 0.75,
    'fbTerms-isin': range(10,40,10)
  }
  # criteria = (df['corpus'] == corpus) & (df['model'] == 'BM25') & (df['qryExpansion'] == qryExpansion) & \
  #           (df['RetrievalCoefficient'] != 0.75) & ((df['fbTerms'] == 10) | (df['fbTerms'] == 20) | (df['fbTerms'] == 30 ))
  df = gen.getDataFrame(dictCriteria,gen.trecMapIndex)
  df = df.iloc[:, [0, 4, 6, 7,8, 9]].sort_values('Trec-MAP', ascending=False)
  df = df.head(int(best))
  print('Corpus : ', corpus)
  print(df, '\n')
  return df

def sort_csv(corpus,qryExpansion , best):
  gen.initializeGlobals('W',2,'')
  df = gen.readWindowsFile('per')
  criteria = (df['corpus'] == corpus) & (df['model'] == 'BM25') & (df['qryExpansion'] == qryExpansion)
  df = df[criteria].iloc[:,[0,4,6,7,9]].sort_values('Trec-MAP',ascending=False)
  df = df.head(int(best))
  print ('Corpus : ' , corpus)
  print(df , '\n')
  return df

# *** Start Colab Form ***
# @title Best Performance
#@markdown Get The Best 5 Trec_Eval Values based on input criteria
Corpus = "Aquaint"  # @param ["Aquaint", "Core17", "WAPO" , "All"]
Model = "BM25"  # @param ["BM25", "LMD", "PL2"]
QryExpansion = "RM3"  # @param ["AX","RM3"]
best = "5"  # @param ["5", "6", "7" , "8"]

# *** End Colab Form ***

if __name__ == '__main__':
  # global dfTotalCount
  allDf = pd.DataFrame()

  print ('Query Expansion : ' + QryExpansion)
  print('The Best ' + best  + ' trec_eval values : ')

  for Corpus in 'Aquaint,Core17,WAPO'.split(','):
    df = first_stage_sort(Corpus,QryExpansion , best)
    allDf = pd.concat([allDf,df])
  print('The Final Feed Back Terms : ')
  df = final_sort(allDf,'fbTerms')
  print('The Final Feed Back Documents : ')
  df = final_sort(allDf, 'fbDocs')