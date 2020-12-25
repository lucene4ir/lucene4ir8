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
#@markdown Check Bigrams Plot - Multiple Indexes

Corpus = "Aquaint"  # @param ["Aquaint", "Core17", "WAPO",'All']
Model = "BM25"  # @param ["BM25", "LMD", "PL2", 'All']
# @markdown The Number of values to plot - 0 for all
# PLotValuesCount = 0  # @param {type:"slider", min:0, max:16, step:1}
# @markdown Use Predefined ticks from ticks.csv files
PredefinedTicks = "No"  # @param ["Yes", "No"]
DisplayData = "No"  # @param ["Yes", "No"]

xAxis = "TrecMAP"  # @param ["modelCoefficient", "TrecMAP" , "CWLMAP","RSum - Cumulative", "RSum - Gravity"]
yAxis = "G - Cumulative"  # @param ["G - Cumulative", "G - Gravity", "TrecMAP","CWLMAP", "BPref", "P10","RBP0.6" , "RBP0.8"]

# *** End Colab Form ***

def getSingleAxisValues(df , axis , indexType):
    criteria = df['indexType'] == indexType
    return df[criteria].iloc[:, gen.getColumnIndex(axis)]

def showFigure(model , corpus , xAxisIndex , yAxisIndex):

    # Main Title
    pltTitle = "%s - %s" % (model, corpus.upper())

    # output Figure Name
    figName = ''
    if (gen.GCsvOutPath != ''):
        corpus = corpus[:2]
        xName = gen.getAxisName(xAxisIndex,model)
        yName = gen.getAxisName(yAxisIndex,model)
        b = ''
        if (yAxisIndex == gen.cumulativeGIndex ):
            b = 'GB0'
            figName = '-'.join([corpus,model,b,xName,yName])
        elif (yAxisIndex == gen.gravityGIndex):
            b = 'GB0.5'
            figName = '-'.join([corpus, model, b, xName, yName])
        else:
            figName = '-'.join([corpus, model, xName, yName])

    # xTicks = [0.02,0.2,0.02]
    # yTicks = [0 ,1,0.1]
    xTicks = ''
    yTicks = xTicks
    legendTitle = 'Index Structure'
    gen.showFigure(model, pltTitle, xAxisIndex, yAxisIndex, legendTitle, xTicks, yTicks, figName)

def csvPlotIndexStructure(Corpus, Model, DisplayData, xAxis, yAxis):
    gen.initializeGlobals('W',1,'')
    xAxisIndex = gen.getAxisIndex (xAxis)
    yAxisIndex = gen.getAxisIndex (yAxis)
    xGroup = gen.getGroup(xAxisIndex)
    yGroup = gen.getGroup(yAxisIndex)
    criteria = {
        'corpus': Corpus,
        'model':Model
    }
    '''
    cumulativeGIndex, 2
    gravityGIndex, 3
    rSumCumulativeIndex, 4 
    rSumGravityIndex, 5
    '''
    if (xGroup == 'ret'):
        b = (xAxisIndex - 4) / 2
        criteria['RetrievabilityB'] = b
    dfX = gen.getDataFrame(criteria, xAxisIndex)
    if (yGroup in [xGroup,'key']):
        dfY = dfX
    elif (yGroup == 'ret'):
        b = yAxisIndex / 2 - 1
        criteria['RetrievabilityB'] = b
        dfY = gen.getDataFrame(criteria,yAxisIndex)

    if (len(dfX) != len(dfY)):
        print ('Incompatible Axis , xValues != yValues')
        return
    allXValues = []
    allYValues = []
    gen.setFigNum()
    for i in 'U B C F'.split():
        indexType = gen.getIndexType(i)
        xValues = getSingleAxisValues(dfX,xAxisIndex,indexType)
        if (xGroup == 'ret'):
            temp = xValues.iloc[0]
            temp /= 1000000
            temp = '{:.2f} M'.format(temp)
            xValues[:] = temp

        yValues = getSingleAxisValues(dfY,yAxisIndex,indexType)
        label = gen.getIndexType(i)
        marker = gen.getMarker(i)
        gen.plotList(xValues,yValues,label,marker)
        allXValues += list(xValues)
        allYValues += list(yValues)
    if (DisplayData == 'Yes'):
        gen.displayDf(dfX, allXValues, allYValues)
    if len(xValues) > 0:
        showFigure(Model,Corpus,xAxisIndex,yAxisIndex)
    else:
        print('No Data to plot')

if __name__ == '__main__':
    [allCorpus, allModel] = gen.getAll(Corpus, Model)
    for Corpus in allCorpus:
        for Model in allModel:
            csvPlotIndexStructure(Corpus, Model, DisplayData, xAxis, yAxis)