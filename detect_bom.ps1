Get-ChildItem -Path . -Recurse -Filter '*.java' | ForEach-Object {
    $path = $_.FullName
    try {
        $fs = [System.IO.File]::OpenRead($path)
        $b1 = $fs.ReadByte()
        $b2 = $fs.ReadByte()
        $b3 = $fs.ReadByte()
    } finally {
        if ($fs) { $fs.Close() }
    }
    if ($b1 -eq 0xEF -and $b2 -eq 0xBB -and $b3 -eq 0xBF) {
        Write-Output $path
    }
}
