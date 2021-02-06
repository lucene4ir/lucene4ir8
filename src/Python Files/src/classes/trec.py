import subprocess

def executeBash(resFile,gainFile):
    measures = '-m map -m bpref -m P.10 -m ndcg'
    cmd = '~/trec_eval/trec_eval %s %s %s' % (measures ,gainFile,resFile)
# ~/Anserini/eval/trec_eval.9.0.4/trec_eval -m map -m P.30 gainFile resFile
    bashCmd = 'bash -c \"%s\" ' % cmd
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


def getTrecData (resFile , gainFile):
    # Given a path af trec File Return [map - BPref - P10 - NDCG]
    f = executeBash(resFile,gainFile)
    result = []
    lines = f.split('\n')
    # map , bpref , P.10 , ndcg'
    for line in lines:
        value = line.split('\t')[2]
        result.append(value)
    return result