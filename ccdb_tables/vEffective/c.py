from __future__ import division
import numpy as np
import sys
import matplotlib.pyplot as plt


if len(sys.argv)<3 or '.txt' in sys.argv[1]:
	print 'Invalid number of arguments. Please use:\n\tpython c.py [OutputDir] [InputList.txt]'
	exit(-1)

Map = {}
for fi in sys.argv[2:]:
	if 'tdc' not in fi: continue
	with open(fi,'rb') as a:
		for line in a:
			if '#' in line: continue
			if 'Inf' in line: continue
			li = line.strip().split("\t")
			if len(li) != 7: continue

			hashCode = li[0]+li[1]+li[2]
			
			if hashCode not in Map: Map[hashCode] = {}

			for idx in range(3,7):
				if (idx-3) in Map[hashCode]:
					Map[hashCode][idx-3].append( float(li[idx]) )
				else:
					Map[hashCode][idx-3] = [ float(li[idx]) ]


name = ["time_offsets","effective_velocity"]
with open( sys.argv[1]+name[0]+".txt", 'w') as g, open(sys.argv[1]+name[1]+".txt",'w') as h:
	for key in Map:
		arr0 = np.asarray(Map[key][0],dtype=float) # TDC offset
		arr1 = np.asarray(Map[key][1],dtype=float) # TDC vEff
		arr2 = np.asarray(Map[key][2],dtype=float) # FADC offset
		arr3 = np.asarray(Map[key][3],dtype=float) # FADC vEff
		g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (key[0],key[1],key[2],np.mean(arr0),np.mean(arr2),np.std(arr0),np.std(arr2) ) )
		h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (key[0],key[1],key[2],np.mean(arr1),np.mean(arr3),np.std(arr1),np.std(arr3) ) )
	
	# Add veto information to table:
	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (1,6,1, 0.,0.,0.,0. ) )
	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (1,6,2, 0.,0.,0.,0. ) )
	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (1,6,3, 0.,0.,0.,0. ) )

	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (2,6,1, 0.,0.,0.,0. ) )
	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (2,6,2, 0.,0.,0.,0. ) )
	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (2,6,3, 0.,0.,0.,0. ) )
	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (2,6,4, 0.,0.,0.,0. ) )
	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (2,6,5, 0.,0.,0.,0. ) )
	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (2,6,6, 0.,0.,0.,0. ) )
	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (2,6,7, 0.,0.,0.,0. ) )

	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (3,6,1, 0.,0.,0.,0. ) )
	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (3,6,2, 0.,0.,0.,0. ) )
	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (3,6,3, 0.,0.,0.,0. ) )
	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (3,6,4, 0.,0.,0.,0. ) )
	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (3,6,5, 0.,0.,0.,0. ) )
	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (3,6,6, 0.,0.,0.,0. ) )

	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (4,6,1, 0.,0.,0.,0. ) )
	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (4,6,2, 0.,0.,0.,0. ) )
	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (4,6,3, 0.,0.,0.,0. ) )
	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (4,6,4, 0.,0.,0.,0. ) )
	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (4,6,5, 0.,0.,0.,0. ) )
	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (4,6,6, 0.,0.,0.,0. ) )

	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (5,6,1, 0.,0.,0.,0. ) )
	g.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (5,6,2, 0.,0.,0.,0. ) )


	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (1,6,1, 0.,0.,0.,0. ) )
	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (1,6,2, 0.,0.,0.,0. ) )
	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (1,6,3, 0.,0.,0.,0. ) )

	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (2,6,1, 0.,0.,0.,0. ) )
	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (2,6,2, 0.,0.,0.,0. ) )
	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (2,6,3, 0.,0.,0.,0. ) )
	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (2,6,4, 0.,0.,0.,0. ) )
	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (2,6,5, 0.,0.,0.,0. ) )
	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (2,6,6, 0.,0.,0.,0. ) )
	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (2,6,7, 0.,0.,0.,0. ) )

	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (3,6,1, 0.,0.,0.,0. ) )
	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (3,6,2, 0.,0.,0.,0. ) )
	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (3,6,3, 0.,0.,0.,0. ) )
	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (3,6,4, 0.,0.,0.,0. ) )
	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (3,6,5, 0.,0.,0.,0. ) )
	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (3,6,6, 0.,0.,0.,0. ) )

	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (4,6,1, 0.,0.,0.,0. ) )
	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (4,6,2, 0.,0.,0.,0. ) )
	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (4,6,3, 0.,0.,0.,0. ) )
	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (4,6,4, 0.,0.,0.,0. ) )
	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (4,6,5, 0.,0.,0.,0. ) )
	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (4,6,6, 0.,0.,0.,0. ) )

	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (5,6,1, 0.,0.,0.,0. ) )
	h.write("%s\t%s\t%s\t%f\t%f\t%f\t%f\n" % (5,6,2, 0.,0.,0.,0. ) )
