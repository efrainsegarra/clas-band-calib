from __future__ import division
import numpy as np
import matplotlib.pyplot as plt


Map_res = {}
Map_adcMean = {}
Map_adcMin = {}
Map_adcMax = {}
with open("run_189.txt") as g:
	for line in g:
		if '#' in line: continue

		li=line.strip().split("\t")
		
		hashCode = li[1]+li[0]+li[2]

		if hashCode in Map_res:
			Map_res[hashCode].append(	float(li[4])/np.sqrt(2.)	)
			Map_adcMin[hashCode].append(	float(li[5])			)
			Map_adcMean[hashCode].append(	float(li[6])			)
			Map_adcMax[hashCode].append(	float(li[7])			)

		else:
			Map_res[hashCode] 	= [float(li[4])/np.sqrt(2.)	]
			Map_adcMin[hashCode] 	= [float(li[5])			]
			Map_adcMean[hashCode]	= [float(li[6])			]
			Map_adcMax[hashCode]	= [float(li[7])			]	 


nFigures = 100
for key in Map_adcMin:
	currLen = len(Map_adcMin[key])
	if currLen < nFigures: nFigures = currLen

for i in np.linspace(0,nFigures-1,nFigures):
	idx = int(i)

	bars = []
	ress = []
	adcs = []
	adcErr = [[],[]]

	for key in Map_res:
		if  Map_res[key][idx] == 0: continue
		bars.append(key)
		ress.append( Map_res[key][idx] )
		adcs.append( Map_adcMean[key][idx] )
		adcErr[0].append( Map_adcMin[key][idx] )
		adcErr[1].append( Map_adcMax[key][idx] )

	bars = np.asarray(bars,dtype=int)

	plt.figure(idx)
	min_a = np.mean(adcErr[0]) / 2000.
	max_a = np.mean(adcErr[1]) / 2000.
	plt.errorbar( bars, ress, fmt='o')
	plt.title("Resolution Between "+str(int(min_a*10)/10)+" and "+str(int(max_a*10)/10)+" MeVee")
	plt.xlabel("Bar ID")
	plt.ylabel("Resolution [ns]")
	plt.ylim([0,0.5])


	meanRes = np.mean(ress)
	plt.axhline(y=meanRes,color='red',linestyle='--')
	plt.axvline(x=100,color='black')
	plt.axvline(x=200,color='black')
	plt.axvline(x=300,color='black')
	plt.axvline(x=400,color='black')
	plt.axvline(x=500,color='black')
	plt.axvline(x=600,color='black')
	

plt.show()
