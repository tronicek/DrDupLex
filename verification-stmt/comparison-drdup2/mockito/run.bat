@echo off
call ..\..\setEnv.bat
set SRC_DIR="/research/mockito/mockito-2.2.0/src/main"

java %OPTIONS% -jar %DRDUPLEX_JAR% drduplex-mockito.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.Separator drduplex-mockito.xml

java %OPTIONS% -jar %DRDUP2_JAR% drdup2-mockito.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.Separator drdup2-mockito.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Diff drduplex-mockito-separated.xml drdup2-mockito-separated.xml drduplex-drdup2.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff drdup2-mockito-separated.xml drduplex-mockito-separated.xml drdup2-drduplex.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drduplex-drdup2.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drdup2-drduplex.xml
