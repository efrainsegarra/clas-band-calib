from __future__ import division
import numpy as np
import matplotlib.pyplot as plt

runNums = ["195","225","229"]


Map = {}
for runno in runNums:
	with open("run_"+runno+"-attenFit.txt","rb") as g:
		for line in g:
			if '#' in line: continue

			li=line.strip().split("\t")
			hashCode = li[0]+li[1]+li[2]

			#if float(li[3]) == 0.: continue		
	
			if hashCode in Map:
				Map[hashCode].append( float(li[3]) )
			else:
				Map[hashCode] = [ float(li[3]) ]


with open("attenuation_lengths.txt","w") as f:
	for key in Map:
		arr0 = np.asarray(Map[key],dtype=float) # List of attenuation lengths for each bar
		

		f.write("%s\t%s\t%s\t%f\t%f\n" % (key[0],key[1],key[2],np.mean(arr0),np.std(arr0) ) )
		

