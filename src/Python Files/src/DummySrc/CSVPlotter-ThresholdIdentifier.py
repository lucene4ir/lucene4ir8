import matplotlib.pyplot as plt
import matplotlib.font_manager as fm
import numpy as nm

# CVS Line Format
# Header:
# 1 - Key Group  - 8 values
# corpus - indexType - qryFilter - qryCount - model - maxResults - RetrievabilityB - RetrievalCoefficient(B, mu, c)
# 2- ret File Group - 3 values
# G, ZeroRValuesCount, SumR
# 3 - res File - 3 values
# resLineCount - resDocCount - limitedQueries < max
# 4 - Performance - 3 values
# MAP, pbref, P10

# Global Variables

# Column Indexes
# Key Group Index
[
modelCoefficientIndex ,

# Retrievability Index
cumulativeGIndex ,
gravityGIndex ,
rSumIndex ,

# Performance Group Index
trecMapIndex ,
trecBprefIndex ,
trecP10Index ,
cwlMapIndex,
cwlP10Index,
rbp6Index ,
rbp8Index ] = [*range(1,12,1)]

GAllValue = '99'

# Input Parameters
GXAxis = [] # Array of X Axis parameters to plot
GYAxis = [] # Array of Y Axis parameters to plot
GIndexType = '' # The IndexType ('U','B','C','F')
GCsvInPath = '' # The Path Of CSV File
GCorpus = '' # Current Corpus ('A','C','W') = ('AQUAINT' , 'Core17' , 'WAPO')
GModel = '' # Current Model ('B','C','MU') = ('BM25' , 'PL2' , 'LMD')
GFBDocs = 0
GFBTerms = 0
GMuLess = False
GXAxis = 0
GYAxis = 0


def getAxisName(i):
    global GModel
    # Column Indexes
    # Key Group Indexes : modelCoefficientIndex
    # Retrievability Indexes : gravityGIndex ,  cumulativeGIndex , rSumIndex
    # Performance Group Indexes : mapIndex , bprefIndex , p10Index
    swithcer = {
        modelCoefficientIndex : GModel,
        cumulativeGIndex : 'G',
        gravityGIndex: 'G',
        rSumIndex: 'RSUM',
        trecMapIndex: 'MAP',
        trecBprefIndex: 'BPREF',
        trecP10Index: 'P10' ,
        cwlMapIndex: 'MAP',
        cwlP10Index: 'P10' ,
        rbp6Index : 'RBP0.6',
        rbp8Index: 'RBP0.8'
    }
    result = swithcer.get(i,"")
    return result

def checkMuLess (i):
    global GMuLess , GModel
    return i == modelCoefficientIndex and GMuLess and GModel == 'MU'

def getGValue (val):
    if (val == GAllValue):
        result = GAllValue
    else:
        result = val.upper()
    return result

def plotRM3 (xValues, yValues):
    global GYAxis
    if (GYIndex == trecMapIndex):
        plt.plot(xValues, yValues, label=GFBDocs,
                 marker= getMarker(GFBDocs),
                 markerSize=8
                 )
    else:
        plt.plot(xValues, yValues,
                 marker=getMarker(GFBDocs),
                 markerSize=8
                 )

def plotIndex(xValues, yValues):
    global GIndexType , GXAxis
    # Check RSUM ( Huge Values )
    if (GXIndex == rSumIndex):
        newXValues = [ str(round(x / 1000000,2)) + ' M'  for x in xValues]
    else:
        newXValues = xValues

    # Remove 5000 From LMD
    if (checkMuLess(GXIndex)):
        newXValues.pop()
        yValues.pop()

    plt.plot(newXValues, yValues, label= getIndexType(GIndexType) ,
             marker=getMarker(GIndexType),
             markerSize=8
             )

