$SampleRate = 22050
$RawDir = Join-Path (Split-Path $PSScriptRoot -Parent) "app\src\main\res\raw"

function Add-SegmentSamples {
    param(
        [System.Collections.Generic.List[Int16]] $Samples,
        [double] $Frequency,
        [double] $Duration,
        [double] $Amplitude,
        [string] $WaveType
    )

    $count = [int]($SampleRate * $Duration)
    for ($n = 0; $n -lt $count; $n++) {
        $t = $n / $SampleRate
        $phase = 2 * [Math]::PI * $Frequency * $t

        if ($Frequency -le 0) {
            $tone = 0
        } elseif ($WaveType -eq "square") {
            $tone = if ([Math]::Sin($phase) -ge 0) { 1 } else { -1 }
        } elseif ($WaveType -eq "triangle") {
            $tone = (2 / [Math]::PI) * [Math]::Asin([Math]::Sin($phase))
        } elseif ($WaveType -eq "bell") {
            $tone = [Math]::Sin($phase) + 0.35 * [Math]::Sin(2 * $phase) + 0.15 * [Math]::Sin(3 * $phase)
        } else {
            $tone = [Math]::Sin($phase)
        }

        $attack = [Math]::Min(1.0, $n / [Math]::Max(1.0, $SampleRate * 0.025))
        $release = [Math]::Min(1.0, ($count - $n) / [Math]::Max(1.0, $SampleRate * 0.06))
        $envelope = [Math]::Max(0.0, [Math]::Min($attack, $release))
        if ($WaveType -eq "bell") {
            $envelope = $envelope * [Math]::Exp(-2.2 * $t / [Math]::Max(0.1, $Duration))
        }

        $value = [int16]([Math]::Max(-30000, [Math]::Min(30000, $tone * $Amplitude * 15000 * $envelope)))
        $Samples.Add($value)
    }
}

function Write-Wav {
    param(
        [string] $FileName,
        [string] $WaveType,
        [array] $Segments
    )

    $samples = [System.Collections.Generic.List[Int16]]::new()
    foreach ($segment in $Segments) {
        Add-SegmentSamples $samples $segment[0] $segment[1] $segment[2] $WaveType
    }

    $path = Join-Path $RawDir $FileName
    $dataSize = $samples.Count * 2
    $writer = [System.IO.BinaryWriter]::new([System.IO.File]::Create($path))
    $writer.Write([System.Text.Encoding]::ASCII.GetBytes("RIFF"))
    $writer.Write([int](36 + $dataSize))
    $writer.Write([System.Text.Encoding]::ASCII.GetBytes("WAVE"))
    $writer.Write([System.Text.Encoding]::ASCII.GetBytes("fmt "))
    $writer.Write([int]16)
    $writer.Write([int16]1)
    $writer.Write([int16]1)
    $writer.Write([int]$SampleRate)
    $writer.Write([int]($SampleRate * 2))
    $writer.Write([int16]2)
    $writer.Write([int16]16)
    $writer.Write([System.Text.Encoding]::ASCII.GetBytes("data"))
    $writer.Write([int]$dataSize)
    foreach ($sample in $samples) {
        $writer.Write($sample)
    }
    $writer.Close()
}

$tracks = @(
    @("track_01.wav", "sine", @(@(262,0.25,0.70), @(330,0.25,0.70), @(392,0.25,0.70), @(523,0.55,0.75), @(0,0.08,0), @(392,0.22,0.65), @(523,0.75,0.80))),
    @("track_02.wav", "square", @(@(330,0.16,0.50), @(0,0.04,0), @(330,0.16,0.50), @(392,0.16,0.50), @(0,0.04,0), @(494,0.18,0.50), @(392,0.18,0.45), @(330,0.30,0.50), @(262,0.40,0.45))),
    @("track_03.wav", "triangle", @(@(392,0.35,0.70), @(349,0.35,0.70), @(330,0.35,0.70), @(294,0.45,0.65), @(262,0.80,0.70))),
    @("track_04.wav", "bell", @(@(523,0.45,0.80), @(659,0.45,0.75), @(784,0.55,0.70), @(1047,0.80,0.65), @(784,0.55,0.65), @(659,0.75,0.65))),
    @("track_05.wav", "square", @(@(196,0.12,0.55), @(0,0.06,0), @(247,0.12,0.55), @(0,0.06,0), @(294,0.12,0.55), @(0,0.06,0), @(330,0.12,0.55), @(392,0.24,0.55), @(330,0.24,0.55), @(294,0.24,0.50))),
    @("track_06.wav", "sine", @(@(440,0.50,0.65), @(392,0.25,0.60), @(349,0.25,0.60), @(330,0.50,0.65), @(294,0.25,0.55), @(330,0.25,0.60), @(392,0.90,0.70))),
    @("track_07.wav", "triangle", @(@(262,0.18,0.60), @(330,0.18,0.60), @(392,0.18,0.60), @(330,0.18,0.60), @(262,0.18,0.60), @(0,0.08,0), @(294,0.18,0.60), @(370,0.18,0.60), @(440,0.36,0.65), @(554,0.36,0.65))),
    @("track_08.wav", "bell", @(@(587,0.30,0.75), @(0,0.12,0), @(523,0.30,0.70), @(0,0.12,0), @(466,0.30,0.70), @(0,0.12,0), @(392,0.90,0.65))),
    @("track_09.wav", "sine", @(@(330,0.22,0.65), @(392,0.22,0.65), @(494,0.22,0.65), @(659,0.44,0.70), @(587,0.22,0.65), @(494,0.22,0.65), @(392,0.44,0.70), @(330,0.65,0.65))),
    @("track_10.wav", "bell", @(@(392,0.55,0.60), @(330,0.55,0.55), @(294,0.55,0.55), @(262,0.75,0.50), @(0,0.20,0), @(262,1.00,0.45)))
)

foreach ($track in $tracks) {
    Write-Wav $track[0] $track[1] $track[2]
}
