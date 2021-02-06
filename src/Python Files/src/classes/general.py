def getCorpus(c):
    switcher = {
        'A': 'AQUAINT',
        'C': 'CORE17',
        'W': 'WAPO'
    }
    return switcher.get(c.upper())

def getModelCoefficient(model):
    switcher = {
        'BM25': 'b',
        'PL2': 'c',
        'LMD': 'mu'
    }
    return switcher.get(model)

def getQryExpansion(c):
    switcher = {
        'A': 'AX',
        'B': 'Baseline',
        'R': 'RM3'
    }
    return switcher.get(c.upper())

def getResHeader():
    return ['qryID','dum','docid','rank','score','tag']

def getGainFile(c):
    switcher = {
        'A': 'Aquaint-AnseriniQrel.qrel',
        'C': '307-690.qrel',
        'W': 'qrels.core18.txt'
    }
    result = '~/trec_eval/Qrels/' + switcher.get(c.upper())
    return result