def findExSet (aKey,index):
    # Get All Lines that belongs to input Initial Key With All Parameters
    # Input : corpus, indexType, qryFilter, qryCount, model, MaxResults , b ,
    global GCsvInPath , GYAxis

    if(index == cumulativeGIndex):
        aKey += '0,'
    elif (index == gravityGIndex):
        aKey += '0.5,'
    elif (index == rSumIndex):
        if (GYIndex == cumulativeGIndex):
            aKey += '0,'
        elif (GYIndex == gravityGIndex):
            aKey += '0.5,'

    if (index == modelCoefficientIndex):
        group = 'Per'
    else:
        group = getGroup(index)
    fileName = GCsvInPath + '\\' + group + '.csv'
    f = open(fileName,'r')
    found = False
    result = []
    for line in f :
        if line.startswith(aKey):
            found = True
            result.append(line)
        elif found:
            break
    f.close()
    return result

def getChartType(y):
    global GModel
    switcher = {
        cumulativeGIndex: "G - Cumulative",
        gravityGIndex : "G - Gravity - B 0.5",
        trecMapIndex: "Mean Average Precision",
        trecP10Index : "P10",
        cwlMapIndex: "Mean Average Precision",
        cwlP10Index: "P10",
        trecBprefIndex : "Binary Preference",
        rSumIndex : "Total Retrievability Mass",
        modelCoefficientIndex : GModel ,
        rbp6Index : 'Rank Based Precision 0.6',
        rbp8Index: 'Rank Based Precision 0.8'
    }
    return switcher.get(y,"nothing")

def getMarker (i):
    switcher = {
        # Multiple  Indexes
        'U': "^", # Upper Triangle
        'B': "o", # Circle
        'C': "X", # X Filled
        'F': "*",  # Star
        # RM3
        '5': "^",  # Upper Triangle
        '10': "o",  # Circle
        '15': "X",  # X Filled
        '20': "*",  # Star
        '25': "v",  # Upper Triangle
        '30': "8",  # Circle
        '35': "s",  # X Filled
        '40': "p",  # Star
        '45': "P",  # Upper Triangle
        '50': "H"
    }
    return switcher.get(i , "nothing")

def getFont():
    # Get Libertine Font From its File
    # Specify The Location of the File in Path Variable
    # Matplotlib cache should be deleted ( The Contents of .matplotlib Folder should be deleted )
    path = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\My Work\libertine\opentype\LinLibertine_R.otf'
    prop = fm.FontProperties(fname=path)
    return prop.get_name()

def getColValue (line , index):
    # Given CSV Line Get the Value of The given Index
    colIndex = getGroupIndex(index)
    parts = line.split(",", colIndex + 1)
    value = parts[colIndex]
    value = float(value)
    return value

def getColumn (index):
    global GModel
    # Get Column Of Values Based on given index
    result = []
    # Get Initial Part Of The Key ( All Except Coefficient )
    # corpus, indexType, qryFilter, qryCount, model, MaxResults , b ,
    inKey = inputKey(index)

    # Get All Lines that begins with the previous initial Key
    exSet = findExSet(inKey,index)
    valCount = len(exSet)
    if ((GModel == 'B' and valCount != 10) and
        (GModel == 'MU' and valCount != 11) and
        (GModel == 'C' and valCount != 8)):
        print ('Invalid return values of key : ' + inKey )

    for line in exSet:
           value =  getColValue(line, index)
           result.append(value)
    return result

def inputKey(index):
    # Get The Input CSV Key Based on input Values
    # Get All Parts Of The Key Except Coefficient with Extra Comma
    # corpus, indexType, qryFilter, qryCount, model, MaxResults , b ,
    global GCorpus , GIndexType , GModel , GFBDocs , GFBTerms

    group = getGroup(index)
    if (group == 'Ret'):
        # Query Count : 50 - 300K
        qryCount = "300K"
        # maxResults : 100 - 1000
        c = "100"
    else:
        # Query Count : 50 - 300K
        qryCount = "50"
        # maxResults : 100 - 1000
        c = "1000"

    sep = ","  # CSV Separator
    qryFilter = "combinedQuery"

    corpus = getCorpus(GCorpus)
    indexType = getIndexType(GIndexType)
    model = getModel(GModel)
    key = sep.join([corpus, indexType, qryFilter, qryCount, model, c,GFBTerms,GFBDocs]) + sep
    return key

