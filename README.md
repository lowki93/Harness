# Harness

## Contributor setup

### Agentic

The project uses [agentic](https://github.com/JohannWilfridCalixte/agent-skills) skills (requires Node.js for `npx`, and [`bun`](https://bun.sh)). Install them from the repository root:

```bash
npx skills@latest add JohannWilfridCalixte/agent-skills -a claude-code -s '*' -y
```

The project config is in [`.agentic.json`](.agentic.json). To set your own models, run `/agentic-configure --global`.
