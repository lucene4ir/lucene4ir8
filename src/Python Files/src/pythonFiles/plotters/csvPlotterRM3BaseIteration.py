# This Module is For Plotting Check Bigrams Charts
import src.pythonFiles.plotters.csvPlotterGeneral as gen

# Import Module in G Drive

# drivePath = '/content/gdrive'
# modPath = '/My Drive/Colab Notebooks'
# from google.colab import drive
# drive.mount (drivePath )
# import sys
# sys.path.append(drivePath + modPath)
# import csvPlotterGeneral as gen

"""
 *** Start Colab Form ***
 Input Data
 @title Plot Bias & Performance Experiments
"""
inCorpus = "WAPO"  # @param ["Aquaint", "Core17", "WAPO","All"]
inModel = "BM25"  # @param ["BM25", "LMD", "PL2" , "All"]
inExp = "All"  # @param ["AX", "RM3",'All']
## @markdown The Number of values to plot - 0 for all
# PLotValuesCount = 0  # @param {type:"slider", min:0, max:16, step:1}
## @markdown Use Predefined ticks from ticks.csv files
##PredefinedTicks = "No"  # @param ["Yes", "No"]
inDisplayData = "No"  # @param ["Yes", "No"]
inBase = "fbDocs" # @param ["fbDocs", "fbTerms",'beta','']
inXAxis = "TrecMAP"  # @param ["fbTerms",'fbDocs',"TrecMAP",'TrecNDCG' , "CWLMAP",'NDCG10','RBP0.4','RBP0.6','RBP0.8','P10']
inYAxis = "G - Cumulative"  # @param ["G - Cumulative", "G - Gravity","TrecMAP","CWLMAP",'NDCG10','RBP0.4','RBP0.6','RBP0.8']

# *** End Colab Form ***

def getSingleAxisValues(df, axis, criteria):
    # criteria = (df[vBase] == move) & (df['qryExpansion'] == exp)
    criteria = gen.getCriteria(df,criteria)
    df = df[criteria]
    return df.iloc[:, gen.getColumnIndex(axis)]

def showFigure(model, corpus, xAxisIndex, yAxisIndex , subTitle ,vBase):
    # Main Title
    pltTitle = "%s - %s" % (model, corpus.upper())
    if (subTitle != ''):
        pltTitle += '\n'+ subTitle

    # PlotType Title
    # pltTitle += '\n AX & RM3 - Based on %s = %s - fcDocs = 20' % (gen.getModelCoefficient(model) ,str(gen.getChosenCoefficient(model)))
    # output Figure Name

    figName = 'All' if len(inExp) != 1 else gen.getFigureFileName(3, corpus, model, inExp, xAxisIndex, yAxisIndex)
    # xTicks = gen.getTicks(xAxisIndex)
    # yTicks = gen.getTicks(yAxisIndex)
    xTicks = ''
    yTicks = xTicks
    legendTitle = 'Qry Expansion-' + vBase
    gen.showFigure(model, pltTitle, xAxisIndex, yAxisIndex, legendTitle, xTicks, yTicks, figName)

def filterDf(df):
    rng = range(10, 40, 10)
    criteria = df['fbTerms'].isin(rng)
    df = df[criteria]
    return df

