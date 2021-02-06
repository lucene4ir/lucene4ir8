# !/usr/bin/env python
import subprocess
import pandas as pd
import io

def executeBash(resFile,gainFile):
    # python3 ~/cwlEval/cwl-eval cwl/tests/qrel_file cwl/tests/result_file -m ~/cwlEval/MyMetrics_file
    cmd = 'python3 ~/cwlEval/cwl-eval %s %s -m ~/cwlEval/MyMetrics_file' % (gainFile,resFile)
  #  bashCmd = 'bash -c \'%s\' ' % cmd
    bashCmd = 'bash -c \"%s\"' % cmd
    result = subprocess.getoutput(bashCmd)
    return result

def getMetricsValues (resFile,gainFile):
    # CWL
    # [Map,NDCG, P10,R4, R6, R8]
    result = executeBash(resFile,gainFile)
    fileData = io.StringIO(result)
    df = pd.read_csv(fileData,delimiter='\t',names=['Topic','Metric','EU','ETU','EC','ETC','ED'])
    df = df.groupby('Metric')['EU'].agg(['mean']).round(decimals=3)
    result = []
    for i in range(6):
        result.append(df.iat[i,0])
    # [Map,NDCG, P10,R4, R6, R8]
    return result

def displayCWl(resFile,gainFile):
    result = executeBash(resFile,gainFile)
    print(result)