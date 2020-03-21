# !/usr/bin/env python
import subprocess
import pandas as pd
import io

def executeBash(resFile,gainFile):
    # python3 ~/cwlEval/cwl-eval cwl/tests/qrel_file cwl/tests/result_file -m ~/cwlEval/MyMetrics_file
    cmd = 'python3 ~/cwlEval/cwl-eval %s %s -m ~/cwlEval/MyMetrics_file' % (gainFile,resFile)
    bashCmd = 'bash -c \'%s\' ' % cmd
    result = subprocess.getoutput(bashCmd)
    return result

class cwlEval ():

    def getUsingUbuntu (resFile,gainFile):
        result = executeBash(resFile,gainFile)
        fileData = io.StringIO(result)
        df = pd.read_csv(fileData,delimiter='\t',names=['Topic','Metric','EU','ETU','EC','ETC','ED'])
        m = df.groupby('Metric')['EU'].agg(['mean']).round(decimals=3)
        [Map, P10, R6, R8] = m.iat[0, 0], m.iat[1, 0], m.iat[2, 0], m.iat[3, 0]
        return [Map, P10, R6, R8]

    def displayCWl(resFile,gainFile):
        result = executeBash(resFile,gainFile)
        print(result)