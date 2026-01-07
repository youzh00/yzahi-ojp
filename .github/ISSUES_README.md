# GitHub Issues Creation Instructions

## Overview
This file contains instructions for creating 20 GitHub issues for the StatementServiceImpl refactoring project. Each issue represents one public method that needs to be refactored using the Action pattern.

## Issue Data Location
All issue data is stored in: `.github/ISSUES_TO_CREATE.json`

This JSON file contains an array of 20 issues, each with:
- `title`: Issue title
- `labels`: Array of labels (e.g., ["good first issue", "ojp-server", "feature"])
- `body`: Complete issue description in Markdown format

## How to Create the Issues

### Option 1: Manual Creation via GitHub Web UI
1. Go to https://github.com/Open-J-Proxy/ojp/issues/new
2. For each object in the JSON array:
   - Copy the `title` field as the issue title
   - Copy the `body` field as the issue description
   - Add the labels from the `labels` array
   - Click "Submit new issue"
3. Repeat for all 20 issues

### Option 2: Using GitHub CLI (Recommended)
```bash
# Install GitHub CLI if not already installed
# https://cli.github.com/

# Navigate to repository
cd /path/to/ojp

# Create issues from JSON (requires jq)
cat .github/ISSUES_TO_CREATE.json | jq -c '.[]' | while read issue; do
  title=$(echo "$issue" | jq -r '.title')
  body=$(echo "$issue" | jq -r '.body')
  labels=$(echo "$issue" | jq -r '.labels | join(",")')
  
  gh issue create \
    --title "$title" \
    --body "$body" \
    --label "$labels"
    
  echo "Created: $title"
  sleep 1  # Rate limiting
done
```

### Option 3: Using GitHub API
```bash
# Using curl with GitHub API
GITHUB_TOKEN="your_token_here"
REPO="Open-J-Proxy/ojp"

cat .github/ISSUES_TO_CREATE.json | jq -c '.[]' | while read issue; do
  curl -X POST \
    -H "Authorization: token $GITHUB_TOKEN" \
    -H "Accept: application/vnd.github.v3+json" \
    "https://api.github.com/repos/$REPO/issues" \
    -d "$issue"
  
  sleep 1  # Rate limiting
done
```

## Issue Summary

### By Complexity
- **Simple (8 issues)**: Good for first-time contributors
  - startTransaction, commitTransaction, rollbackTransaction, terminateSession
  - xaEnd, xaRecover, xaForget, xaSetTransactionTimeout, xaGetTransactionTimeout, xaIsSameRM
  
- **Medium (10 issues)**: Require moderate understanding
  - callResource
  - executeUpdate, executeQuery, fetchNextRows
  - xaStart, xaPrepare, xaCommit, xaRollback

- **Complex (2 issues)**: Advanced streaming operations
  - createLob, readLob

### By Category
- **Transaction (3)**: startTransaction, commitTransaction, rollbackTransaction
- **Session (1)**: terminateSession
- **Resource (1)**: callResource
- **Statement (3)**: executeUpdate, executeQuery, fetchNextRows
- **XA Operations (10)**: xaStart, xaEnd, xaPrepare, xaCommit, xaRollback, xaRecover, xaForget, xaSetTransactionTimeout, xaGetTransactionTimeout, xaIsSameRM
- **LOB (2)**: createLob, readLob

## Labels Used
- `good first issue`: For simple complexity issues (8 issues)
- `ojp-server`: All issues (20 issues)
- `feature`: All issues (20 issues)

## Important Notes

1. **Update PR Reference**: Before creating issues, replace "PR #XXX" in the JSON file with the actual PR number from this refactoring work.

2. **Issue Order**: Consider creating issues in phases:
   - Phase 1: Simple transaction issues (good first issues)
   - Phase 2: Medium complexity statement execution
   - Phase 3: XA operations
   - Phase 4: Complex LOB operations

3. **Milestone**: Consider creating a milestone "StatementServiceImpl Refactoring" and assigning all issues to it.

4. **Project Board**: Consider adding all issues to a project board for better tracking.

5. **Documentation**: All issues reference `documents/designs/STATEMENTSERVICE_ACTION_PATTERN_MIGRATION.md` which contains:
   - Mermaid class diagram
   - Mermaid sequence diagram
   - Complete implementation guide
   - Reference implementation details

## Verification
After creating all issues:
- [ ] 20 issues created
- [ ] All have correct labels
- [ ] "good first issue" applied to 8 simple issues
- [ ] All reference the migration guide
- [ ] All reference this PR as template
- [ ] Issues are properly categorized/milestone assigned

## Cleanup
Once all issues are created successfully, this file and ISSUES_TO_CREATE.json can be removed from the repository if desired, or kept for reference.
