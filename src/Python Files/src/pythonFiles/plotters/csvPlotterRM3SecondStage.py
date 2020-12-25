'''
This Module is For Plotting Second Stage Of Qry Expansion Experiments Number 2
Plot Bias Results For RM3 beta = 0.5 / AX beta = 0.4 - when b = 0.75
'''

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
Corpus = "Aquaint"  # @param ["Aquaint", "Core17", "WAPO","All"]
Model = "BM25"  # @param ["BM25", "LMD", "PL2" , "All"]
Exp = "RM3"  # @param ["AX", "RM3",'All']
## @markdown The Number of values to plot - 0 for all
# PLotValuesCount = 0  # @param {type:"slider", min:0, max:16, step:1}
## @markdown Use Predefined ticks from ticks.csv files
##PredefinedTicks = "No"  # @param ["Yes", "No"]
DisplayData = "No"  # @param ["Yes", "No"]

xAxis = "TrecMAP" # @param ["fbTerms","TrecMAP",'TrecNDCG',"CWLMAP",'NDCG10','RBP0.4','RBP0.6','RBP0.8']
yAxis = "G - Cumulative"  # @param ["G - Cumulative", "G - Gravity","TrecMAP","CWLMAP",'NDCG10','RBP0.4','RBP0.6','RBP0.8']
# *** End Colab Form ***

specialColoring = True

def getSingleAxisValues(df , axis , fbDocs):
    criteria = df['fbDocs'] == fbDocs
    return df[criteria].iloc[:, gen.getColumnIndex(axis)]

def showFigure(model , corpus , xAxisIndex , yAxisIndex):
    # Main Title
    pltTitle = "%s - %s" % (model, corpus.upper())

    # PlotType Title
    pltTitle += '\n %s - Based on %s = %s' % (Exp , gen.getModelCoefficient(model) ,str(gen.getChosenCoefficient(model)))

    # output Figure Name
    figName = gen.getFigureFileName(3,corpus,model,Exp,xAxisIndex , yAxisIndex)

    # xTicks = [0,55,5]
    # [0.22,0.27,0.01] [5,55,5]
    # yTicks = [0.25,0.3,0.005]
    # corpus = corpus[0].upper()
    # if corpus == 'C':
    #     yTicks = [0.23, 0.305, 0.005] # Core
    # elif corpus == 'A':
    #     yTicks = [0.19, 0.27, 0.005]  # AQuaint
    # else:
    #     yTicks = [0.245, 0.325, 0.005]  # Wapo
    xTicks = ''
    yTicks = ''
    legendTitle = 'fbDocs'

    gen.showFigure(model, pltTitle, xAxisIndex, yAxisIndex, legendTitle , xTicks, yTicks, figName)

def filterDef (df,model):
    if (model != 'BM25'):
        avRange = [5, 10, 20]
        criteria = df['fbTerms'].isin(avRange) & df['fbDocs'].isin(avRange)
        df = df[criteria]
    return df

def csvPlot(Corpus, Model, DisplayData , xAxis , yAxis):

    perCaptions = ["TrecMAP","CWLMAP",'NDCG10','RBP0.4','RBP0.6','RBP0.8']
    if (xAxis in perCaptions and yAxis in perCaptions):
        print ('Invalid Axes choice , both axes are Performance Values')
        return

    xAxisIndex = gen.getAxisIndex (xAxis)
    yAxisIndex = gen.getAxisIndex (yAxis)
    rc = gen.getChosenCoefficient(Model)
    if (Exp == 'RM3'):
        beta = 0.5
    else:
        beta = 0.4
    criteria = {
        'corpus' : Corpus,
        'model' : Model,
        'qryExpansion': Exp,
        'beta' : beta,
        'RetrievalCoefficient': rc
    }
    fbdocsRange = range(5, 55, 5)
    if (yAxisIndex in [gen.cumulativeGIndex, gen.gravityGIndex]):
        fbdocsRange = range(5, 35, 5)
        # gen.cumulativeGIndex = 2 ,  gen.gravityGIndex = 3
        b = yAxisIndex / 2 - 1
        criteria['RetrievabilityB'] = b
    if (xAxis == 'fbTerms'):
        dfY = gen.getDataFrame(criteria, yAxisIndex)
        # dfY = filterDef(dfY,Model)
        dfX = dfY
    elif (yAxis in perCaptions):
        dfX = gen.getDataFrame(criteria, xAxisIndex)
        dfY = dfX
    else:
        dfY = gen.getDataFrame(criteria, yAxisIndex)
        criteria.pop('RetrievabilityB')
        criteria['fbTerms-<'] = 31
        criteria['fbDocs-<'] = 31
        dfX = gen.getDataFrame(criteria, xAxisIndex)
        # dfX = filterDef(dfX,Model)

    if (len(dfX) != len(dfY)):
        print('X and Y are not of same size ')
        return

    allXValues = []
    allYValues = []
    gen.setFigNum()
    for fbDocs in fbdocsRange:
        label = fbDocs
        marker = gen.getMarker(str(fbDocs))
        xValues = getSingleAxisValues(dfX,xAxisIndex,fbDocs)
        yValues = getSingleAxisValues(dfY,yAxisIndex,fbDocs)
        gen.plotList(xValues,yValues,label,marker)
        allXValues += list(xValues)
        allYValues += list(yValues)

    if (DisplayData == 'Yes'):
        gen.displayDf(dfX , allXValues , allYValues)
    if len(xValues) > 0:
        showFigure(Model, Corpus, xAxisIndex, yAxisIndex)
    else:
        print('No Data to plot')

if __name__ == '__main__':
    [allCorpus, allModel] = gen.getAll(Corpus, Model)
    if (Exp == 'All'):
        allExp = ['AX','RM3']
    else:
        allExp = [Exp]
    dir = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\2nd Experiment - RM3\AllPlots'
    if (Exp == 'RM3'):
        outPath = dir + '\RM3\All\3rd Stage - Bias Measurement\Separated'
    else:
        outPath = dir + '\AX\Separated'
    outPath = r'C:\Users\kkb19103\Desktop\check'
    outPath = r'C:\Users\kkb19103\Desktop\My Files 07-08-2019\BiasMeasurementExperiments\2nd Experiment - RM3\AllPlots\temp'
    # outPath=''
    gen.initializeGlobals('W', 2 , outPath)
    # for Corpus in allCorpus:
        # for Model in allModel:
            # for Exp in allExp:
    csvPlot (Corpus, Model, DisplayData , xAxis ,  yAxis)