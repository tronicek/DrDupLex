@echo off
call ..\..\setEnv.bat
set SRC_DIR="/research/OpenJDK/jaxp/src"

java %OPTIONS% -jar %DRDUPLEX_JAR% drduplex-jaxp.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.Separator drduplex-jaxp.xml

java %OPTIONS% -jar %DRDUP2_JAR% drdup2-jaxp.properties
java %OPTIONS% -cp %TOOL_JAR% drdup.Separator drdup2-jaxp.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Diff drduplex-jaxp-separated.xml drdup2-jaxp-separated.xml drduplex-drdup2.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Diff drdup2-jaxp-separated.xml drduplex-jaxp-separated.xml drdup2-drduplex.xml

java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drduplex-drdup2.xml
java %OPTIONS% -cp %TOOL_JAR% drdup.Sourcer %SRC_DIR% drdup2-drduplex.xml
