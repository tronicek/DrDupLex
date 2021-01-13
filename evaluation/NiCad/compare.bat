@echo off
set SRC_DIR="/research/BigCloneEval/BigCloneEval/ijadataset/bcb_reduced"
set TOOL_JAR="/research/projects/EvalTool/target/EvalTool-1.0-jar-with-dependencies.jar"
set CHECKER_JAR="/research/projects/CloneChecker/target/CloneChecker-1.0-jar-with-dependencies.jar"
set OPTIONS=-ea -Xmx14G

set BIGCLONEBENCH_SRC_DIR="/home/ubuntu/BigCloneBench/bcb_reduced/"
java -cp %TOOL_JAR% drdup.ChangeDir %BIGCLONEBENCH_SRC_DIR% bigclonebench-blind-minsize-6.xml
java -cp %TOOL_JAR% drdup.ChangeDir %BIGCLONEBENCH_SRC_DIR% bigclonebench-blind-minsize-10.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Separator drduplex-bigclonebench-6sc.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Separator drduplex-bigclonebench-10sc.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Diff drduplex-bigclonebench-10sc-separated.xml bigclonebench-blind-minsize-6-dir.xml drduplex-nicad.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff bigclonebench-blind-minsize-10-dir.xml drduplex-bigclonebench-6sc-separated.xml nicad-drduplex.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drduplex-nicad.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% nicad-drduplex.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Counter drduplex-nicad.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Counter nicad-drduplex.xml

java %OPTIONS% -jar %CHECKER_JAR% drduplex-nicad-checker.properties
java %OPTIONS% -jar %CHECKER_JAR% nicad-drduplex-checker.properties
