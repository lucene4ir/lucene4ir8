# This Module is For Plotting Check Bigrams Charts
import src.pythonFiles.csvPlotterGeneral as gen


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

Corpus = "Aquaint"  # @param ["Aquaint", "Core17", "WAPO"]
Model = "BM25"  # @param ["BM25", "LMD", "PL2"]
# @markdown The Number of values to plot - 0 for all
# PLotValuesCount = 0  # @param {type:"slider", min:0, max:16, step:1}
# @markdown Use Predefined ticks from ticks.csv files
PredefinedTicks = "No"  # @param ["Yes", "No"]
DisplayData = "No"  # @param ["Yes", "No"]

xAxis = "TrecMAP"  # @param ["modelCoefficient", "TrecMAP" , "CWLMAP","RSum"]
yAxis = "G - Gravity"  # @param ["G - Cumulative", "G - Gravity", "TrecMAP","CWLMAP", "BPref", "P10","RBP0.6" , "RBP0.8"]

# *** End Colab Form ***

def getSingleAxisValues(df , axis , indexType):
    criteria = df['indexType'] == indexType
    return df[criteria].iloc[:, gen.getColumnIndex(axis)]

def showFigure(model , corpus , xAxisIndex , yAxisIndex):

    # Main Title
    pltTitle = "%s - %s" % (model, corpus.upper())

    # Performance Extension (Trec or CWL)
    perType = gen.getPerformanceType(xAxisIndex)
    if perType != '':
        pltTitle += '- ' + perType + ' Measurement'
    else:
        perType = gen.getPerformanceType(yAxisIndex)
        if perType != '':
            pltTitle += '- ' + perType.upper() + ' Measurement'

    # PlotType Title
    pltTitle += '\n Multiple Indexes'

    # output Figure Name
    figName = ''
    if (gen.GCsvOutPath != ''):
        corpus = corpus[:2]
        xName = gen.getAxisName(xAxisIndex)
        yName = gen.getAxisName(yAxisIndex)
        b = ''
        if (yAxisIndex == gen.cumulativeGIndex ):
            b = 'GB0'
            figName = '-'.join(corpus,model,b,xName,yName)
        elif (yAxisIndex == gen.gravityGIndex):
            b = 'GB0.5'
            figName = '-'.join(corpus, model, b, xName, yName)
        else:
            figName = '-'.join(corpus, model, xName, yName)

    xTicks = ''
    yTicks = ''
    legendTitle = 'Index Structure'
    gen.showFigure(model, pltTitle, xAxisIndex, yAxisIndex, legendTitle, xTicks, yTicks, figName)

def csvPlotIndexStructure(Corpus, Model, DisplayData, xAxis, yAxis):

    gen.initializeGlobals()
    xAxisIndex = gen.getAxisIndex (xAxis)
    yAxisIndex = gen.getAxisIndex (yAxis)
    xGroup = gen.getGroup(xAxisIndex)
    yGroup = gen.getGroup(yAxisIndex)
    dfX = gen.getDataFrame('C',xAxisIndex,Corpus,Model,0 )
    dfY = dfX
    if (xGroup not in ['key' , yGroup]):
        dfY = gen.getDataFrame('C',yAxisIndex,Corpus,Model,0 )

    allXValues = []
    allYValues = []
    for i in ['U','B','C' , 'F']:
        indexType = gen.getIndexType(i)
        xValues = getSingleAxisValues(dfX,xAxisIndex,indexType)
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
    csvPlotIndexStructure(Corpus, Model, DisplayData, xAxis, yAxis)