def getCorpus (c):
    switcher = {
        'A':"Aquaint",
        'C':'Core17',
        'W':'WAPO'
    }
    return switcher.get(c ,"nothing")

def getIndexType(i):
    switcher = {
        'U':"UnigramIndex",
        'B': "BigramIndex",
        'C' : "CombinedIndex",
        'F': "FieldedIndex"
    }
    return switcher.get(i ,"")

def getModel (model):
    switcher = {
            "B":"BM25",
             "MU": "LMD",
             'C' : "PL2",
         }
    return switcher.get(model ,"nothing")

def getGroup (index):
    if (index == modelCoefficientIndex):
        result = 'Key'
    elif (index >= trecMapIndex):
        result = 'Per'
    else:
        result = 'Ret'
    return result

def isRM3():
    global GFBDocs
    return GFBDocs != ''

def getGroupIndex (index):
    GIndex = 8
    modelCoefficientFileIndex = 8

    group = getGroup(index)
    if (index == modelCoefficientIndex ):
        result = modelCoefficientFileIndex
        if (group == 'Ret'):
            result += 1
    elif (group == 'Per'):
        result = index + 4
    elif (index == rSumIndex):
        result = GIndex + 2
    else :
        result = GIndex
    return result

def getFigName():
    global GXAxis , GYAxis , GModel , GFBTerms

    switcher = {
        'A':'AQ',
        'C':'CO',
        'W':'WA'
    }
    corpus = switcher.get(GCorpus,"")
    model = getModel(GModel)
    if (GYIndex == gravityGIndex):
        b = 'GB0.5'
    else:
        b = 'GB0'
    xName = getAxisName(GXIndex)
    yName = getAxisName(GYIndex)
    if (corpus == "" or model == "" or xName == "" or yName == ""):
        result = ""
    elif getGroup(GXIndex) == 'Ret' or getGroup(GYIndex) == 'Ret' :
        result = '/' + "-".join([corpus,model,b,xName,yName]) + ".png"
    elif isRM3() :
        result = '/' + "-".join([corpus,model,'RM3-fbTerms' + GFBTerms , xName,yName]) + ".png"
    else:
        result = '/' + "-".join([corpus, model, xName, yName]) + ".png"
    return result

# def printMaxTwo(xValues , yValues):
#     i = yValues.index(max(yValues))
#     print(xValues[i] , yValues[i])

def csvPlotter(outFolder):
    # Read CSV File based on Given values From setMainParameters Function
    # Check if All Models
    global GCorpus,GModel,GIndexType,GXAxis,GYAxis,GFBDocs,GFBTerms

    if (GCorpus == GAllValue):
        allCorpuses = ["A","C","W"]
    else:
        allCorpuses = [GCorpus]
    if (GModel == GAllValue):
        allModels = ["B","MU","C"]
    else:
        allModels = [GModel]

    # Check if All Indexes
    if (GIndexType == GAllValue):
        allIndexes = "B U C F".split(" ")
    elif (GIndexType == ""):
        allIndexes = []
    else:
        allIndexes = [GIndexType]

    if (GFBDocs == GAllValue):
        allFBDocs = [*range(5,55,5)]
    else:
        allFBDocs = [GFBDocs]

    if (GFBTerms == GAllValue):
        allFBTerms = [*range(10,40,10)]
    else:
        allFBTerms = [GFBTerms]

    allX = GXAxis
    allY = GYAxis
    if (not isinstance(GYAxis, list)):
        allY = [GYAxis]
    if (not isinstance(GXAxis, list)):
        allX = [GXAxis]
    figNum = 1
    for GCorpus in allCorpuses:
        for GModel in allModels :
            for GFBTerms in allFBTerms:
                GFBTerms = str(GFBTerms)
                for GFBDocs in allFBDocs:
                    GFBDocs = str(GFBDocs)
                    for GXIndex in allX:
                        for GYIndex in allY:
                            plt.figure(figNum)
                            for GIndexType in allIndexes:
                                xValues = getColumn(GXIndex)
                                yValues = getColumn(GYIndex)
                                # plotIndex(xValues, yValues)
                                # for i in range(9):
                                #     xValues.pop()
                                #     yValues.pop()
                                plotRM3(xValues, yValues)
                plotFigure()
                if (outFolder != ""):
                    figName = getFigName()
                    plt.figure(figNum).savefig(outFolder + figName)
                figNum += 1
                print ("Printed " + getChartType(GXIndex) + " " + getChartType(GYIndex))
    if (outFolder == ""):
        plotShow()

