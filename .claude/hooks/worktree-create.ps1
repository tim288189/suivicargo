#!/usr/bin/env powershell
# Hook WorktreeCreate — Claude Code (v6 2026-05-09 — fix path marker + note explicative)
#
# Source : ~/.claude/templates/a2k-basics/hooks/worktree-create.ps1
# Cf. ~/.claude/rules/actions_governance.md § "Mécanique override worktree sub-agent (hook v6+)"
#
# ===========================================================================
# QU'EST-CE QU'UN MARKER FILE ? (mécanisme override per-dispatch v5+)
# ===========================================================================
#
# Problème résolu : le mécanisme "env var" pour overrider la source/branch d'un
# sub-agent worktree NE MARCHE PAS depuis l'orchestrateur Bash tool. Raisons
# (validées empirique 2026-05-09) :
#   - Bash tool ne persiste PAS state shell entre calls (system prompt explicite)
#   - Harness Claude Code snapshot env au lancement → modif env système après
#     lancement = no-op sur process harness
#   - Hooks lancés comme child process = héritent env snapshot harness
#
# Solution : fichier marker filesystem (= state partagé accessible orchestrateur
# Bash tool ET hook au runtime).
#
# CONVENTION (v6 — corrigée post-empirique Test 2) :
#   - Marker source : <ORCHESTRATEUR-CWD>/.claude/.worktree-source.tmp
#   - Marker branch : <ORCHESTRATEUR-CWD>/.claude/.worktree-branch-name.tmp
#
#   ⚠ <ORCHESTRATEUR-CWD> = cwd de l'orchestrateur AU MOMENT du dispatch
#   (= sa worktree orchestrateur si harness option "worktree" cochée, sinon
#   develop racine ou autre branche locale). PAS le repo principal racine.
#
# WORKFLOW PER-DISPATCH :
#   1. Orchestrateur Bash : Set-Content .claude/.worktree-source.tmp "HEAD"
#      (path relatif depuis cwd orchestrateur — équivalent
#      <ORCHESTRATEUR-CWD>/.claude/.worktree-source.tmp en absolu)
#   2. (optionnel) Set-Content .claude/.worktree-branch-name.tmp "<branch-name>"
#   3. Dispatch Agent({isolation: "worktree", ...})
#   4. Hook v6 lit ces fichiers depuis $cwd payload + AUTO-SUPPRIME post-lecture
#   5. Sub-agent spawne avec source/branch override
#   6. Pas de cleanup orchestrateur nécessaire (auto déjà fait par hook)
#
# CYCLE DE VIE :
#   - Marker file = ÉPHÉMÈRE (créé orchestrateur → lu+supprimé hook)
#   - Auto-cleanup : si hook trouve le marker, le supprime IMMÉDIATEMENT après
#     lecture (pas de marker orphelin si flow normal)
#   - Garde-fou : marker orphelin survivable si crash hook entre lecture et
#     suppression (rare). Cleanup manuel via Remove-Item si besoin.
#
# CASCADE PRIORITÉ source/branch :
#   1. Marker file (per-dispatch override actuel)
#   2. Env var legacy (jamais déclenché Bash tool — fallback si invocation autre)
#   3. Payload du harness (base_branch / worktree_name si présents)
#   4. Fallback final (origin/HEAD pour source, agent-<id> ou wt-<sess>-<ts> pour name)
#
# GITIGNORED : <repo>/.gitignore doit contenir
#   .claude/.worktree-source.tmp
#   .claude/.worktree-branch-name.tmp
# (sinon markers seront vus dans git status / risque commit accidentel)
#
# ===========================================================================
#
# CORRECTIONS v6 (post-empirique Test 2) :
#   - B4 : marker path lecture = $cwd payload (= cwd orchestrateur), PAS $basePath
#          (= repo principal racine). Bug v5 : orchestrateur écrit marker dans
#          son cwd worktree, hook v5 le cherchait dans repo principal racine
#          → path d'écriture ≠ path de lecture → marker jamais lu, fallback final.
#   - Note explicative complète en header (cf. ci-dessus).
#
# Cf. https://code.claude.com/docs/en/hooks.md § WorktreeCreate

$rawInput = [Console]::In.ReadToEnd()
[Console]::Error.WriteLine("[hook v6 DIAGNOSTIC] raw_payload=$rawInput")

