import pandas as pd
import matplotlib.pyplot as plt
import matplotlib.font_manager as fm
import numpy as nm

# Import PyDrive and associated libraries.
# This only needs to be done once per notebook.

# from pydrive.auth import GoogleAuth
# from pydrive.drive import GoogleDrive
# from google.colab import auth
# from oauth2client.client import GoogleCredentials

# Global Variables
# Column Indexes
# Key Group Index
# All Axis Indexes
[
    modelCoefficientIndex,  # 1

    # Retrievability Index 2
    cumulativeGIndex,
    gravityGIndex,
    rSumCumulativeIndex,
    rSumGravityIndex,

    # RM3 Indexes 6
    fbTermsIndex,
    fbDocsIndex,
    # Performance Group Index 8
    trecMapIndex,
    trecBprefIndex,
    trecP10Index,
    trecNDCGIndex,
    cwlMapIndex,
    cwlNDCGIndex,
    cwlP10Index,
    rbp4Index,
    rbp6Index,
    rbp8Index] = [*range(1, 18, 1)]

# All Input Data in Global Form
[
    GCorpus,  # Current Corpus ('A','C','W') = ('AQUAINT' , 'Core17' , 'WAPO')
    GModel,  # Current Model ('B','C','MU') = ('BM25' , 'PL2' , 'LMD')
    GIndexType,  # The IndexType ('U','B','C','F')
    GFBTerms,
    GFBDocs,
    GCoefficient,
    GPlotCount,
    GXAxis,
    GYAxis,
    GPlotType,
    GPredefinedTicks
    #  GDisplayData
] = range(11)

# Other Required Global Parameters
GCsvInPath = ''  # The Path Of input CSV File
GCsvOutPath = ''  # The Path Of output CSV File
GXLimits = []
GYLimits = []
GAllValue = -999
GFigNum = 0
GReadSource = ""
GExNum = GAllValue
GB = GAllValue
[GIndexStructureType,
 GSpecificTermType,
 GSpecificCoefficientPer,
 GSpecificCoefficientBias] = [*range(4)]

# ***********  Get Shortcuts ***************

# General Gets

def getTicks(index):
    switcher = {
        trecMapIndex: [0.18, 0.26, 0.01],
        trecP10Index: [0.36, 0.46, 0.01],
        cumulativeGIndex: [0.42,0.54,0.01],
        cwlNDCGIndex:[0.525,0.725,0.025],
        fbDocsIndex:[0,55,5],
        fbTermsIndex:[0,55,5]
    }
    result = switcher.get(index, '')
    return result

def getFigureFileName(stage, corpus, model,exp, xAxis, yAxis):
    global GCsvOutPath
    result = ''
    if (GCsvOutPath != ''):
        if (stage == 3):
            # AQ-BM25-B0.75-FBTERMS-G-B=0.png
            result = '-'.join([exp,
                               corpus[:2],
                               model,
                               getModelCoefficient(model) + str(getChosenCoefficient(model)),
                               getAxisName(xAxis, model),
                               getAxisName(yAxis, model)]
                              )
    if (result != ''):
        result = result.upper() + '.png'
    return result

def getChartType(index, model):
    switcher = {
        cumulativeGIndex: "G - Cumulative - B = 0 - C = 100",
        gravityGIndex: "G - Gravity - B = 0.5 - C = 100",
        trecMapIndex: "MAP",
        trecP10Index: "P10",
        trecNDCGIndex:"Trec NDCG",
        cwlMapIndex: "CWL MAP",
        cwlP10Index: "P10",
        trecBprefIndex: "Binary Preference",
        rSumCumulativeIndex: "Total Retrievability Mass",
        rSumGravityIndex: "Total Retrievability Mass",
        modelCoefficientIndex: getModelCoefficient(model),
        rbp4Index : 'Rank Biased Precision 0.4',
        rbp6Index: 'Rank Biased Precision 0.6',
        rbp8Index: 'Rank Biased Precision 0.8',
        fbTermsIndex: 'FbTerms',
        fbDocsIndex: 'FbDocs',
        cwlNDCGIndex: 'NDCG10'
    }
    return switcher.get(index)

