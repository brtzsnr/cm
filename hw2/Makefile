.PHONY: all compile cl ct fl ft lfl lft

all: cl ct fl ft

compile:
	javac data_structures/*.java data_structures/implementation/*.java

cl:	compile
	java -ea -Xmx4g data_structures/Main cl ${THREADS}

ct:	compile
	java -ea -Xmx4g data_structures/Main ct ${THREADS}

fl:	compile
	java -ea -Xmx4g data_structures/Main fl ${THREADS}

ft:	compile
	java -ea -Xmx4g data_structures/Main ft ${THREADS}

lfl:	compile
	java -ea -Xmx4g data_structures/Main lfl ${THREADS}

lft:	compile
	java -ea -Xmx4g data_structures/Main lft ${THREADS}
