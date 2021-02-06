'''
Given CSV File :
For Each Line
Create Bash File Based on line
Run it
Extract The results to regenerate
'''

import subprocess
import src.classes.CWL as cwl
import src.classes.trec as trec
import src.classes.general as gen

def processLine(line):
# 'corpus,indexType,qryExpansion,beta,model,maxResults,fbTerms,fbDocs,RetrievalCoefficient,
# Trec-MAP,Trec-Bref,Trec-P10,Trec-NDCG,CWL-MAP,CWL-NDCG,CWL-P10,CWL-RBP0.4,CWL-RBP0.6,CWL-RBP0.8'
    parts = line.split(',')
    # [corpus , exp , beta , model , fbTerms , docs , coefficient] = [parts[0]] + parts[2:5] + parts[6:9]
    return [parts[2] , [parts[0]] + parts[2:5] + parts[6:9]]

def createBash(resList):
    [corpus, exp, beta, model, terms, docs, coefficient] = resList
    bashfolder = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\LUCENE\anserini-master'
    originalBash = bashfolder + '\dumPerBash.sh'
    newBash = bashfolder + '\dumBash.sh'
    # copyfile(originalBash,newBash)
    f = open(originalBash,'r')
    lines = f.read()
    f.close()
    '''
    corpusShort=core18
    corpus=WAPO
    gainFile=Aquaint
    
    ---------------------------
     'W': 'qrels.core18.txt',
     'C': '307-690.qrel',
      'A': 'Aquaint-AnseriniQrel.qrel'
    ---------------------------
    -spl -spl.c $c / -bm25 -b $b -k1 1.2 / -qld -mu $mu
     -rm3 -rm3.fbTerms $fbTerms -rm3.fbDocs $fbDocs -rm3.originalQueryWeight $beta /
     -axiom -axiom.n $fbDocs -axiom.top $fbTerms -axiom.beta $beta -axiom.deterministic -rerankCutoff 20
     ------------------------------------------
     ~/trec_eval/trec_eval -m ndcg ~/trec_eval/Qrels/$gainFile out/dumRes.res > ndcg.txt
    '''
    init = corpus[0]
    if (init == 'A'):
        corpusShort='robust05'
        gainFile = 'Aquaint-AnseriniQrel.qrel'
    elif (init == 'C'):
        corpusShort = 'core17'
        gainFile = '307-690.qrel'
    else:
        corpusShort = 'core18'
        gainFile = 'qrels.core18.txt'

    parameters = 'corpusShort=%s\ncorpus=%s' % (corpusShort,corpus)

    # -spl -spl.c $c / -bm25 -b $b -k1 1.2 / -qld -mu $mu
    init = model[0]
    if (init == 'B'):
        modelLine= '-bm25 -b %s -k1 1.2' % (coefficient)
    elif (init == 'L'):
        modelLine = '-qld -mu ' + coefficient
    else:
        modelLine = '-spl -spl.c ' + coefficient

    # -rm3 -rm3.fbTerms $fbTerms -rm3.fbDocs $fbDocs -rm3.originalQueryWeight $beta /
    # -axiom -axiom.n $fbDocs -axiom.top $fbTerms -axiom.beta $beta -axiom.deterministic -rerankCutoff 20
    # if (exp == 'AX'):
    #     expLine = '-axiom -axiom.n %s -axiom.top %s -axiom.beta %s -axiom.deterministic -rerankCutoff 20' % \
    #               (docs,terms,beta)
    # else:
    #     expLine = '-rm3 -rm3.fbDocs %s -rm3.fbTerms %s -rm3.originalQueryWeight %s' % \
    #               (docs, terms, beta)

   # trecLine = '~/trec_eval/trec_eval -m ndcg ~/trec_eval/Qrels/%s out/dumRes.res > ndcg.txt' % (gainFile)
    lines = lines.replace('#params',parameters)
    lines = lines.replace('#modelLine' , modelLine)
    # lines = lines.replace('#expLine' , expLine)
    # lines = lines.replace('#gainfile',gainFile)
    # lines = lines.replace('\\\n','').replace(r'\r','')
    f = open(newBash,'w')
    f.write(lines)
    f.close()
    return gainFile

def runBash():
    cmd = r"cd ~/Anserini8 && cat dumBash.sh | tr -d '\r' > dumBash1.sh && ./dumBash1.sh"
    bashCmd = 'bash -c \"%s\" ' % cmd
    result = subprocess.getoutput(bashCmd)
    return result

def extractData(gainFile):
    path = '~/Anserini8/out/dumRes.res'
    gainFile = '~/trec_eval/Qrels/' + gainFile
    # result = cwl.cwlEval.getMetricsValues(path,gainFile)
    result = trec.TrecClass.getTrecData(path,gainFile)
    # [Map,NDCG, P10,R4, R6, R8]
    return result[3]
    # parts = result.split('ndcg all ')
    # ndcgval = parts[1]
    # return ndcgval

def requiredLine(exp):
    return exp == 'Baseline'
def iterateCSV():
  csvFile = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\2nd Experiment - RM3\CSV\Ex2Per.csv'
  outLines = []
  f = open(csvFile,'r')
  line = f.readline()
  outLines.append(line)
  for line in f:
      print("Begin : " + line)
      exp , reslist = processLine(line)
      if (requiredLine(exp)):
          gainFile = createBash(reslist)
          runBash()
          ndcg = extractData(gainFile)
          # ndcg = getDcg()
          # line = line.replace('nd',ndcg)
          line = line.replace('nd', str(ndcg))
          # line = line.replace('r4', str(r4))
          outLines.append(line)
      else:
          outLines.append(line)
      print("Finished : " + line)

  csvFile = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\2nd Experiment - RM3\CSV\Ex2PerNew.csv'
  f = open(csvFile,'w')
  for line in outLines:
    f.write(line)
  f.close()

def extractLine():
   # Aquaint UnigramIndex AX 0.25 BM25 1000	30 20 0.75 0.2432 0.2774 0.454 0.5237 0.486	0.701 0.688	0.738 0.72 0.691

   corpus = gen.General.getCorpus('c')
   exp = 'AX'
   beta = 0.25
   terms = 10
   '%s,UnigramIndex,%s,%s,BM25,1000,%s,20,0.75' % (corpus,exp,beta,terms)

if __name__ == '__main__':
    # iterateCSV()
    extractLine()