def addTicks ():
    global GXAxis , GYAxis
    plt.tick_params(labelsize=12)
    x = getTicks(GXIndex)
    y = getTicks(GYIndex)
    if (GXIndex != rSumIndex):
        plt.xticks(nm.arange(x[0], x[1], x[2]))
    plt.yticks(nm.arange(y[0], y[1], y[2]))

def getPerformanceType(i):
    if (i >= trecMapIndex and i <= trecP10Index):
        result = 'trec'
    elif (i>=cwlMapIndex):
       result = 'cwl'
    else:
        result = ''
    return result

def plotFigure ():
    global GModel,GCorpus,GXAxis, GYAxis , GFBTerms

    xTitle = getChartType(GXIndex)
    yTitle = getChartType(GYIndex)

    model = getModel(GModel)
    corpus = getCorpus(GCorpus).upper()
    pltTitle = "%s - %s" % (model,corpus)
    if isRM3():
        pltTitle += '\n RM3 - fbTerms: ' + GFBTerms
    xPerType = getPerformanceType(GXIndex)
    yPerType = getPerformanceType(GYIndex)
    # if (xPerType == 'trec' or yPerType == 'trec'):
    #     pltTitle += '- TrecEval'
    # elif (xPerType == 'cwl' or yPerType == 'cwl'):
    #     pltTitle += '- CWL'
    plt.ylabel(yTitle)
    plt.xlabel(xTitle)
    plt.title(pltTitle)
    # Legend Location : upper right 1 - upper left 2 - lower left 3
    # lower right 4 - right 5 - center left 6 - center right 7
    # lower center 8 - upper center 9 - center 10
    if (isRM3()):
        plt.legend(ncol=2 , title='fbDocs 5:5:50')
    else:
        plt.legend()
    # The Properties are Here
    fFamily = getFont()
    fSize = 17
    fWeight = 900
    plt.ylabel(yTitle, fontSize=fSize, fontweight=fWeight, fontfamily=fFamily)
    plt.xlabel(xTitle, fontSize=fSize, fontweight=fWeight, fontfamily=fFamily)
    plt.title(pltTitle, fontSize=fSize, fontweight=fWeight, fontfamily=fFamily)
    addTicks()
    # *** End Of Properties

def plotShow():
    plt.rcParams["axes.linewidth"] = 2
    plt.show()

def setMainPlotParameters(corpus, model, indexType, fbTerms, fbDocs, csvInPath, xAxis, yAxis, muLess):
    global GXAxis , GYAxis , GIndexType , GFBTerms, GFBDocs, GCsvInPath , GCorpus , GModel , GMuLess

    # All Values of MU Or Not True  - False
    GMuLess = muLess
    # Input The Main Parameters to plot
    # B (BM25) - C (PL2) - mu (LMD) - GAllValue
    GModel = model
    # Aquaint - Core17 - WAPO - GAllValue
    GCorpus = corpus
    # Index Type :
    # U (UnigramIndex) -
    # B (BigramIndex) -
    # C (CombinedIndex) -
    # F (FieldedIndex)
    GIndexType = indexType

    GFBDocs = fbDocs
    GFBTerms = fbTerms

    GCsvInPath = csvInPath
    # Column Indexes
    # Key Group Indexes : modelCoefficientIndex
    # Retrievability Indexes : gravityGIndex ,  cumulativeGIndex , rSumIndex
    # Performance Group Indexes : mapIndex , bprefIndex , p10Index

    # All Combinations will be performed
    GXAxis = xAxis
    GYAxis = yAxis


