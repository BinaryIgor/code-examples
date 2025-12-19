The repo you were given is solely for analysis to help with understanding and development. Remember to follow these principles when asked for changes:
* look and respect common convention and styles
* where styles/conventions differ, always prefer locality - what's local have priority over what's global
* whenever possible and applicable, add tests to introduced changes; if related file with tests exist, extend it instead of creating a new one
* return changes in the easiest to apply format possible


If in doubt, here are some guiding conventions:
* no should in tests - just returns, resolves, creates suffices
* prefer parameterized tests, unless readability suffers
* prefer local variables for data
* generally, build expected objects rather than checking single fields