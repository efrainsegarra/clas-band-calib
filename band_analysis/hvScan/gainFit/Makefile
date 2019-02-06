CFLAGS:=`root-config --cflags`
LFLAGS:=`root-config --libs`

all: gainfitter

gainfitter: gainfitter.cpp
	g++ -std=c++11 $(CFLAGS) $(LFLAGS) $^ -o $@


.PHONY: all clean

clean:
	rm -rf gainfitter *.o