def csvPlot(corpus, Model, base , exp , displayData, xAxis, yAxis):
    perCaptions = ["TrecMAP", "CWLMAP", 'NDCG10', 'RBP0.4', 'RBP0.6', 'RBP0.8']
    if (xAxis in perCaptions and yAxis in perCaptions):
        print('Invalid Axes choice , both axes are Performance Values')
        return

    xAxisIndex = gen.getAxisIndex(xAxis)
    yAxisIndex = gen.getAxisIndex(yAxis)
    rc = gen.getChosenCoefficient(Model)

    criteria = {
        'corpus': corpus,
        # 'model': Model,
        # 'qryExpansion': Exp,
        # 'beta-!': 0.4,
        # 'fbDocs': 20,
        'RetrievalCoefficient': rc
    }

    # Customize Criteria
    # Identify Qry Expansion
    if (len(exp) < 2):
        criteria['qryExpansion'] = exp
    # Identify VBase Range
    vBase = 'fbDocs' if base == 'fbTerms' else 'fbTerms'
    vBaseValue = range(10,40,10)
    criteria[vBase + '-isin'] = vBaseValue
    # Check Gini Criteria
    if (yAxisIndex in [gen.cumulativeGIndex, gen.gravityGIndex]):
        # gen.cumulativeGIndex = 2 ,  gen.gravityGIndex = 3
        b = yAxisIndex / 2 - 1
        criteria['RetrievabilityB'] = b
        baseValue = 35
    else:
        # Performance Base Range
        baseValue = 55
    # Identify Base Range
    if (base in ['fbDocs','fbTerms']):
        criteria[base+'-<'] = baseValue
        criteria['beta-isin'] = [0.4,0.5]
    else:
        # Base beta (Weight)
        criteria['beta-!'] = 0.4
        criteria['fbDocs'] = 20

    # Check Based Axis
    if (xAxis in ['fbTerms', 'fbDocs']):
        # Base on Y
        dfY = gen.getDataFrame(criteria, yAxisIndex)
        dfX = dfY
    elif (yAxis in perCaptions):
        # Base on X
        dfX = gen.getDataFrame(criteria, xAxisIndex)
        dfY = dfX
    else:
        # Base on Both (X,Y)
        dfY = gen.getDataFrame(criteria, yAxisIndex)
        criteria.pop('RetrievabilityB')
        dfX = gen.getDataFrame(criteria, xAxisIndex)

    if (len(dfX) != len(dfY)):
        print('X and Y are not of same size ')
        return

    allXValues = []
    allYValues = []
    gen.setFigNum()

    for expItem in exp:
        for move in vBaseValue:
            label = expItem + '-' + str(move)
            criteria = {
                vBase: move,
                'qryExpansion':expItem
            }
            if base != 'beta':
                criteria['beta'] = 0.4 if expItem == 'AX' else 0.5
            xValues = getSingleAxisValues(dfX, xAxisIndex, criteria)
            yValues = getSingleAxisValues(dfY, yAxisIndex, criteria)
            # Properties Label , Line , Color , Marker
            if (expItem == 'AX'):
                marker = gen.getMarker(str(move))
                line = 'solid'
            else:
                marker = gen.getMarker(str(move - 5))
                line = 'dashed'
            switcher = {
                10: 'b',
                20: 'g',
                30: 'tab:orange'
            }
            color = switcher.get(move)
            gen.plotListSpecific(xValues, yValues, label, marker, line, color)
            # gen.plotList(xValues, yValues, label, marker)
            allXValues += list(xValues)
            allYValues += list(yValues)

    if (displayData == 'Yes'):
        gen.displayDf(dfX, allXValues, allYValues)

    # gen.drawBaseLine(xAxisIndex, yAxisIndex)
    if len(xValues) > 0:
        subTitle = 'xAxis over '
        if (base == 'beta'):
            subTitle += '\u03B2={0.25,0.5,0.75} , fbDocs=20'
        else:
            subTitle += base + '=[5:5:%d]' % (baseValue - 5)
        showFigure(Model, corpus, xAxisIndex, yAxisIndex,subTitle,vBase)
    else:
        print('No Data to plot')

def getOutPath(exp):
    dir = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\2nd Experiment - RM3\AllPlots'
    if (exp == 'RM3'):
        outPath = dir + '\RM3\All\3rd Stage - Bias Measurement\Separated'
    else:
        outPath = dir + '\AX\Separated'
    outPath = r'C:\Users\kkb19103\Desktop\check'
    return outPath

if __name__ == '__main__':
    [allCorpus, allModel] = gen.getAll(inCorpus, inModel)
    exp = ['AX', 'RM3'] if (inExp == 'All') else [inExp]
    base = inBase
    # outPath = getOutPath(inExp)
    outPath = ''
    gen.initializeGlobals('W', 2, outPath)
    for corpus in allCorpus:
        csvPlot(corpus, inModel, base , exp ,  inDisplayData, inXAxis, inYAxis)