$inputJson = $rawInput | ConvertFrom-Json
$sessionId = $inputJson.session_id
$cwd = $inputJson.cwd
$agentId = $inputJson.agent_id
$payloadWorktreeName = $inputJson.worktree_name
$payloadBaseBranch = $inputJson.base_branch

[Console]::Error.WriteLine("[hook v6 DIAGNOSTIC] parsed: session_id=$sessionId cwd=$cwd agent_id=$agentId worktree_name=$payloadWorktreeName base_branch=$payloadBaseBranch")

# B1 fix : basePath via --git-common-dir (pour git worktree add target dans repo principal)
$gitCommonDir = git -C "$cwd" rev-parse --git-common-dir 2>$null
if (-not $gitCommonDir -or $LASTEXITCODE -ne 0) {
    [Console]::Error.WriteLine("[hook v6] ERROR: cannot resolve --git-common-dir from cwd=$cwd")
    exit 2
}
Push-Location $cwd
try { $gitCommonDirAbs = (Resolve-Path $gitCommonDir).Path } finally { Pop-Location }
$basePath = Split-Path $gitCommonDirAbs -Parent
$basePath = $basePath -replace '/', '\'

# B4 FIX v6 : Read marker files depuis $cwd ORCHESTRATEUR (PAS $basePath repo principal)
# Permet pattern multi-orchestrator parallèle + cohérence path écriture/lecture
$cwdNormalized = $cwd -replace '/', '\'
$markerSource = Join-Path $cwdNormalized ".claude\.worktree-source.tmp"
$markerBranch = Join-Path $cwdNormalized ".claude\.worktree-branch-name.tmp"

[Console]::Error.WriteLine("[hook v6] marker paths (depuis cwd orchestrateur=$cwdNormalized): source=$markerSource branch=$markerBranch")

$markerSourceValue = $null
if (Test-Path $markerSource) {
    $markerSourceValue = (Get-Content $markerSource -Raw).Trim()
    Remove-Item $markerSource -Force
    [Console]::Error.WriteLine("[hook v6] override source from marker: '$markerSourceValue' (marker auto-cleaned)")
} else {
    [Console]::Error.WriteLine("[hook v6] no marker source file at $markerSource (= no override, cascade fallback)")
}

$markerBranchValue = $null
if (Test-Path $markerBranch) {
    $markerBranchValue = (Get-Content $markerBranch -Raw).Trim()
    Remove-Item $markerBranch -Force
    [Console]::Error.WriteLine("[hook v6] override branch from marker: '$markerBranchValue' (marker auto-cleaned)")
}

# Cascade priorité (inchangée v5) : marker file > env var (legacy) > payload > fallback
$source = if ($markerSourceValue) {
    $markerSourceValue
} elseif ($env:CLAUDE_WORKTREE_SOURCE) {
    $env:CLAUDE_WORKTREE_SOURCE
} elseif ($payloadBaseBranch) {
    $payloadBaseBranch
} else {
    "origin/HEAD"
}

$worktreeName = if ($markerBranchValue) {
    $markerBranchValue
} elseif ($env:CLAUDE_WORKTREE_BRANCH_NAME) {
    $env:CLAUDE_WORKTREE_BRANCH_NAME
} elseif ($payloadWorktreeName) {
    $payloadWorktreeName
} elseif ($agentId) {
    "agent-$agentId"
} else {
    $sessionPrefix = if ($sessionId.Length -ge 8) { $sessionId.Substring(0, 8) } else { $sessionId }
    "wt-$sessionPrefix-$([DateTimeOffset]::Now.ToUnixTimeSeconds())"
}

$branchName = $worktreeName
$worktreePath = Join-Path $basePath ".claude\worktrees\$worktreeName"

[Console]::Error.WriteLine("[hook v6] FINAL: worktreeName=$worktreeName source=$source path=$worktreePath")

# B3 fix : git command sans ErrorActionPreference Stop, $LASTEXITCODE check explicit
$gitOutput = & git -C "$basePath" worktree add -B "$branchName" "$worktreePath" "$source" 2>&1
$exitCode = $LASTEXITCODE
$gitOutput | ForEach-Object { [Console]::Error.WriteLine("[git] $_") }

if ($exitCode -ne 0) {
    [Console]::Error.WriteLine("[hook v6] ERROR: git worktree add failed (exit $exitCode)")
    exit 2
}

Write-Output $worktreePath
