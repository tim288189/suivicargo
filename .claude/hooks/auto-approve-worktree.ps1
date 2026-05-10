# ============================================================
# Hook PreToolUse pour Claude Code (Windows / PowerShell)
# Auto-approuve les commandes Bash dans un worktree isole.
# Bloque les commandes systeme dangereuses et les operations
# git destructives ou qui contournent les controles.
#
# Source : ~/.claude/templates/a2k-basics/hooks/auto-approve-worktree.ps1
# Cf. ~/.claude/rules/git_workflow.md § "Pré-requis set-up A2K"
# ============================================================

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# ------------------------------------------------------------
# Logging
# ------------------------------------------------------------
$projectDir = $env:CLAUDE_PROJECT_DIR
if (-not $projectDir) { $projectDir = (Get-Location).Path }
$logDir  = Join-Path $projectDir '.claude\hooks'
$logFile = Join-Path $logDir 'debug.log'

if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir -Force | Out-Null
}

function Write-Log($msg) {
    $ts = Get-Date -Format 'yyyy-MM-dd HH:mm:ss.fff'
    Add-Content -Path $logFile -Value "[$ts] $msg" -Encoding UTF8
}

Write-Log "=== Hook invoque ==="

# ------------------------------------------------------------
# Lecture du payload JSON sur stdin
# ------------------------------------------------------------
$inputJson = [Console]::In.ReadToEnd()
Write-Log "Payload brut: $inputJson"

try {
    $payload = $inputJson | ConvertFrom-Json
} catch {
    Write-Log "ERREUR parsing JSON: $_"
    exit 0
}

$toolName = $payload.tool_name
$cwd      = $payload.cwd
$command  = $payload.tool_input.command

Write-Log "tool_name='$toolName' cwd='$cwd'"
Write-Log "command='$command'"

# On ne s'occupe que des appels Bash
if ($toolName -ne 'Bash') {
    Write-Log "Skip: pas un Bash (recu: '$toolName')"
    exit 0
}

# ------------------------------------------------------------
# Detection worktree
# ------------------------------------------------------------
$isWorktree = $false
$cwdNorm = $cwd -replace '\\', '/'
Write-Log "cwd normalise: '$cwdNorm'"

# Markers de chemin (le plus specifique en premier)
$worktreeMarkers = @(
    '.claude/worktrees',  # emplacement par defaut Claude Code
    '.worktrees',
    'worktrees',
    'trees',
    '.worktree'
)

foreach ($m in $worktreeMarkers) {
    $escaped = [regex]::Escape($m)
    if ($cwdNorm -match "/$escaped/") {
        $isWorktree = $true
        Write-Log "Match marker: '$m'"
        break
    }
}

# Fallback: detection via git pour worktrees a chemins arbitraires
if (-not $isWorktree -and (Test-Path $cwd)) {
    try {
        Push-Location $cwd
        $gitDir = git rev-parse --git-dir 2>$null
        Pop-Location
        Write-Log "git rev-parse --git-dir => '$gitDir'"
        if ($gitDir -and ($gitDir -replace '\\','/') -match '/worktrees/') {
            $isWorktree = $true
            Write-Log "Detection git: worktree confirme"
        }
    } catch {
        Write-Log "Erreur git rev-parse: $_"
    }
}

if (-not $isWorktree) {
    Write-Log "Decision: PAS UN WORKTREE -> flow normal"
    exit 0
}

# ------------------------------------------------------------
# LISTE NOIRE: commandes systeme dangereuses
# ------------------------------------------------------------
$systemDangerous = @(
    'rm\s+-rf\s+/',
    'rm\s+-rf\s+~',
    'rm\s+-rf\s+\$HOME',
    'mkfs',
    'dd\s+if=.*of=/dev/',
    'Format-Volume',
    'Remove-Item.*-Recurse.*C:\\',
    'rmdir\s+/s.*C:\\',
    'curl.*\|\s*sh',
    'wget.*\|\s*sh',
    'iwr.*\|\s*iex',
    'Invoke-Expression.*DownloadString',
    'chmod\s+-R\s+777\s+/',
    'sudo\s+rm'
)

# ------------------------------------------------------------
# LISTE NOIRE: operations git destructives ou qui contournent
# les controles (force push, no-verify, reecriture d'historique,
# modification de config globale, gpg sign desactive, etc.)
# ------------------------------------------------------------
$gitDangerous = @(
# Push (toutes formes, force ou non)
    '\bgit\s+push\b',

    # Reset destructif
    '\bgit\s+reset\s+--hard\b',

    # Commits qui contournent les hooks ou la signature
    '\bgit\s+commit\b[^&;|]*--no-verify\b',
    '\bgit\s+commit\b[^&;|]*--no-gpg-sign\b',
    '\bgit\s+commit\b[^&;|]*--amend\b',

    # Forme: git -c commit.gpgsign=false ... (avant le mot commit)
    # NOTE: inline (?-i) force case-sensitive sur ces patterns uniquement.
    # Sans ce flag, PowerShell -match conflate -c (--config, dangereux:
    # peut desactiver gpg sign) et -C (--chdir, legitime: change cwd).
    # Faux-positif identifie 2026-05-10 sur 'git -C <path> commit' (workflow
    # orchestrateur worktree). Fix β: surgical, autres patterns inchanges.
    '(?-i)\bgit\s+-c\s+commit\.gpgsign=false\b',
    '(?-i)\bgit\s+-c\s+[^\s]+\s+commit\b',

    # Rebase interactif (reecriture d'historique)
    '\bgit\s+rebase\s+(-i|--interactive)\b',

    # Modification d'identite ou de config globale
    '\bgit\s+config\s+user\.email\b',
    '\bgit\s+config\s+user\.name\b',
    '\bgit\s+config\s+--global\b',

    # Reecriture / nettoyage d'historique
    '\bgit\s+filter-branch\b',
    '\bgit\s+reflog\s+expire\b',
    '\bgit\s+reflog\s+delete\b',
    '\bgit\s+gc\b[^&;|]*--aggressive\b',
    '\bgit\s+update-ref\s+-d\b'
)

$allDangerous = $systemDangerous + $gitDangerous

foreach ($pattern in $allDangerous) {
    if ($command -match $pattern) {
        Write-Log "BLOCK pattern matche: '$pattern'"
        $blockResponse = @{
            hookSpecificOutput = @{
                hookEventName            = 'PreToolUse'
                permissionDecision       = 'deny'
                permissionDecisionReason = "Commande bloquee par hook worktree (pattern: $pattern). Si necessaire, execute-la manuellement hors sub-agent."
            }
        } | ConvertTo-Json -Compress -Depth 5
        Write-Output $blockResponse
        Write-Log "Sortie: $blockResponse"
        exit 0
    }
}

# ------------------------------------------------------------
# Tout va bien -> auto-approuver
# ------------------------------------------------------------
$approveResponse = @{
    hookSpecificOutput = @{
        hookEventName            = 'PreToolUse'
        permissionDecision       = 'allow'
        permissionDecisionReason = 'Auto-approuve: commande dans un worktree isole'
    }
} | ConvertTo-Json -Compress -Depth 5

Write-Output $approveResponse
Write-Log "Decision: ALLOW"
Write-Log "Sortie: $approveResponse"
exit 0