def getTicks(i):
    global GModel , GCorpus
    if isRM3():
        b = [-0.1, 1.1, 0.1]
    else:
        b = [0, 1.1, 0.1]
    c = [0, 55, 5]
    mu = [0, 5500, 500]
    muless = [0, 1100, 100]
    g = [0.3, 0.8, 0.05]
    rbp6 = [0.5 , 0.8 , 0.05]
    rbp8 = [0.4,0.75,0.05]
    rm3Map = [0.1, 0.7, 0.05]

    if (GCorpus == 'A'):
        map = [0.09, 0.17, 0.01]
        p10 = [0.25, 0.45, 0.02]
        bPref = [0.18, 0.4, 0.02]
    elif (GCorpus == 'C'):
        map = [0.09, 0.19, 0.01]
        p10 = [0.28, 0.56, 0.02]
        bPref = [0.12, 0.26, 0.01]
    else:
        g = [0.2, 0.75, 0.05]
        map = [0.04, 0.18, 0.01]
        p10 = [0.24, 0.46, 0.02]
        bPref = [0.06, 0.26, 0.02]



    result = []
    if (i == modelCoefficientIndex):
        if (checkMuLess(i)):
            result = muless
        else:
            switcher = {
                'B':b,
                'MU': mu,
                'C': c
            }
            result = switcher.get(GModel,[])
    elif (i == cumulativeGIndex or i == gravityGIndex):
        result = g
    elif (i == trecMapIndex or i == cwlMapIndex):
        if isRM3():
            result = rm3Map
        else:
            result = map
    elif (i == trecP10Index):
        result = p10
    elif (i == trecBprefIndex):
        result = bPref
    elif (i == rbp6Index):
        result = rbp6
    elif (i == rbp8Index):
        result = rbp8
    return result

if __name__ == '__main__':
    # All Values of MU Or Not True  - False
    muLess = False
    # Input The Main Parameters to plot
    # B (BM25) - C (PL2) - mu (LMD) - GAllValue
    model = 'c'

    # Aquaint - Core17 - WAPO - GAllValue
    corpus = GAllValue
    # Index Type :
    # U (UnigramIndex) -
    # B (BigramIndex) -
    # C (CombinedIndex) -
    # F (FieldedIndex)
    indexType = 'u'

    fbDocs = GAllValue
    fbTerms = GAllValue
    # csvInPath = r"C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\out.csv"
    csvInPath = r'C:\Users\kkb19103\Desktop\CSV\CSV'

    csvOutPath = ''
    csvOutPath = r'C:\Users\kkb19103\Desktop\AllPlots'

    # Column Indexes
    # Key Group Indexes : modelCoefficientIndex
    # Retrievability Indexes : gravityGIndex ,  cumulativeGIndex , rSumIndex
    # Performance Group Indexes : mapIndex , bprefIndex , p10Index

    # All Combinations will be performed
    xAxis = [modelCoefficientIndex]
    yAxis = [trecMapIndex,cwlMapIndex]
    # GYAxis = [cumulativeGIndex , gravityGIndex]

    # Refine parameters
    model = getGValue(model)
    corpus = getGValue(corpus)
    indexType = getGValue(indexType)

    setMainPlotParameters(corpus, model, indexType,fbTerms, fbDocs , csvInPath, xAxis, yAxis, muLess)
    csvPlotter(csvOutPath)