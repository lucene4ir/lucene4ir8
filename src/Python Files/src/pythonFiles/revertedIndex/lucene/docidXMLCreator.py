# General Variables
folder = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\LUCENE\anserini\revertedIndex'
xmlTemplate = ""

def getScoreDistribution(N):
    # Using  Distribution of given N values to 10 groups based on
    # " Simplified Similarity Scoring Using Term Ranks " By Anh and Moffat 2002 page 228
    # Return Dictionary Key = Cumulated Scale Repetition , Value : group value (10 : 1)
    if (N == 1000):
        # results = [1,2,4,8,16,31,63,125,251,499] -
        # cumulated = [1,3,7,15,31,62,125,250,501,1000]
        scores = {
            1:10,
            3:9,
            7:8,
            15:7,
            31:6,
            62:5,
            125:4,
            250:3,
            501:2,
            1000:1
        }
    else:
        groups = 10
        b = pow(1 + N, 1 / groups)
        cumulated = 0
        res = 0
        scores = {}
        for i in range(groups,0,-1):
            if (i == groups):
                raw = b-1
            else:
                raw *= b
            target = res + raw
            currentRound = round(target)
            # Repetition value must be > 0
            if (currentRound > 0):
                cumulated += currentRound
                scores[cumulated] = i
            res = target - currentRound
    return scores

def outputQry (qryid , docids):
    global xmlTemplate
    xmlText = xmlTemplate.replace('#qid', qryid).replace('#docids', docids)
    qryid = "{:06d}".format(int(qryid))
    fileName = '%s\XML\XML%s.xml' % (folder, qryid)
    f = open(fileName, 'w')
    f.write(xmlText)
    f.close()
    print('Doc ' + qryid + ' is Finished')

def processQry (qryid , docidList):
    # Process given document list based on given qryid
    # 1- Calculate Their scores Distribution based on Anh and Moffat
    # 2- Repeat DocIDs based on their Scores
    # Output The Final Format of Reapeated DocIDS
    docidList = docidList.split()
    N = len(docidList)
    scores = getScoreDistribution(N)
    repeatedDocIDList = ""
    removeKey = 0
    for i in range(N):
        for key in scores.keys():
            if (i+1 <= key):
                val = scores[key]
                docid = docidList[i]
                for i in range(val):
                   repeatedDocIDList += ' ' + docid
                break
            else:
                removeKey = key
        if removeKey > 0:
            scores.pop(removeKey)
            removeKey = 0
    docidList = None
    outputQry(qryid,repeatedDocIDList)

def processResFile(resPath):
    f = open(resPath,'r')
    prevQryID = ''
    docidList = ''

    for line in f:
        parts = line.split()
        qryID = parts[0]
        docid = parts[2].replace('-','')
        parts = None
        if (prevQryID == ''):
            prevQryID = qryID
        if (qryID == prevQryID):
            docidList += docid + ' '
        else:
            processQry(prevQryID , docidList)
            docidList = docid
            prevQryID = qryID

    processQry(prevQryID, docidList)
    print('Done')

def readXMLTemplate ():
    global xmlTemplate
    f = open(folder + '\XMLFormat.xml')
    xmlTemplate = f.readlines()
    xmlTemplate = ''.join(xmlTemplate)
    f.close()

def main():
    scorePath =  folder + r'\BaseScore.res'
    readXMLTemplate()
    processResFile(scorePath)

if __name__ == '__main__':
    main()