def getPerformanceType(i):
    group = getGroup(i)
    result = ''
    if (group == 'per'):
        if (i >= trecMapIndex and i <= trecNDCGIndex):
            result = 'TREC'
        elif i >= cwlMapIndex:
            result = 'CWL'
    return result

def getMarker(i):
    switcher = {
        # Multiple  Indexes
        'U': "^",  # Upper Triangle
        'B': "o",  # Circle
        'C': "X",  # X Filled
        'F': "*",  # Star
        # RM3
        '5': "^",  # Upper Triangle
        '10': "o",  # Circle
        '15': "X",  # X Filled
        '20': "*",  # Star
        '25': "v",  # Upper Triangle
        '30': "s",  # Circle
        '35': "8",  # X Filled
        '40': "p",  # Star
        '45': "P",  # Upper Triangle
        '50': "H"
    }
    return switcher.get(i, "nothing")

def getIndexType(i):
    switcher = {
        'U': "UnigramIndex",
        'B': "BigramIndex",
        'C': "CombinedIndex",
        'F': "FieldedIndex"
    }
    return switcher.get(i)

def getModel(model):
    switcher = {
        "b": "BM25",
        "mu": "LMD",
        'c': "PL2",
    }
    return switcher.get(model)

def getModelCoefficient(model):
    switcher = {
        "BM25": "b",
        "LMD": "mu",
        "PL2": 'c'
    }
    return switcher.get(model)

def getCorpus(c):
    switcher = {
        'A': 'AQUAINT',
        'C': 'CORE17',
        'W': 'WAPO'
    }
    return switcher.get(c)

# Special Gets

def getFont():
    # Get Libertine Font From its File
    # Specify The Location of the File in Path Variable
    # Matplotlib cache should be deleted
    # ( The Contents of .matplotlib Folder should be deleted )
    path = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\My Work\libertine\opentype\LinLibertine_R.otf'
    prop = fm.FontProperties(fname=path)
    # [fFamily, fSize, fWeight]
    return [prop.get_name(),17,900]

def getGValue(val):
    if val == GAllValue or val == '' or val == 'ALL':
        result = GAllValue
    else:
        result = val.upper()
    return result

def getAxisName(i, model):
    global GModel
    # Column Indexes
    # Key Group Indexes : modelCoefficientIndex
    # Retrievability Indexes : gravityGIndex ,  cumulativeGIndex , rSumIndex
    # Performance Group Indexes : mapIndex , bprefIndex , p10Index
    swithcer = {
        modelCoefficientIndex: getModelCoefficient(model),
        cumulativeGIndex: 'GB0',
        gravityGIndex: 'GB0.5',
        rSumCumulativeIndex: 'RSUM',
        rSumGravityIndex: 'RSUM',
        trecMapIndex: 'MAPTREC',
        trecBprefIndex: 'BPREF',
        trecP10Index: 'P10',
        trecNDCGIndex:'TrecNDCG',
        cwlMapIndex: 'MAPCWL',
        cwlP10Index: 'P10',
        rbp4Index: 'RBP0.4',
        rbp6Index: 'RBP0.6',
        rbp8Index: 'RBP0.8',
        fbTermsIndex: 'fbTerms',
        cwlNDCGIndex: 'NDCG10'
    }
    result = swithcer.get(i, "")
    return result

