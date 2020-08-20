import pandas as pd
import matplotlib.font_manager as fm
import matplotlib.pyplot as plt
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
    rSumIndex,

    # RM3 Indexes 5
    fbTermsIndex,
    # Performance Group Index 6
    trecMapIndex,
    trecBprefIndex,
    trecP10Index,
    cwlMapIndex,
    cwlP10Index,
    rbp6Index,
    rbp8Index] = [*range(1, 13, 1)]

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
GReadSource = ""
GB = GAllValue
[GIndexStructureType,
 GSpecificTermType,
 GSpecificCoefficientPer,
 GSpecificCoefficientBias] = [*range(4)]

# ***********  Get Shortcuts ***************

# General Gets

def getFigureFileName (stage , corpus , model,xAxis,yAxis):
    result = ''
    if (stage == 3):
        # AQ-BM25-B0.75-FBTERMS-G-B=0.png
        result = '-'.join(  [corpus[:2],
                            model,
                            getModelCoefficient(model) + str(getChosenCoefficient(model)),
                            getAxisName(xAxis,model),
                            getAxisName(yAxis,model)]
                           )

    if (result != ''):
        result = result.upper() + '.png'
    return result

def getChartType(index, model):
    switcher = {
        cumulativeGIndex: "G - Cumulative - B = 0",
        gravityGIndex: "G - Gravity - B = 0.5",
        trecMapIndex: "Trec Eval Map",
        trecP10Index: "P10",
        cwlMapIndex: "CWL Map",
        cwlP10Index: "P10",
        trecBprefIndex: "Binary Preference",
        rSumIndex: "Total Retrievability Mass",
        modelCoefficientIndex: getModelCoefficient(model),
        rbp6Index: 'Rank Based Precision 0.6',
        rbp8Index: 'Rank Based Precision 0.8' ,
        fbTermsIndex : 'FbTerms'
    }
    return switcher.get(index)

def getPerformanceType(i):
    group = getGroup(i)
    result = ''
    if (group == 'per'):
        if (i >= trecMapIndex and i <= trecP10Index):
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
        '30': "8",  # Circle
        '35': "s",  # X Filled
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
    return prop.get_name()

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
        rSumIndex: 'RSUM',
        trecMapIndex: 'MAP',
        trecBprefIndex: 'BPREF',
        trecP10Index: 'P10',
        cwlMapIndex: 'MAP',
        cwlP10Index: 'P10',
        rbp6Index: 'RBP0.6',
        rbp8Index: 'RBP0.8',
        fbTermsIndex: 'fbTerms'
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
        'RBP0.6': rbp6Index,
        'RBP0.8': rbp8Index,
        'RSum': rSumIndex,
        'fbTerms': fbTermsIndex
    }
    return switcher.get(axisLabel)

def getGroup(index):
    result = ""
    if index in [modelCoefficientIndex, fbTermsIndex]:
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
    if (group == 'per' and axis > fbTermsIndex):
        result = axis + 3
    else:
        switcher = {
            modelCoefficientIndex: 8,
            fbTermsIndex: 6,
            cumulativeGIndex: 10,
            gravityGIndex: 10,
            rSumIndex: 12
        }
        result = switcher.get(axis)
    return result

#
# ******    End Get Shortcuts  ***********
#

def readWindowsFile(inGroup):
    global GCsvInPath, GCorpus
    # Windows Read
    if inGroup == 'Ticks':
        corpus = getCorpus(GCorpus)[:2].upper()
        fileName = r'C:\Users\kkb19103\Desktop\CSV\CSV\Ticks\%sTicks.csv' % (corpus)
    else:
        if inGroup == 'key':
            inGroup = 'per'
        fileName = '%s\\%s.csv' % (GCsvInPath, inGroup)
    return pd.read_csv(fileName)

# def readGoogleFile(group):
#     global GCorpus
#     if group == 'Ticks':
#         corpus = getCorpus(GCorpus)[:2].upper()
#         fileName = corpus + group + '.csv'
#     else:
#         if group == 'key':
#             group = 'per'
#         fileName = group + '.csv'
#
#     switcher = {
#         'per.csv': '1ak_b2jdAerMtE9Ah9-rl9L2MS_u9FN0S',
#         'ret.csv': '1JPeBpJfiyyImBRrYa4sWnpFDTZzHcAjS',
#         'AQTicks.csv': '12jpPUT2PPidZFM3PsTg7cnOVRWnX_rBu',
#         'COTicks.csv': '11MYQ7VxJCMrHEKewpJERWmPIzgnq9Lsw',
#         'WATicks.csv': '1h3-azAXdMxd53NgOqTtguz-a2Po06sIr'
#     }
#     fileID = switcher.get(fileName, '')
#     # Authenticate and create the PyDrive client.
#     # This only needs to be done once per notebook.
#
#
#     auth.authenticate_user()
#     gauth = GoogleAuth()
#     gauth.credentials = GoogleCredentials.get_application_default()
#     drive = GoogleDrive(gauth)
#
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


def getChosenCoefficient (model):
    switcher = {
        'BM25': 0.75,
        'LMD': 310,
        'PL2': 5.1
    }
    return switcher.get(model)

