---
name: lostark-deployment
description: Deploy this project on Vercel, Oracle VM, Railway, and FreeDB with env-var guidance and cross-domain CSRF notes. Use for setup, migration, or troubleshooting deployment configs.
---

# Lostark Deployment Guide

## Workflow
1. Read `docs/deployment/README.md` for the overall topology and env matrix.
2. Use the platform-specific doc for step-by-step setup:
   - Vercel: `docs/deployment/vercel.md`
   - Oracle VM: `docs/deployment/oracle-vm.md`
   - Railway: `docs/deployment/railway.md`
   - FreeDB: `docs/deployment/freedb.md`
3. For cross-domain frontend/backend, highlight the CSRF and cookie settings.
4. Keep environment variable names exact; never include secret values.

## Output expectations
- Provide exact build/start commands per platform.
- List required environment variables for the selected target.
- Include health-check endpoints when relevant.

## Resources
- `docs/deployment/README.md`
- `docs/deployment/vercel.md`
- `docs/deployment/oracle-vm.md`
- `docs/deployment/railway.md`
- `docs/deployment/freedb.md`