def getAxisIndex(axisLabel):
    switcher = {
        'modelCoefficient': modelCoefficientIndex,
        'TrecMAP': trecMapIndex,
        'CWLMAP': cwlMapIndex,
        'G - Cumulative': cumulativeGIndex,
        'G - Gravity': gravityGIndex,
        'BPref': trecBprefIndex,
        'P10': trecP10Index,
        'TrecNDCG':trecNDCGIndex,
        'RBP0.4': rbp4Index,
        'RBP0.6': rbp6Index,
        'RBP0.8': rbp8Index,
        'RSum - Cumulative': rSumCumulativeIndex,
        'RSum - Gravity':rSumGravityIndex,
        'fbTerms': fbTermsIndex,
        'fbDocs': fbDocsIndex,
         'NDCG10':cwlNDCGIndex
    }
    return switcher.get(axisLabel)

def drawBaseLine(xAxisIndex,yAxisIndex):
    criteria = {
        'qryExpansion':'Baseline'
    }
    xGroup = getGroup(xAxisIndex)
    yGroup = getGroup(yAxisIndex)
    xDf = getDataFrame(criteria,xAxisIndex)
    if ('key' in [xGroup, yGroup] or xGroup == yGroup):
        yDf = xDf
    else:
        if (yAxisIndex in [cumulativeGIndex, gravityGIndex]):
            b = yAxisIndex / 2 - 1
            # gen.cumulativeGIndex = 2 ,  gen.gravityGIndex = 3
            criteria['RetrievabilityB'] = b
        yDf = getDataFrame(criteria,yAxisIndex)
    x = xDf.iloc[:, getColumnIndex(xAxisIndex)]
    y = yDf.iloc[:, getColumnIndex(yAxisIndex)]
    plt.plot(x, y,
             marker='$B$',
             markersize=10,
             color='r'
             )

def getGroup(index):
    result = ""
    if index in [modelCoefficientIndex, fbTermsIndex,fbDocsIndex]:
        result = 'key'
    elif index < fbTermsIndex:
        result = 'ret'
    else:
        result = 'per'
    return result

def getColumnIndex(axis):
    """
                            # Program Axis Indexes      Per Index       Ret Index
[modelCoefficientIndex,             # 1                     #8              #8

    # Retrievability Index      Starting From 2
    cumulativeGIndex,               # 2                                     #10
    gravityGIndex,                  # 3                                     #10
    rSumIndex,                      # 4                                     #12
    # RM3 Indexes               Starting From 5
    fbTermsIndex,                   # 5                     #6              #6
    # Performance Group Index   Starting From 6         Starting From 9
    trecMapIndex,                   # 6
    trecBprefIndex,                 # 7
    trecP10Index,                   # 8
    cwlMapIndex,                    # 9
    cwlP10Index,                    # 10
    rbp6Index,                      # 11
    rbp8Index]                      # 12
    """
    group = getGroup(axis)
    result = 0
    if (group == 'per' and axis > fbDocsIndex):
        result = axis + 2
    else:
        switcher = {
            fbTermsIndex: 6,
            fbDocsIndex:7,
            modelCoefficientIndex: 8,
            cumulativeGIndex: 10,
            gravityGIndex: 10,
            rSumCumulativeIndex: 12,
            rSumGravityIndex:12
        }
        result = switcher.get(axis)
    return result

#
# ******    End Get Shortcuts  ***********
#

def readWindowsFile(inGroup):
    global GCsvInPath, GCorpus, GExNum
    # Windows Read
    if inGroup == 'Ticks':
        corpus = getCorpus(GCorpus)[:2].upper()
        fileName = r'C:\Users\kkb19103\Desktop\CSV\CSV\Ticks\%sTicks.csv' % (corpus)
    else:
        if inGroup == 'key':
            inGroup = 'per'
        fileName = '%s\\Ex%d%s.csv' % (GCsvInPath, GExNum, inGroup)
    return pd.read_csv(fileName)

