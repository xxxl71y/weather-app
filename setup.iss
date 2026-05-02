#define AppName "Weather Now"
#define AppExeName "msedge.exe"
#define AppURL "https://xxxl71y.github.io/weather-app/"

[Setup]
AppId={{A1B2C3D4-E5F6-7890-ABCD-EF1234567890}
AppName={#AppName}
AppVersion=1.4
DefaultDirName={localappdata}\{#AppName}
DefaultGroupName={#AppName}
OutputDir=.
OutputBaseFilename=WeatherNow-Setup
Compression=none
SolidCompression=no
SignedUninstaller=yes
UseSetupLdr=no
UninstallDisplayIcon={app}\icon.svg
PrivilegesRequired=lowest
WizardStyle=modern
DisableProgramGroupPage=yes
; 简体中文界面
ShowLanguageDialog=no

[Messages]
; 默认英文即可, Inno Setup 会自动检测系统语言
BeveledLabel={#AppName}

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
Source: "index.html"; DestDir: "{app}"; Flags: ignoreversion
Source: "manifest.json"; DestDir: "{app}"; Flags: ignoreversion
Source: "sw.js"; DestDir: "{app}"; Flags: ignoreversion
Source: "icon.svg"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
Name: "{userdesktop}\Weather"; Filename: "{sd}\Program Files (x86)\Microsoft\Edge\Application\{#AppExeName}"; Parameters: "--app={#AppURL}"; WorkingDir: "{app}"; Comment: "实时天气查询"
Name: "{userdesktop}\Weather"; Filename: "{sd}\Program Files\Microsoft\Edge\Application\{#AppExeName}"; Parameters: "--app={#AppURL}"; WorkingDir: "{app}"; Comment: "实时天气查询"; Check: DirExists(ExpandConstant('{sd}\Program Files\Microsoft\Edge\Application'))
Name: "{group}\Weather"; Filename: "{sd}\Program Files (x86)\Microsoft\Edge\Application\{#AppExeName}"; Parameters: "--app={#AppURL}"; WorkingDir: "{app}"
Name: "{group}\Weather"; Filename: "{sd}\Program Files\Microsoft\Edge\Application\{#AppExeName}"; Parameters: "--app={#AppURL}"; WorkingDir: "{app}"; Check: DirExists(ExpandConstant('{sd}\Program Files\Microsoft\Edge\Application'))
Name: "{group}\Uninstall Weather Now"; Filename: "{uninstallexe}"

[Run]
Filename: "{sd}\Program Files (x86)\Microsoft\Edge\Application\{#AppExeName}"; Parameters: "--app={#AppURL}"; Description: "启动 Weather Now"; Flags: nowait postinstall skipifsilent shellexec
Filename: "{sd}\Program Files\Microsoft\Edge\Application\{#AppExeName}"; Parameters: "--app={#AppURL}"; Description: "启动 Weather Now"; Flags: nowait postinstall skipifsilent shellexec; Check: DirExists(ExpandConstant('{sd}\Program Files\Microsoft\Edge\Application'))
