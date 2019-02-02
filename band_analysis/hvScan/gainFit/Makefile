CFLAGS:=`root-config --cflags`
LFLAGS:=`root-config --libs`

all: pmt_test

pmt_test: pmt_test.cpp
	g++ $(CFLAGS) $(LFLAGS) $^ -o $@


.PHONY: all clean

clean:
	rm -rf pmt_test *.o
