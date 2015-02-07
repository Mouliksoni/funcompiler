1.)Run the make file from the directory where this readme is present.
2.)To generate the IR and tiny code, run
	java -cp classes:lib/antlr.jar Micro <path_to_input_file>
3.)The tiny code(along with the commented IR) will be generated in the current directory with name out.tiny
4.)Run the tiny code on the simulator : "tinyR out.tiny"





