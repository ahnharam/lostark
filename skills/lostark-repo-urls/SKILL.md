---
name: lostark-repo-urls
description: Standardize repository URLs for this project. Use when providing clone/remote commands, linking to GitHub files/issues/PRs, or preparing for a future GitLab migration.
---

# Lostark Repo URLs

## Base URLs
- Web: `https://github.com/ahnharam/lostark`
- Git (HTTPS): `https://github.com/ahnharam/lostark.git`

## Workflow
1. Use the web base URL for links to files, issues, PRs, or actions.
2. Use the HTTPS URL with `.git` for `git clone` and `git remote` commands.
3. Prefer relative paths in docs; only use absolute URLs when needed.
4. If GitLab migration comes up, ask for the new base URL and swap it consistently.

## Output expectations
- Keep URLs exact; do not add tokens.
- Provide both web and git URLs when asked for access info.
