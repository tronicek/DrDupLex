set SRC_DIR="/research/BigCloneEval/BigCloneEval/ijadataset/bcb_reduced"
set DRDUPLEX_JAR="/research/projects/DrDupLex/target/DrDupLex-1.0-jar-with-dependencies.jar"
set TOOL_JAR="/research/projects/EvalTool/target/EvalTool-1.0-jar-with-dependencies.jar"
set CHECKER_JAR="/research/projects/CloneChecker/target/CloneChecker-1.0-jar-with-dependencies.jar"
set OPTIONS=-ea -Xmx14G

java %OPTIONS% -jar %DRDUPLEX_JAR% bigclonebench-sc.properties 2> output-sc.txt
java %OPTIONS% -cp %TOOL_JAR% drdup.Separator drduplex-bigclonebench-sc.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Counter drduplex-bigclonebench-sc-separated.xml
