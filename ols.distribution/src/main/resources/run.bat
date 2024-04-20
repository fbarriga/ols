@echo off

:setup
rem determine the location this script is run in...
set BASEDIR=%~dp0

rem use included JRE
set path=%BASEDIR%\jre\bin;%path%

rem all paths are used relatively from the base dir...
set PLUGINDIR=%BASEDIR%\plugins
set CLASSPATH=.;%BASEDIR%\bin\*

rem give the client roughly 1gigabyte of memory
set MEMSETTINGS=-Xmx1024m

rem <https://github.com/jawi/ols/issues/125>
set SYSPROPS=-Djna.nosys=true

rem For now, use the "console enabled" java for Windows...
java %MEMSETTINGS% %SYSPROPS% -cp "%CLASSPATH%" nl.lxtreme.ols.runner.Runner -pluginDir="%PLUGINDIR%" %*

