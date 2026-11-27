# Pull Request Conventions

Apply these to every pull request opened from this project. Title and body are always written in English.

## Title

Format: `type(scope): [TICKET-ID] description`

- `type`: one of `feat`, `fix`, `update`, `impr`, `revert`
  - `feat`: new feature or documentation
  - `fix`: bug fix
  - `impr`: improvement to existing code, tooling or workflow
  - `update`: dependency, changelog or release update
  - `revert`: revert of an earlier change
- `scope`: the area touched, lowercase (e.g. `router`, `tests`, `docs`, `workflow`)
- `[TICKET-ID]`: the tracker ticket (e.g. `[SWI-5]`). Leave it out when the work has no ticket.
- `description`: short, imperative, lowercase, no trailing period

Examples:

- `feat(tests): [SWI-5] add NavigationLink extension tests`
- `fix(workflow): handle fork PRs in the review job`
- `update(changelog): v1.2.0 release`

## Body

Use this template:

```
**What**:
Clear summary of what this PR accomplishes

**Breaking Changes**:
Highlight any API, public interface, or dependency changes

**Review notes**: (optional)
Reviewer findings you chose not to fix, each with the reason
```

Dependabot PRs keep their own titles and bodies.