def getCriteria (df , filterType , axis , corpus, model,fbTerms):
    """
    Return Criteria Used to Filter DataFrame based on given input

    Filter Types:
        C = Over Coefficient
        F = Over FeedBack Terms
        R = Over Retrievability
    """
    criteria = (df['corpus'] == corpus)
    if (filterType == 'C'):
        criteria &=  (df['model'] == model) &  (df['fbTerms'] == fbTerms)
    elif (filterType == 'F'):
        modelCoefficient = getChosenCoefficient(model)
        criteria &= (df['RetrievalCoefficient'] == modelCoefficient)
    elif (filterType == 'R'):
        modelCoefficient = getChosenCoefficient(model)
        criteria &= (df['RetrievalCoefficient'] == modelCoefficient) & (df['fbTerms'].isin ([5,10,20]) & (df['fbDocs'].isin ([5,10,20])))

    # Filter G Values if Chosen
    if (axis == cumulativeGIndex):
        criteria &= (df['RetrievabilityB'] == 0)
    elif (axis == gravityGIndex):
        criteria &= (df['RetrievabilityB'] == 0.5)
    return criteria

def getDataFrame(filterType , axis , corpus, model,fbTerms):

    # Read DataFrame From Csv
    group = getGroup(axis)
    df = readFile(group)
    # Filter DataFrame based on Input
    criteria = getCriteria(df , filterType , axis , corpus, model,fbTerms)
    df = df[criteria]

    # Sort DataFrame
    keyHeaders = list(df.columns)
    lastIndex = 9
    if (group == 'ret'):
        lastIndex = 10
    keyHeaders = keyHeaders[:2] + [keyHeaders[4]] + keyHeaders[6:lastIndex]

    df.sort_values(by=keyHeaders , inplace=True)

    # if (displayData == 'Yes'):
    #     displayedDf (df , axis)
    return df

def displayDf(df , xValues , yValues):
    pd.set_option('mode.chained_assignment', None)
    pd.set_option('colheader_justify', 'center')
    pd.set_option('display.max_rows', None)
    pd.set_option('display.max_columns', None)
    pd.set_option('display.width', None)
    # df['Seq'] = range(1 , len(df) + 1)
    # displayedDf = df.iloc[:,[len(df.columns) - 1 ,0,1,4,6,7,8,getColumnIndex(axis)]]

    df = df.iloc[:, [0,1,4,6,7,8 ]]
    df['X'] = xValues
    df['Y'] = yValues
    df = df.rename({'RetrievalCoefficient': 'RC'}, axis='columns')
    df.reset_index(inplace=True)
    print('DataFrame \n' , str(df))

def initializeGlobals():
    global GReadSource, GCsvInPath, GCsvOutPath , figNum
    GCsvInPath = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\CSV\CSV'
    GReadSource = 'W'
    GCsvOutPath = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\2nd Experiment - RM3\AllPlots\All\3rd Stage - Bias Measurement'

    try : figNum
    except NameError : figNum = 1
    print(figNum)

def plotList(xValues, yValues , label , marker):
    plt.plot(xValues, yValues, label=label,
             marker=marker,
             markerSize=8
             )
    #
    # xValues = list(xValues)
    # yValues = list(yValues)
    # for i in range(3):
    #     x = xValues[i]
    #     y = yValues[i]
    #     plt.annotate('fbTerms 0', xy=( x ,y + 0.01 ))

def setFigNum ():
    global figNum
    plt.figure(figNum)

def showFigure(model, pltTitle, xAxisIndex, yAxisIndex, legTitle, xTicks , yTicks , outFigName):
    global GReadSource , figNum

    fig = plt.figure(figNum)
    plt.legend(title=legTitle,ncol=2)
    if (xTicks):
        plt.xticks(nm.arange(xTicks[0] , xTicks[1] , xTicks[2] ))
    if (yTicks):
        plt.yticks(nm.arange(yTicks[0] , yTicks[1] , yTicks[2] ))
    # Axis Title
    xTitle = getChartType(xAxisIndex, model)
    yTitle = getChartType(yAxisIndex, model)

    plt.ylabel(yTitle)
    plt.xlabel(xTitle)
    plt.title(pltTitle)

    # The Properties are Here

    if GReadSource == 'W':
        fWeight = 900
        fSize = 17
        fFamily = getFont()
        plt.ylabel(yTitle, fontSize=fSize, fontweight=fWeight, fontfamily=fFamily)
        plt.xlabel(xTitle, fontSize=fSize, fontweight=fWeight, fontfamily=fFamily)
        plt.title(pltTitle, fontSize=fSize, fontweight=fWeight, fontfamily=fFamily)
    else:
        fWeight = 200
        fSize = 15
        plt.ylabel(yTitle, fontSize=fSize, fontweight=fWeight)
        plt.xlabel(xTitle, fontSize=fSize, fontweight=fWeight)
        plt.title(pltTitle, fontSize=fSize, fontweight=fWeight)

    # if GPredefinedTicks:
    #  addPredefinedTicks()

    plt.show()
    if outFigName != '':
        fileName = GCsvOutPath + '\\' + outFigName
        # plt.figure(figNum).savefig(fileName)
        fig.savefig(fileName)
        figNum += 1
        print('File %s is Saved' % outFigName)