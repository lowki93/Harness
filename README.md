# Harness

## Contributor setup

### Agentic

The project vendors the [agentic](https://github.com/JohannWilfridCalixte/agent-skills) skills in `.agents/skills/`, linked from `.claude/skills/`: they work after a clone. They need [`bun`](https://bun.sh).

To update them (requires Node.js for `npx`), then commit the result in a PR:

```bash
npx skills@latest update -p -y
```

The project config is in [`.agentic.json`](.agentic.json). To set your own models, run `/agentic-configure --global`.
