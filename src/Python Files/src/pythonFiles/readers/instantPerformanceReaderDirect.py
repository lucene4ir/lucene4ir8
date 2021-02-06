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
def createBash(corpus, exp, beta, terms):
    bashfolder = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\LUCENE\anserini'
    originalBash = bashfolder + '\perBash.sh'
    newBash = bashfolder + '\dumBash.sh'
    # copyfile(originalBash,newBash)
    f = open(originalBash,'r')
    lines = f.read()
    f.close()
    init = corpus
    if (init == 'A'):
        corpusShort='robust05'
        gainFile = 'Aquaint-AnseriniQrel.qrel'
    elif (init == 'C'):
        corpusShort = 'core17'
        gainFile = '307-690.qrel'
    else:
        corpusShort = 'core18'
        gainFile = 'qrels.core18.txt'
    corpus = gen.General.getCorpus(init)
    sep = '=%s\n'
    parameters = (sep.join(['corpusShort','corpus' , 'fbTerms' , 'beta','exp']) + sep) % \
                 (corpusShort , corpus , terms , beta,exp)

    lines = lines.replace('#Params',parameters)
    f = open(newBash,'w')
    f.write(lines)
    f.close()
    return gainFile

def runBash():
    cmd = r"cd ~/anserini && cat dumBash.sh | tr -d '\r' > dumBash1.sh && ./dumBash1.sh"
    bashCmd = 'bash -c \"%s\" ' % cmd
    subprocess.getoutput(bashCmd)

def extractData(gainFile):
    path = '~/anserini/out/dum.res'
    gainFile = '~/trec_eval/Qrels/' + gainFile
    # Trec - map , bpref , P.10 , ndcg'
    trecResults = trec.TrecClass.getTrecData(path,gainFile)
    # CWL - [Map,NDCG, P10,R4, R6, R8]
    cwlResults = cwl.cwlEval.getMetricsValues(path,gainFile)
    result = [str(x) for x in trecResults + cwlResults]
    return result

def processLines():
   path = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\2nd Experiment - RM3\CSV\Ex2Per.csv'
   f = open(path,'a')
   for corpus in 'C W'.split():
       for exp in 'AX RM3'.split():
           for terms in range(10,40,10):
               for beta in [0.25 ,  0.5 , 0.75]:
                   line = '%s,UnigramIndex,%s,%s,BM25,1000,%s,20,0.75,' % (gen.General.getCorpus(corpus) , exp, beta, terms)
                   gainFile = createBash(corpus, exp, beta, terms)
                   runBash()
                   results = extractData(gainFile)
                   line += ','.join(results) + "\n"
                   f.write(line)
                   print('Line Processed : ' , line , end='')
   f.close()

def createBaselineBash(corpus):
    bashfolder = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\LUCENE\anserini'
    originalBash = bashfolder + '\perBash.sh'
    newBash = bashfolder + '\dumBash.sh'
    # copyfile(originalBash,newBash)
    f = open(originalBash, 'r')
    lines = f.read()
    f.close()
    init = corpus.upper()
    if (init == 'A'):
        corpusShort = 'robust05'
        gainFile = 'Aquaint-AnseriniQrel.qrel'
    elif (init == 'C'):
        corpusShort = 'core17'
        gainFile = '307-690.qrel'
    else:
        corpusShort = 'core18'
        gainFile = 'qrels.core18.txt'
    corpus = gen.General.getCorpus(init)
    sep = '=%s\n'
    parameters = (sep.join(['corpusShort', 'corpus']) + sep) % \
                 (corpusShort, corpus)

    lines = lines.replace('#Params', parameters)
    f = open(newBash, 'w')
    f.write(lines)
    f.close()
    return gainFile

def processBaseLines():
    path = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\2nd Experiment - RM3\CSV\Ex2Per.csv'
    f = open(path, 'a')
    for corpus in 'c w'.split():
        line = '%s,UnigramIndex,Baseline,0,BM25,1000,0,0,0.75,' % (gen.General.getCorpus(corpus))
        gainFile = createBaselineBash(corpus)
        runBash()
        results = extractData(gainFile)
        line += ','.join(results) + "\n"
        f.write(line)
        print('Line Processed : ', line, end='')

if __name__ == '__main__':
   processBaseLines()
