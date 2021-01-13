set SRC_DIR="/research/BigCloneEval/BigCloneEval/ijadataset/bcb_reduced"
set DRDUPLEX_JAR="/research/projects/DrDupLex/target/DrDupLex-1.0-jar-with-dependencies.jar"
set TOOL_JAR="/research/projects/EvalTool/target/EvalTool-1.0-jar-with-dependencies.jar"
set CHECKER_JAR="/research/projects/CloneChecker/target/CloneChecker-1.0-jar-with-dependencies.jar"
set OPTIONS=-ea -Xmx14G

java %OPTIONS% -jar %DRDUPLEX_JAR% bigclonebench.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.Separator drduplex-bigclonebench.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Counter drduplex-bigclonebench-separated.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drduplex-bigclonebench-separated.xml
java %OPTIONS% -jar %CHECKER_JAR% checker.properties
