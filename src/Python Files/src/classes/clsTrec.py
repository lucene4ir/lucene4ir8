import os.path as ospath
import os
import subprocess

defaultVal = -999

def executeBash(resFile,gainFile):
    # ~/trecEval/trec_eval ~/trecEval/Qrels/qrels.core18.txt ./result0.1.res > ./trec0.1.trec
    cmd = '~/trecEval/trec_eval %s %s' % (gainFile,resFile)
    bashCmd = 'bash -c \'%s\' ' % cmd
    result = subprocess.getoutput(bashCmd)
    return result

def getGainFile (line):
    c = line[0].upper()
    switcher = {
        'W': 'qrels.core18.txt',
        'C': '307-690.qrel',
        'A': 'Aquaint-AnseriniQrel.qrel'
    }
    result = switcher.get(c,'')
    return result
def getValue(line, caption):
    if line.startswith(caption):  # or line.startswith('P10'):
        parts = line.split('\t')
        val = parts[parts.__len__() - 1]
        result = float(val)
    else:
        result = defaultVal
    return result
class TrecClass() :
    def getTrecData (resFile , gainFile):
        # Given a path af trec File Return [map - BPref - P10]
        [map,bpref,p10] = [defaultVal,defaultVal,defaultVal]
        f = executeBash(resFile,gainFile)
        result = ['','','']
        parts = f.split('\n')
        for line in parts:
           if (map == defaultVal):
                map = getValue (line , 'map')
           elif (bpref==defaultVal):
               bpref = getValue(line,'bpref')
           elif(p10 == defaultVal):
               p10 = getValue(line,'P10')
           else:
               result = [map, bpref, p10]
               break
        return result
    def createBash (resFolder,bashFile):
        if ospath.exists(resFolder):
            files = os.listdir(resFolder)
            lines = []
            for f in files:
                if (f.endswith('.res')):
                    # ~/trecEval/trec_eval ~/trecEval/Qrels/qrels.core18.txt ./result0.1.res > ./trec0.1.trec
                    qrel = getGainFile(f)
                    resFile = './' + f
                    trecFile = resFile.replace('.res','.trec')
                    line = '~/trecEval/trec_eval ~/trecEval/Qrels/%s %s > %s\n' % (qrel,resFile,trecFile)
                    lines.append(line)
            if (len(lines) > 0):
                f = open(bashFile,'w')
                for line in lines:
                    f.write(line)
                f.close()