# def readGoogleFile(group):
#     global GCorpus, GExNum
#     if group == 'Ticks':
#         corpus = getCorpus(GCorpus)[:2].upper()
#         fileName = corpus + group + '.csv'
#     else:
#         if (group == 'key'):
#             group = 'per'
#         fileName = 'Ex%d%s.csv' % (GExNum, group)
#
#     switcher = {
#         'Ex1per.csv': '12uKAxYiCQQRd8UW61OR2T5aQEYRr5na1',
#         'Ex1ret.csv': '1mWHC19d9RTjelfpQxQ1jfHjEDQhVgIaJ',
#         'Ex2per.csv': '1y-gopefnhvaFunRcoNQ6-P7f3l3kVoY3',
#         'Ex2ret.csv': '1Feri1ZqdwJ3hQk_kMFiLasHELOpaswrQ',
#         'AQTicks.csv': '12jpPUT2PPidZFM3PsTg7cnOVRWnX_rBu',
#         'COTicks.csv': '11MYQ7VxJCMrHEKewpJERWmPIzgnq9Lsw',
#         'WATicks.csv': '1h3-azAXdMxd53NgOqTtguz-a2Po06sIr'
#     }
#
#     fileID = switcher.get(fileName, '')
#
#     # Authenticate and create the PyDrive client.
#     # This only needs to be done once per notebook.
#
#     # auth.authenticate_user()
#     # gauth = GoogleAuth()
#     # gauth.credentials = GoogleCredentials.get_application_default()
#     # drive = GoogleDrive(gauth)
#
#     # Download a file based on its file ID.
#     #
#     # A file ID looks like: laggVyWshwcyP6kEI-y_W3P8D26sz
#     downloaded = drive.CreateFile({'id': fileID})
#     downloaded.GetContentFile(fileName)
#     df = pd.read_csv(fileName)
#     return df

def readFile(inGroup):
    global GReadSource
    if GReadSource.upper() == 'W':
        # None
        df = readWindowsFile(inGroup)
    else:
        None
        # df = readGoogleFile(inGroup)
    return df

def getChosenCoefficient(model):
    switcher = {
        'BM25': 0.75,
        'LMD': 310,
        'PL2': 5.1
    }
    return switcher.get(model)

def getCriteria(df, dictCriteria ):
    '''
    First Criteria should be Equal criteria
    Operators might be inserted at the end of the key in input dictionary
    -< for <
    -> for >
    -! for !=
    -isin for isin
    '''

    criteria = ''
    for item in dictCriteria:
      if (item.__contains__('-')):
          parts = item.split('-')
          key = parts[0]
          operator = parts[1]
          value = dictCriteria[item]
          if (operator == '<'):
              criteria &= df[key] < value
          elif (operator == '>'):
              criteria &= df[key] > value
          elif (operator == '!'):
              criteria &= df[key] != value
          elif (operator == 'isin'):
              criteria &= df[key].isin(value)
      else:
          # First Criteria should be Equal
          value = dictCriteria[item]
          if (len(criteria) == 0):
            criteria = df[item] == value
          else:
              criteria &= df[item] == value
    return criteria

def getDataFrame(dictCriteria , axis):
    # Read DataFrame From Csv
    group = getGroup(axis)
    df = readFile(group)
    # Filter DataFrame based on Input
    criteria = getCriteria(df, dictCriteria )
    df = df[criteria]
    # Sort DataFrame
    keyHeaders = list(df.columns)
    lastIndex = 9
    if (group == 'ret'):
        lastIndex = 10
    keyHeaders = [keyHeaders[2]] + [keyHeaders[0]] +  keyHeaders[3:4] + keyHeaders[6:lastIndex]
    df.sort_values(by=keyHeaders, inplace=True)
    return df

def displayDf(df, xValues, yValues):
    pd.set_option('mode.chained_assignment', None)
    pd.set_option('colheader_justify', 'center')
    pd.set_option('display.max_rows', None)
    pd.set_option('display.max_columns', None)
    pd.set_option('display.width', None)
    # df['Seq'] = range(1 , len(df) + 1)
    # displayedDf = df.iloc[:,[len(df.columns) - 1 ,0,1,4,6,7,8,getColumnIndex(axis)]]

    df = df.iloc[:, [0, 1, 4, 6, 7, 8]]
    df['X'] = xValues
    df['Y'] = yValues
    df = df.rename({'RetrievalCoefficient': 'RC'}, axis='columns',index=None)
    # df.reset_index(inplace=True)
    print('DataFrame \n', str(df))

