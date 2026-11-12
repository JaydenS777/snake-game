Option Explicit

Dim shell, fileSystem, projectFolder, batchPath, logPath, command, exitCode
Set shell = CreateObject("WScript.Shell")
Set fileSystem = CreateObject("Scripting.FileSystemObject")

projectFolder = fileSystem.GetParentFolderName(WScript.ScriptFullName)
batchPath = fileSystem.BuildPath(projectFolder, "Run Snake Game.bat")
logPath = fileSystem.BuildPath(shell.ExpandEnvironmentStrings("%TEMP%"), "SnakeGameLauncher.log")

command = "cmd.exe /d /c " & Chr(34) & Chr(34) & batchPath & Chr(34) _
    & " > " & Chr(34) & logPath & Chr(34) & " 2>&1" & Chr(34)
exitCode = shell.Run(command, 0, True)

If exitCode <> 0 Then
    shell.Popup "The game could not be started. Details were saved to:" _
        & vbCrLf & logPath, 0, "Snake Game", 48
End If
