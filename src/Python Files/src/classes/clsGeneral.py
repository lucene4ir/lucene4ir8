# from src.classes.clsGeneral import General as gen
class General:
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