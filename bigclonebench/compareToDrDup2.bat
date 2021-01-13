@echo off
set SRC_DIR="/research/BigCloneEval/BigCloneEval/ijadataset/bcb_reduced"
set TOOL_JAR="/research/projects/EvalTool/target/EvalTool-1.0-jar-with-dependencies.jar"
set CHECKER_JAR="/research/projects/CloneChecker/target/CloneChecker-1.0-jar-with-dependencies.jar"
set OPTIONS=-ea -Xmx14G
set DRDUP2_DIR="/research/projects/DrDup2/bigclonebench"

java %OPTIONS% -cp %TOOL_JAR% drdup.Diff drduplex-bigclonebench-separated.xml %DRDUP2_DIR%/drdup-bigclonebench-separated.xml drduplex-drdup2.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff %DRDUP2_DIR%/drdup-bigclonebench-separated.xml drduplex-bigclonebench-separated.xml drdup2-drduplex.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drduplex-drdup2.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drdup2-drduplex.xml
