$files = Get-ChildItem -Path . -Recurse -Filter '*.java'
foreach ($f in $files) {
    $path = $f.FullName
    try {
        $bytes = [System.IO.File]::ReadAllBytes($path)
        if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
            $new = $bytes[3..($bytes.Length - 1)]
            [System.IO.File]::WriteAllBytes($path, $new)
            Write-Output "Removed BOM: $path"
        }
    } catch {
        Write-Error "Failed to process $path : $_"
    }
}