def initializeGlobals(readSource, exNum ,outPath):
    global GReadSource, GCsvInPath, GCsvOutPath, GExNum

    GReadSource = readSource
    # GCsvOutPath = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\2nd Experiment - RM3\AllPlots\AXD\All\3rd Stage - Bias Measurement\Separated'
    GCsvOutPath = outPath
    # GCsvOutPath = r'C:\Users\kkb19103\Desktop'
    # GCsvOutPath = ''
    GExNum = exNum
    # C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments
    if (readSource == 'W'):
        if (exNum == 1):
            exFolder = '1st Experiment - Bigrams Influence'
        else:
            exFolder = '2nd Experiment - RM3'
        GCsvInPath = (r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\%s\CSV') % exFolder

def getAll(corpus, model):
    if (corpus == 'All'):
        allCorpus = ["Aquaint", "Core17", "WAPO"]
    else:
        allCorpus = [corpus]

    if (model == 'All'):
        allModel = ["BM25", "LMD", "PL2"]
    else:
        allModel = [model]
    return [allCorpus, allModel]

def plotListSpecific(xValues, yValues, label,marker,line,color):
    plt.plot(xValues, yValues, label=label,
             marker=marker,
             markersize=8,
             color=color,
             linestyle=line
             )

def plotList(xValues, yValues, label,marker):
    plt.plot(xValues, yValues, label=label,
             marker=marker,
             markersize=8,
             )

def setFigNum():
    global GFigNum
    GFigNum += 1
    plt.figure(GFigNum)

def showFigure(model, pltTitle, xAxisIndex, yAxisIndex, legTitle, xTicks, yTicks, outFigName):
    global GReadSource, GFigNum

    # drawBaseLine(xAxisIndex,yAxisIndex)
    fig = plt.figure(GFigNum)
    plt.legend(title=legTitle, ncol=2)
    if (xTicks):
        plt.xticks(nm.arange(xTicks[0], xTicks[1], xTicks[2]))
    if (yTicks):
        plt.yticks(nm.arange(yTicks[0], yTicks[1], yTicks[2]))

    if 'fbDocs' in legTitle:
        exp = 'fbTerms'
    else:
        exp = 'fbDocs'
    # Axis Title
    xTitle = getChartType(xAxisIndex, model)
    xTitle = xTitle
    # xTitle = base
    yTitle = getChartType(yAxisIndex, model)

    plt.ylabel(yTitle)
    plt.xlabel(xTitle)
    plt.title(pltTitle)

    # The Properties are Here

    [fFamily, fSize, fWeight] = getFont()
    if GReadSource == 'W':
        # fWeight = 900
        # fSize = 17
        [fFamily , fSize , fWeight]  = getFont()
        # fFamily = 'Tahoma'
        plt.ylabel(yTitle, fontsize=fSize, fontweight=fWeight, fontfamily=fFamily)
        plt.xlabel(xTitle, fontsize=fSize, fontweight=fWeight, fontfamily=fFamily)
        plt.title(pltTitle, fontsize=fSize, fontweight=fWeight, fontfamily=fFamily)
    else:
        # fWeight = 200
        # fSize = 15
        plt.ylabel(yTitle, fontsize=fSize, fontweight=fWeight)
        plt.xlabel(xTitle, fontsize=fSize, fontweight=fWeight)
        plt.title(pltTitle, fontsize=fSize, fontweight=fWeight)

    # if GPredefinedTicks:
    #  addPredefinedTicks()
    plt.show()

    if outFigName != '':
        fileName = GCsvOutPath + '\\' + outFigName
        # plt.figure(GFigNum).savefig(fileName)
        fig.savefig(fileName)
        print('File %s is Saved' % outFigName)
